package org.ccrusius.erlang.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

import org.ccrusius.erlang.utils.FileUtils
import org.ccrusius.erlang.utils.AppFile

/**
 * @author Cesar Crusius
 */
@ParallelizableTask
class Application extends DefaultTask {

  @TaskAction
  void build() {
    // All the work is done via sub-tasks.
    logger.info("Building OTP application")
  }

  /**
   * Create the application sub-tasks.
   */
  void createSubTasks(Project project) {
    if(getAppFileFile()) {
      createAppFileSubTasks(project)
      createBeamSubTasks(project)
      createInstallTask(project)
    }
    linkToParent(project)
  }

  @Internal
  private
  AppFile getAppFile() { project.extensions.erlang.appFile }

  @Internal
  private
  File getAppFileFile() { getAppFile().appFile }

  @Internal
  private
  String getAppName() { getAppFile().appName }

  @Internal
  private
  File getOutputDir() { project.extensions.ebuildAppDir }

  @Internal
  File getInstallDir() {
    def app = getAppFile()
    new File("${project.buildDir}/install/erlang-lib/${app.appDirName}")
  }

  @Internal
  private
  List getSourceFiles() {
    def all = new File(project.projectDir, "src").listFiles()
    all.findAll { FileUtils.getExtension(it) == '.erl' }
  }

  /// -------------------------------------------------------------------------
  ///
  /// Create the .app file generation task
  ///
  /// -------------------------------------------------------------------------
  private
  void createAppFileSubTasks(Project project) {
    def app = getAppFile()
    def dir = new File(getOutputDir(), 'ebin')
    def file = app.appFile
    def out = new File(dir, "${app.appName}.app")

    this.appFileTask = project.getTasks().create(file.name, DefaultTask.class)
    this.appFileTask.setDescription("Generate application '.app' file")
    this.appFileTask.inputs.file(file)
    this.appFileTask.outputs.file(out)
    this.appFileTask << { app.write(out) }
    dependsOn this.appFileTask
  }

  private DefaultTask appFileTask = null

  /// -------------------------------------------------------------------------
  ///
  /// Create the Erlang compilation tasks.
  ///
  /// For each .erl file in src/, produce a .beam file in ebin/
  ///
  /// -------------------------------------------------------------------------
  private
  void createBeamSubTasks(Project project) {
    def dir = new File(getOutputDir(), 'ebin')

    beamTasks.addAll(getSourceFiles().collect {
                       def task = project.getTasks().create(
                         FileUtils.getCompiledName(it),
                         Compile.class)
                       task.setDescription("Compile ${it.name}")
                       task.setSourceFile(it)
                       task.setOutputDir(dir)
                       dependsOn task
                       task
                     })
  }

  private final List<DefaultTask> beamTasks = new ArrayList<DefaultTask>()

  /// -------------------------------------------------------------------------
  ///
  /// Create the application installation task.
  ///
  /// -------------------------------------------------------------------------
  private
  void createInstallTask(Project project) {
    def app = getAppFile()
    def dir = getInstallDir()
    def ebin = new File(dir, 'ebin')

    def install = project.tasks.create(
      "install${app.appName.capitalize()}Application",
      DefaultTask.class)

    def friendly = FileUtils.getAbsolutePath(dir)
    def pdir = FileUtils.getAbsolutePath(project.projectDir)
    if(friendly.startsWith(pdir)) {
      friendly = (friendly - pdir).substring(1)
    }
    install.setDescription("Install application in ${friendly}")
    ///
    /// Copy the .app file
    ///
    install.dependsOn this.appFileTask
    def srcApp = this.appFileTask.outputs.files.singleFile
    install.inputs.file(srcApp)
    install.outputs.file(new File(ebin, srcApp.name))
    install << { project.copy {
        from srcApp
        into ebin
      }}
    ///
    /// Copy the .beam files
    ///
    install.outputs.dir(ebin)
    beamTasks.each {
      install.dependsOn it
      def out = it.outputFile
      install.inputs.file(out)
      install.outputs.file(new File(ebin, out.name))
      install << { project.copy {
          from out
          into ebin
        }}
    }
  }

  /// -------------------------------------------------------------------------
  ///
  /// Link application task to parent's application task(s).
  ///
  /// -------------------------------------------------------------------------
  private
  void linkToParent(Project project) {
    if(project.parent) {
      project.parent.tasks.withType(Application).collect {
        it.dependsOn this
      }
    }
  }
}
