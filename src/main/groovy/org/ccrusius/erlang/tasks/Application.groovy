package org.ccrusius.erlang.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

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
    if(getAppFile()) {
      createAppFileSubTasks(project)
      createBeamSubTasks(project)
    }
    linkToParent(project)
  }

  @Internal
  private
  File getAppFile() { project.extensions.erlang.appFile.appFile }

  @Internal
  private
  String getAppName() { project.extensions.erlang.appFile.appName }

  @Internal
  private
  File getOutputDir() { project.extensions.ebuildAppDir }

  @Internal
  private
  List getSourceFiles() {
    def all = new File(project.projectDir, "src").listFiles()
    all.findAll { FileUtils.getExtension(it) == '.erl' }
  }

  /**
   * Create the .app file generation task.
   */
  private
  void createAppFileSubTasks(Project project) {
    def dir = new File(getOutputDir(), 'ebin')

    def task = project.getTasks().create(
      getAppFile().name,
      Copy.class)
    task.setDescription("Generate application '.app' file")
    task.from(getAppFile())
    task.into(dir)
    dependsOn task
  }

  /**
   * Create the Erlang compilation tasks.
   *
   * For each .erl file in src/, produce a .beam file in ebin/
   */
  private
  void createBeamSubTasks(Project project) {
    def dir = new File(getOutputDir(), 'ebin')

    getSourceFiles().each {
      def task = project.getTasks().create(
        FileUtils.getCompiledName(it),
        Compile.class)
      task.setDescription("Compile ${it.name}")
      task.setSourceFile(it)
      task.setOutputDir(dir)
      dependsOn task
    }
  }

  private
  void linkToParent(Project project) {
    if(project.parent) {
      project.parent.tasks.withType(Application).collect {
        it.dependsOn this
      }
    }
  }
}
