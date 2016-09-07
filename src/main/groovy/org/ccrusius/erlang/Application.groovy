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
    def dir = new File(getBaseDir(), "ebin")
    def all = dir.listFiles()
    def candidates = all.findAll { FileUtils.getExtension(it) == '.app' }
    if(candidates.size() == 0) {
      throw new GradleException("No .app file in '${dir.absolutePath}'")
    }
    if(candidates.size() > 1) {
      throw new GradleException("Too many .app files in '${dir.absolutePath}'")
    }
    return candidates[0]
  }

  @OutputFile
  File getOutAppFile() {
    new File(
      "${getOutputDir()}/ebin",
      "${getAppFile().name}")
  }

  @Internal
  String getAppName() {
    def appFile = FileUtils.getAbsolutePath(getAppFile())
    def escript = project.extensions.erlang.installation.getEscript()
    escript.eval("""
      {ok,[{application,AppName,_}]}=file:consult(\"${appFile}\"),
      io:format("~w",[AppName]).
    """)
  }

  @Internal
  String getAppVsn() {
    def appFile = FileUtils.getAbsolutePath(getAppFile())
    def escript = project.extensions.erlang.installation.getEscript()
    escript.eval("""
      {ok,[{application,_,Props}]}=file:consult(\"${appFile}\"),
      io:format("~s",[proplists:get_value(vsn, Props)]).
    """)
  }

  @InputFiles
  List getSourceFiles() {
    def all = new File(getBaseDir(),"src").listFiles()
    all.findAll { FileUtils.getExtension(it) == '.erl' }
  }

  @OutputDirectory
  File getOutputDir() {
    if(this.outputDir == null) {
      this.outputDir = "${project.ebuildDir}/lib/${getAppName()}-${getAppVsn()}"
    }
    project.file(this.outputDir)
  }

  private Object outputDir

  @OutputFiles
  List getOutputBeams() {
    def dir = getOutputDir()
    getSourceFiles().collect {
      new File(
        "${FileUtils.getAbsolutePath(dir)}/ebin/${FileUtils.getCompiledName(it)}"
      )
    }
  }

  @TaskAction
  void build() {
    logger.info("Building application ${getAppName()}")
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
