package org.ccrusius.erlang.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.ParallelizableTask

import org.ccrusius.erlang.utils.FileUtils

/**
 * Compile an Erlang source using 'erlc'.
 *
 * Most of the time, one will be compiling an '.erl' file into a
 * '.beam', but this task (should) support anything 'erlc' does.
 *
 * @author Cesar Crusius
 */
@ParallelizableTask
class Compile extends DefaultTask {

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
    if(outputFile == null) {
      outputFile = new File(
        getOutputDir(),
        FileUtils.getCompiledName(getSourceFile()))
    }
    project.file(outputFile)
  }

  private Object outputFile

  @TaskAction
  void compile() {
    project.extensions.erlang.installation.getErlc().run(
      getSourceFile(),
      getOutputDir())
  }
}
