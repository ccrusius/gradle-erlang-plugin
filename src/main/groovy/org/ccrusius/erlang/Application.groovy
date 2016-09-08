package org.ccrusius.erlang

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

/**
 * @author Cesar Crusius
 */
class Application extends DefaultTask {

  @InputDirectory
  File getBaseDir() {
    if(this.baseDir == null) {
      return project.file("${project.projectDir}")
    }
    return project.file(this.baseDir)
  }

  void setBaseDir(Object dir) {
    this.baseDir = dir
  }

  private Object baseDir

  @InputFile
  File getAppFile() {
    project.extensions.erlang.appFile.appFile
  }

  @OutputFile
  File getOutAppFile() {
    new File(
      "${getOutputDir()}/ebin",
      "${getAppFile().name}")
  }

  @InputFiles
  List getSourceFiles() {
    def all = new File(getBaseDir(),"src").listFiles()
    all.findAll { utils.FileUtils.getExtension(it) == '.erl' }
  }

  @OutputDirectory
  File getOutputDir() {
    if(outputDir == null) {
      def ext = project.extensions.erlang
      outputDir = "${project.ebuildDir}/lib/${ext.appFile.appName}-${ext.appFile.appVsn}"
    }
    project.file(outputDir)
  }

  private Object outputDir

  @OutputFiles
  List getOutputBeams() {
    def dir = getOutputDir()
    getSourceFiles().collect {
      new File(
        "${utils.FileUtils.getAbsolutePath(dir)}/ebin/${utils.FileUtils.getCompiledName(it)}"
      )
    }
  }

  @TaskAction
  void build() {
    logger.info("Building application ${project.extensions.erlang.appFile.appName}")
    //
    // Prepare output directory
    //
    def outputEbin = new File(getOutputDir(), "ebin")
    outputEbin.mkdirs()
    //
    // Compile all source Erlang files
    //
    def erlc = project.extensions.erlang.installation.getErlc()
    getSourceFiles().each { erlc.run(it, outputEbin) }
    //
    // Copy application file
    //
    getOutAppFile() << getAppFile().bytes
  }
}
