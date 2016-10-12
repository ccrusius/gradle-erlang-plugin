package org.ccrusius.erlang.tasks

import org.gradle.api.Task
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.ResetOp

import org.ccrusius.erlang.utils.FileUtils

/// ===========================================================================
///
/// Call rebar
///
/// task rebar(type: Rebar) {
///   outputs.file("some/generated/file")
///   setRebarVersion "version"
///   setRebarTarget "target"
///   setDirectory "dir"
/// }
/// rebar.finalize() /// THIS IS NECESSARY
///
/// ===========================================================================
@ParallelizableTask
class Rebar extends DefaultTask {

  Rebar() {
      setDescription("Call rebar")
  }

  /// -------------------------------------------------------------------------
  ///
  /// Create all the sub-tasks, and set up dependencies
  ///
  /// -------------------------------------------------------------------------
  void finalize() {
    this.dependsOn getBuildRebarTask()
  }

  /// -------------------------------------------------------------------------
  ///
  /// The desired rebar version
  ///
  /// -------------------------------------------------------------------------

  @Internal
  String getRebarVersion() {
    if(!this.rebarVersion) {
      this.rebarVersion = '3.3.1'
    }
    return this.rebarVersion
  }

  void setRebarVersion(String version) {
    this.rebarVersion = version
  }

  private String rebarVersion

  /// -------------------------------------------------------------------------
  ///
  /// The rebar target
  ///
  /// -------------------------------------------------------------------------

  @Input
  String getRebarTarget() {
    if(this.target) {
      return this.target
    }
    return 'compile'
  }

  void setRebarTarget(String target) {
    this.target = target
  }

  private String target

  /// -------------------------------------------------------------------------
  ///
  /// The rebar source tree directory
  ///
  /// -------------------------------------------------------------------------

  @Internal
  File getRebarDir() {
    if(this.rebarDir) { return project.file(this.rebarDir) }
    return project.file("${project.buildDir}/erlang/ext/rebar-${getRebarVersion()}")
  }

  void setRebarDir(Object rebarDir) {
    this.rebarDir = rebarDir
  }

  private Object rebarDir

  /// -------------------------------------------------------------------------
  ///
  /// The directory where to call 'rebar' from
  ///
  /// -------------------------------------------------------------------------

  @Input
  File getDirectory() {
    project.file(this.directory)
  }

  void setDirectory(Object directory) {
    this.directory = directory
  }

  private Object directory

  /// -------------------------------------------------------------------------
  ///
  /// The rebar executable, and the task that generates it.
  ///
  /// -------------------------------------------------------------------------

  @Internal
  File getRebarExe() {
    def major = getRebarVersion()[0]
    new File(getRebarDir(), major == '2' ? 'rebar' : "rebar${major}")
  }

  /// -------------------------------------------------------------------------
  ///
  /// Task action: call rebar
  ///
  /// -------------------------------------------------------------------------

  @TaskAction
  void build() {
    String target = getRebarTarget()
    String exe = FileUtils.getAbsolutePath(getRebarExe())

    def cmdline = [ exe, target ]
    print("====== ${cmdline.join(',')}")
    def process = new ProcessBuilder(cmdline)
                      .redirectErrorStream(true)
                      .directory(getDirectory())
                      .start()
    process.inputStream.eachLine { println it }
    process.waitFor()
    if(process.exitValue() != 0) {
      throw new GradleException("${exe} ${target} failed.")
    }
  }

  /// -------------------------------------------------------------------------
  ///
  /// The task that builds the rebar executable
  ///
  /// -------------------------------------------------------------------------

  @Internal
  Task getBuildRebarTask() {
    def rebarDir = getRebarDir()
    def version = getRebarVersion()
    def name = "rebar-${version}-build"
    if(!this.buildRebarTask) {
      this.buildRebarTask = project.tasks.findByPath(name)
    }
    if(!this.buildRebarTask) {
      def exe = getRebarExe()
      this.buildRebarTask = project.tasks.create(name, DefaultTask.class)
      this.buildRebarTask.with {
        setDescription("Build rebar ${version} executable")
        dependsOn getFetchRebarTask()
        outputs.file(exe)
        doLast {
          def cmdline = [ './bootstrap' ]
          def process = new ProcessBuilder(cmdline)
                            .redirectErrorStream(true)
                            .directory(rebarDir)
                            .start()
          process.inputStream.eachLine { println it }
          process.waitFor()
          if(process.exitValue() != 0) {
            throw new GradleException('rebar failed.')
          }
        }
      }
    }
    return this.buildRebarTask
  }

  private Task buildRebarTask

  /// -------------------------------------------------------------------------
  ///
  /// The task that fetches the source from github
  ///
  /// -------------------------------------------------------------------------

  @Internal
  Task getFetchRebarTask() {
    def rebarDir = getRebarDir()
    def version = getRebarVersion()
    def name = "rebar-${version}-fetch"
    if(!this.fetchRebarTask) {
      this.fetchRebarTask = project.tasks.findByPath(name)
    }
    if(!this.fetchRebarTask) {
      this.fetchRebarTask = project.tasks.create(name, DefaultTask.class)
      this.fetchRebarTask.with {
        setDescription("Fetch rebar ${version} sources from github")
        outputs.file(new File("${rebarDir}/.git/ref/tags/${version}"))
        outputs.file(new File("${rebarDir}/bootstrap"))
        doLast {
          def git = rebarDir.exists() ?
            Grgit.init(dir: rebarDir) :
            Grgit.clone(dir: rebarDir, uri: "https://github.com/rebar/rebar")
          git.fetch()
          git.reset(commit: version, mode: ResetOp.Mode.HARD)
        }
      }
    }
    return this.fetchRebarTask
  }

  private Task fetchRebarTask
}
