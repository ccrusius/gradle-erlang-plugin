package org.ccrusius.erlang

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * @author Cesar Crusius
 */
class Erlc extends DefaultTask {

  @InputFile
  File getSourceFile() {
    project.file(this.source)
  }

  void setSourceFile(Object source) {
    this.source = source
  }

  private Object source

  @OutputDirectory
  File getOutputDir() {
    if(this.outputDir == null) { return getSourceFile().getParentFile() }
    project.file(this.outputDir)
  }

  void setOutputDir(Object dir) {
    this.outputDir = dir
  }

  private Object outputDir

  @OutputFile
  File getOutputFile() {
    if(this.outputFile == null) {
      this.outputFile = project.file(
        getOutputDir().toString()
        + "/" + utils.FileUtils.getCompiledName(getSourceFile()))
    }
    project.file(this.outputFile)
  }

  private Object outputFile

  @TaskAction
  void compile() {
    project.extensions.erlang.installation.getErlc().run(
      getSourceFile(),
      getOutputDir())
  }
}
