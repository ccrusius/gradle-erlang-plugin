package org.ccrusius.erlang

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

/**
 * @author Cesar Crusius
 */
class Application extends DefaultTask {

  @InputDirectory
  File getBaseDir() {
    if(this.baseDir == null) {
      return project.file(".")
    }
    return project.file(this.baseDir)
  }

  void setBaseDir(Object dir) {
    this.baseDir = dir
  }

  private Object baseDir

  @InputFile
  File getAppFile() {
    def dir = new File(getBaseDir(), "ebin")
    def all = dir.listFiles().collect { new ErlSourceFile(it) }
    def candidates = all.findAll { it.getExtension() == '.app' }
    if(candidates.size() == 0) {
      throw new GradleException("No .app file in '${dir.absolutePath}'")
    }
    if(candidates.size() > 1) {
      throw new GradleException("Too many .app files in '${dir.absolutePath}'")
    }
    return candidates[0]
  }

  @Internal
  String getAppName() {
    def appFile = getAppFile()
    def escript = project.extensions.erlang.installation.getEscript()
    escript.eval("""
      {ok,[{application,AppName,_}]}=file:consult("${appFile.absolutePath}"),
      io:format("~w",[AppName]).
    """)
  }

  @InputFiles
  List getSourceFiles() {
    def dir = new File(getBaseDir(),"src")
    def all = dir.listFiles().collect { new ErlSourceFile(it) }
    all.findAll { it.getExtension() == '.erl' }
  }

  @OutputDirectory
  File getOutputDir() {
    if(this.outputDir == null) {
      this.outputDir = new File(
        "${project.buildDir.absolutePath}/erlang/lib",
        getAppName())
    }
    project.file(this.outputDir)
  }

  private Object outputDir

  @OutputFiles
  List getOutputBeams() {
    def dir = getOutputDir()
    getSourceFiles().collect {
      new File(
        "${dir.absolutePath}/ebin",
        it.getCompiledName())
    }
  }

  @TaskAction
  void build() {
    logger.info("Building application ${getAppName()}")
    def outputEbin = new File(getOutputDir(), "ebin")
    outputEbin.mkdirs()

    def erlc = project.extensions.erlang.installation.getErlc()
    getSourceFiles().each { erlc.run(it, outputEbin) }
  }

}
