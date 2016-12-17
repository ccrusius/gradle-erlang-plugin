package org.ccrusius.erlang.tasks

import org.gradle.api.Task
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

import org.ccrusius.erlang.utils.FileUtils
import org.ccrusius.erlang.utils.AppFile

/// ===========================================================================
///
/// Application
///
/// Builds an application from an OTP-compliant directory structure:
///
///    <root>/ebin/<app>.app
///    <root>/src/<src>.erl*
///
/// task my_app(type: Application) {
///   baseDir 'blah'
///   [outDir 'bloh']
/// }
///
/// ===========================================================================
@ParallelizableTask
class Application extends DefaultTask {

  Application() {
    project.tasks.findByPath('ebuild').dependsOn this
  }

  /// -------------------------------------------------------------------------
  ///
  /// Build the application
  ///
  /// -------------------------------------------------------------------------
  @TaskAction
  void build() {
    // All the work is done via sub-tasks.
    logger.info("Building OTP application")
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
        dependsOn getOutAppFileTask()
        getOutBeamFileTasks().each { this.dependsOn it }
      })
  }

  /// -------------------------------------------------------------------------
  ///
  /// The base directory. This is the root of the OTP application source tree.
  ///
  /// -------------------------------------------------------------------------

  @Internal
  File getBaseDir() {
    project.file(this.baseDir)
  }

  void setBaseDir(Object baseDir) {
    this.baseDir = baseDir
  }

  private Object baseDir

  /// -------------------------------------------------------------------------
  ///
  /// The output directory
  ///
  /// -------------------------------------------------------------------------

  @Internal
  File getOutDir() {
    if(this.outDir) { return project.file(this.outDir) }
    return project.file(
      "${project.buildDir}/erlang/lib/${getAppName()}-${getVersion()}")
  }

  void setOutDir(Object outDir) {
    this.outDir = outDir
  }

  private Object outDir

  /// -------------------------------------------------------------------------
  ///
  /// The application file
  ///
  /// -------------------------------------------------------------------------

  @Internal
  File getAppFile() {
    if(this.appFile) { return project.file(this.appFile) }

    def dir = new File(getBaseDir(), 'ebin')
    def candidates = dir.listFiles().findAll {
      FileUtils.getExtension(it) == '.app'
    }
    switch(candidates.size()) {
      case { it == 0 }:
        throw new GradleException(
          "Erlang: No .app file in '${dir.absolutePath}'")
      case { it > 1 }:
        throw new GradleException(
          "Erlang: Too many .app files in '${dir.absolutePath}'")
    }
    this.appFile = candidates[0]
    return this.appFile
  }

  private Object appFile

  /// -------------------------------------------------------------------------
  ///
  /// The application name
  ///
  /// -------------------------------------------------------------------------

  @Internal
  String getAppName() {
    if(!this.appName) {
      this.appName = new AppFile(project, getAppFile()).getAppName()
    }
    return this.appName
  }

  private String appName

  /// -------------------------------------------------------------------------
  ///
  /// The application version
  ///
  /// -------------------------------------------------------------------------

  @Input
  String getVersion() {
    if(!this.appVersion) {
      this.appVersion = new AppFile(project, getAppFile()).getAppVersion()
    }
    return this.appVersion
  }

  void setVersion(String version) {
    this.appVersion = version
  }

  private String appVersion

  /// -------------------------------------------------------------------------
  ///
  /// The application file build-defined properties
  ///
  /// -------------------------------------------------------------------------

  @Input
  List<String> getAppFileVariableNames() {
    this.appFileVariables.collect { it.get(0) }
  }

  @Input
  List<String> getAppFileVariableValues() {
    this.appFileVariables.collect { it.get(1) }
  }

  void addAppFileVariable(String atom, String replacement) {
    this.appFileVariables.add(new Tuple2(atom, replacement))
  }

  List<Tuple2> getAppFileVariables() {
    return appFileVariables
  }

  private List<Tuple2> appFileVariables = new ArrayList<Tuple2>()

  /// -------------------------------------------------------------------------
  ///
  /// The Erlang source files
  ///
  /// -------------------------------------------------------------------------

  @Internal
  List<File> getSourceFiles() {
    def dir = new File(getBaseDir(), 'src')
    return dir.listFiles().findAll { FileUtils.getExtension(it) == '.erl' }
  }

  /// -------------------------------------------------------------------------
  ///
  /// The output app file, and the task that generates it.
  ///
  /// -------------------------------------------------------------------------

  @Internal
  File getOutAppFile() {
    project.file("${getOutDir()}/ebin/${getAppName()}.app")
  }

  @Internal
  Conf getOutAppFileTask() {
    if(!this.outAppFileTask) {
      this.outAppFileTask = project.tasks.create(
        "${getOutDir().name}#${getOutAppFile().name}",
        Conf.class)
      this.outAppFileTask.with {
        setSource(getAppFile())
        setOutput(getOutAppFile())
        setReplacements(getAppFileVariables())
        setDescription("Generate '.app' file for ${getAppName()}")
      }
      this.outAppFileTask.setVersion(this.getVersion())
    }
    return this.outAppFileTask
  }

  private Conf outAppFileTask

  /// -------------------------------------------------------------------------
  ///
  /// Add compiler options
  ///
  /// -------------------------------------------------------------------------

  @Input
  List<String> getCompilerOpts() {
    this.compilerOpts
  }

  void setCompilerOpts(Object... opts) {
    compilerOpts.clear()
    compilerOpts.addAll(opts.collect{it.toString()})
  }

  void setCompilerOpts(List<Object> opts) {
    compilerOpts.clear()
    compilerOpts.addAll(opts.collect{it.toString()})
  }

  void addCompilerOpts(Object... opts) {
    compilerOpts.addAll(opts.collect{it.toString()})
  }

  void addCompilerOpts(List<Object> opts) {
    compilerOpts.addAll(opts.collect{it.toString()})
  }

  private List<String> compilerOpts = []

  /// -------------------------------------------------------------------------
  ///
  /// Get library directories from dependencies
  ///
  /// -------------------------------------------------------------------------

  @Internal
  Set<File> getExternalAppDirs() { this.externalAppDirs }

  void addExternalAppDirs(Object task) {
    switch(task) {
      case {!it}:
        return
      case {it instanceof Application}:
        externalAppDirs.addAll(task.outDir)
        break
      case {it instanceof PrecompiledApplication}:
        externalAppDirs.addAll(task.baseDir)
        break
    }
    if(task instanceof Task) {
      task.dependsOn.each {
        addExternalAppDirs(it)
      }
    }
  }

  @Override
  Task dependsOn(Object... paths) {
    paths.each { this.addExternalAppDirs(it) }
    super.dependsOn paths
  }

  private Set<File> externalAppDirs = []

  /// -------------------------------------------------------------------------
  ///
  /// The output beam files, and the tasks that generate them
  ///
  /// -------------------------------------------------------------------------

  @Internal
  List<File> getOutBeamFiles() {
    getSourceFiles().collect {
      project.file("${getOutDir()}/ebin/${FileUtils.getCompiledName(it)}")
    }
  }

  @Internal
  List<Compile> getOutBeamFileTasks() {
    if(!this.outBeamFileTasks) {
      this.outBeamFileTasks = new ArrayList<Compile>()
      def dir = new File(getOutDir(), 'ebin')

      this.outBeamFileTasks.addAll(
        getSourceFiles().collect {
          def name = FileUtils.getCompiledName(it)
          def task = project.getTasks().create(
            "${getOutDir().name}#${name}",Compile.class)
          task.setDescription("Compile ${it.name}")
          task.setSourceFile(it)
          task.setOutputDir(dir)
          getExternalAppDirs().each {
            task.addArguments(
              '-pa',
              FileUtils.getAbsolutePath(new File(it,'ebin')))
          }
          task.addArguments(getCompilerOpts())
          task
        })
    }
    return this.outBeamFileTasks
  }

  private List<Compile> outBeamFileTasks
}
