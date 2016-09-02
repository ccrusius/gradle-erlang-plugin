package org.ccrusius.erlang

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
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

  @Internal
  String getSourceExtension() {
    def name = getSourceFile().getName()
    def idx = name.lastIndexOf('.')
    if(idx > 0) {
      return name.substring(idx)
    }
    null
  }

  @Internal
  String getSourceBaseName() {
    def name = getSourceFile().getName()
    def idx = name.lastIndexOf('.')
    if(idx > 0) {
      return name.substring(0,idx)
    }
    name
  }

  @Internal
  String getOutputExtension() {
    def inp = getSourceExtension()
    if(inp == ".erl" || inp == ".S" || inp == ".core") {
      return ".beam"
    }
    if(inp == ".yrl") { return ".erl" }
    if(inp == ".mib") { return ".bin" }
    if(inp == ".bin") { return ".hrl" }
    throw new GradleException('Erlang source file has unsupported extension.')
  }

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
      project.file(
        getOutputDir().toString()
        + "/" + getSourceBaseName()
        + getOutputExtension())
    }
    else {
      project.file(this.outputFile)
    }
  }

  private Object outputFile

  @Input
  String getCompiler() {
    if(this.compiler == null) {
      return project.extensions.erlang.erlc
    }
    compiler
  }

  void setCompiler(Object compiler) {
    this.compiler = compiler
  }

  private String compiler

  @TaskAction
  void compile() {
    logger.info('Compiling ' + getSourceFile().getName())
    getOutputDir().mkdirs()
    def command = [
      getCompiler(),
      "-o", getOutputDir().toString() + "/",
      getSourceFile().toString()
    ]
    logger.debug(command.join(' '))
    def process = new ProcessBuilder(command).start()
    process.inputStream.eachLine { println it }
    process.waitFor()
    if(process.exitValue() != 0) {
      throw new GradleException('erlc failed.')
    }
  }
}
