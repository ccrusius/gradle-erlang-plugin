package org.ccrusius.erlang.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
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

  /*
   * What to rename the file to, before compiling.
   * This is advanced usage, intended for renaming modules.
   */
  @Input
  File getNewName() {
    if(newName == null) { return getSourceFile() }
    project.file(newName)
  }

  void setNewName(String newName) {
    this.newName = newName
  }

  private Object newName = null

  @OutputFile
  File getOutputFile() {
    if(outputFile == null) {
      outputFile = new File(
        getOutputDir(),
        FileUtils.getCompiledName(getNewName()))
    }
    project.file(outputFile)
  }

  private Object outputFile

  @Input
  List<String> getArguments() {
    return args
  }

  void setArguments(String... args) {
    setArguments(Arrays.asList(args));
  }

  void setArguments(List<String> args) {
    this.args.clear()
    this.args.addAll(args)
  }

  private final List<String> args = new ArrayList<String>()

  /*
   * The task action.
   */
  @TaskAction
  void compile() {
    def source = getSourceFile()
    def newSource = null
    if(this.newName) {
      newSource = new File(source.parent, getNewName().name)
      assert !newSource.exists()
      newSource << source.text
    }

    try {
      project.extensions.erlang.installation.getErlc()
      .withArguments(args)
      .run(newSource ? newSource : source, getOutputDir())
    }
    finally {
      if(newSource) { newSource.delete() }
    }
  }
}
