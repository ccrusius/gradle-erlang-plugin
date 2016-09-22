package org.ccrusius.erlang.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

import org.ccrusius.erlang.ApplicationInfo
import org.ccrusius.erlang.utils.FileUtils

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
    if(getResourceFile()) {
      createAppFileSubTasks(project)
      createBeamSubTasks(project)
      createInstallTask(project)
    }
    linkToParent(project)
  }

  @Internal
  ApplicationInfo getAppInfo() {
    appInfo ? appInfo : project.extensions.erlang.appInfo
  }

  void setAppInfo(ApplicationInfo appInfo) {
    this.appInfo = appInfo
  }

  private ApplicationInfo appInfo = null

  @Internal
  private
  File getResourceFile() { getAppInfo().resourceFile }

  @Internal
  private
  String getAppName() { getAppInfo().name }

  @Internal
  private
  File getOutputDir() {
    new File(project.extensions.ebuildLibDir, getAppName())
  }

  @Internal
  File getInstallDir() {
    def app = getAppInfo()
    new File("${project.buildDir}/install/erlang-lib/${app.dirName}")
  }

  @Internal
  private
  List getSourceFiles() {
    def all = new File(getAppInfo().sourceDir, "src").listFiles()
    all.findAll { FileUtils.getExtension(it) == '.erl' }
  }

  /// -------------------------------------------------------------------------
  ///
  /// Create the .app file generation task
  ///
  /// -------------------------------------------------------------------------
  private
  void createAppFileSubTasks(Project project) {
    def app = getAppInfo()
    def dir = new File(getOutputDir(), 'ebin')
    def file = app.resourceFile
    def out = new File(dir, "${app.name}.app")

    this.appFileTask = project.getTasks().create(file.name, DefaultTask.class)
    this.appFileTask.setDescription("Generate application '.app' file")
    this.appFileTask.inputs.file(file)
    this.appFileTask.outputs.file(out)
    this.appFileTask << { app.write(out) }
    dependsOn this.appFileTask
  }

  private DefaultTask appFileTask = null

  @Optional
  @OutputFile
  File getOutputAppFile() {
    if(appFileTask) {
      appFileTask.outputs.files.singleFile
    }
  }

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

  @Optional
  @OutputFiles
  List<File> getOutputBeams() {
    if(beamTasks.size() > 0) {
      beamTasks.collect {
        it.outputFile
      }
    }
  }

  /// -------------------------------------------------------------------------
  ///
  /// Create the application installation task.
  ///
  /// -------------------------------------------------------------------------

  private
  void createInstallTask(Project project) {
    def app = getAppInfo()
    def dir = getInstallDir()
    def ebin = new File(dir, 'ebin')

    def install = project.tasks.create(
      "install${app.name.capitalize()}Application",
      ApplicationInstall.class)

    def friendly = FileUtils.getAbsolutePath(dir)
    def pdir = FileUtils.getAbsolutePath(getAppInfo().sourceDir)
    if(friendly.startsWith(pdir)) {
      friendly = (friendly - pdir).substring(1)
    }
    install.setDescription("Install application in ${friendly}")
    ///
    /// Copy the .app file
    ///
    install.dependsOn this.appFileTask
    def srcApp = this.appFileTask.outputs.files.singleFile
    install.setInputAppFile(srcApp)
    ///
    /// Copy the .beam files
    ///
    beamTasks.each {
      install.dependsOn it
      def out = it.outputFile
      install.addInputBeams(out)
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
