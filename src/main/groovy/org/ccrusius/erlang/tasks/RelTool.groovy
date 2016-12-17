package org.ccrusius.erlang.tasks

import org.gradle.api.Task
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

import org.ccrusius.erlang.utils.FileUtils
import org.ccrusius.erlang.utils.RelFile

/// ===========================================================================
///
/// Call reltool given a configuration file
///
/// task reltool(type: RelTool) {
///   configFile "rel.config"
///   [version "version"]
///   [outDir dir]
/// }
///
/// ===========================================================================
@ParallelizableTask
class RelTool extends DefaultTask {

  RelTool() {
    /// Always run when asked to
    outputs.upToDateWhen { false }
  }

  /// -------------------------------------------------------------------------
  ///
  /// Create all the sub-tasks, and set up dependencies
  ///
  /// -------------------------------------------------------------------------
  @Override
  public Task configure(Closure configClosure) {
    return super.configure(
      configClosure >> {
        dependsOn getOutRelFileTask()
      })
  }

  /// -------------------------------------------------------------------------
  ///
  /// The configuration file
  ///
  /// -------------------------------------------------------------------------

  @Internal
  File getConfigFile() {
    if(this.configFile) { return project.file(this.configFile) }
    null
  }

  void setConfigFile(Object file) {
    this.configFile = file
  }

  private Object configFile

  /// -------------------------------------------------------------------------
  ///
  /// The output directory
  ///
  /// -------------------------------------------------------------------------

  @OutputDirectory
  File getOutDir() {
    if(this.outDir) { return project.file(this.outDir) }
    return project.file("${project.buildDir}/erlang/rel/${getRelName()}-${getVersion()}")
  }

  void setOutDir(Object outDir) {
    this.outDir = outDir
  }

  private Object outDir

  /// -------------------------------------------------------------------------
  ///
  /// The release version
  ///
  /// -------------------------------------------------------------------------

  @Internal
  String getVersion() {
    if(!this.relVersion) {
      this.relVersion = new RelFile(project, getConfigFile()).getRelVersion()
    }
    return this.relVersion
  }

  void setVersion(String version) {
    this.relVersion = version
  }

  private String relVersion

  /// -------------------------------------------------------------------------
  ///
  /// The release name
  ///
  /// -------------------------------------------------------------------------

  @Internal
  String getRelName() {
    if(!this.relName && getConfigFile()) {
      this.relName = new RelFile(project, getConfigFile()).getRelName()
    }
    return this.relName
  }

  private String relName

  /// -------------------------------------------------------------------------
  ///
  /// The release config file build-defined properties
  ///
  /// -------------------------------------------------------------------------

  @Input
  List<String> getRelFileVariableNames() {
    this.relFileVariables.collect { it.get(0) }
  }

  @Input
  List<String> getRelFileVariableValues() {
    this.relFileVariables.collect { it.get(1) }
  }

  void addRelFileVariable(String atom, String replacement) {
    this.relFileVariables.add(new Tuple2(atom, replacement))
  }

  List<Tuple2> getRelFileVariables() {
    return relFileVariables
  }

  private List<Tuple2> relFileVariables = new ArrayList<Tuple2>()

  /// -------------------------------------------------------------------------
  ///
  /// The output rel file, and the task that generates it.
  ///
  /// -------------------------------------------------------------------------

  @Internal
  File getOutRelFile() {
    new File(getOutDir().parentFile, "${getRelName()}-${getVersion()}.config")
  }

  @Internal
  Conf getOutRelFileTask() {
    if(!this.outRelFileTask) {
      this.outRelFileTask = project.tasks.create(
        "${getOutDir().name}#${getOutRelFile().name}",
        Conf.class)
      this.outRelFileTask.with {
        setSource(getConfigFile())
        setOutput(getOutRelFile())
        setReplacements(getRelFileVariables())
        setDescription("Generate '.rel' file for ${getRelName()}")
      }
      this.outRelFileTask.setVersion(this.getVersion())
    }
    return this.outRelFileTask
  }

  private Conf outRelFileTask

  /// -------------------------------------------------------------------------
  ///
  /// Get library directories from dependencies
  ///
  /// -------------------------------------------------------------------------

  @Internal
  Set<File> getLibDirs() { this.libDirs }

  void addLibDirs(Object task) {
    switch(task) {
      case {!it}:
        return
      case {it instanceof Application}:
        libDirs.addAll(task.outDir.parentFile)
        break
      case {it instanceof PrecompiledApplication}:
        libDirs.addAll(task.baseDir.parentFile)
        break
    }
    if(task instanceof Task) {
      task.dependsOn.each {
        addLibDirs(it)
      }
    }
  }

  @Override
  Task dependsOn(Object... paths) {
    paths.each { this.addLibDirs(it) }
    super.dependsOn paths
  }

  private Set<File> libDirs = []

  /// -------------------------------------------------------------------------
  /// -------------------------------------------------------------------------

  @TaskAction
  void build() {
    def escript = project.extensions.erlang.installation.escript
    def dir = getOutDir()
    if(dir.exists()) { assert dir.deleteDir() }
    dir.mkdirs()

    if(getLibDirs().size() == 0) {
      throw new GradleException("No library directories. Did you forget the dependencies?")
    }

    escript.eval("""
io:format(\"reltool: Reading reltool configuration file~n\"),
{ok,[{sys, Props}]} = file:consult(\"${FileUtils.getAbsolutePath(getOutRelFile())}\"),

io:format(\"reltool: Massaging reltool configuration~n\"),
Conf = {sys, [ { lib_dirs, [
    \"${libDirs.collect{FileUtils.getAbsolutePath(it)}.join('","')}\"
 ] } | Props ] },

io:format(\"reltool: Conf=~p~n\",[Conf]),

io:format(\"reltool: Processing reltool configuration~n\"),
{ok, Spec} = reltool:get_target_spec([Conf]),

%%% io:format(\"reltool: Spec=~p~n\",[Spec]),

io:format(\"reltool: Producing release~n\"),
reltool:eval_target_spec(
    Spec,
    code:root_dir(),
    \"${FileUtils.getAbsolutePath(getOutDir())}\"),

io:format(\"reltool: Done~n\").
""")
  }
}
