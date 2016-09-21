package org.ccrusius.erlang.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction
import org.ccrusius.erlang.utils.AppFile
import org.ccrusius.erlang.utils.FileUtils

///
/// Install a pre-compiled Erlang application
///
/// @author Cesar Crusius
///
@ParallelizableTask
class ApplicationInstall extends DefaultTask {

  /// -------------------------------------------------------------------------
  ///
  /// The input .app file
  ///
  /// -------------------------------------------------------------------------

  @InputFile
  File getInputAppFile() {
    project.file(this.inputAppFile)
  }

  void setInputAppFile(Object file) {
    this.inputAppFile = file
  }

  private Object inputAppFile

  /// -------------------------------------------------------------------------
  ///
  /// The input .beam files
  ///
  /// -------------------------------------------------------------------------

  @Optional @InputFiles
  List<File> getInputBeams() {
    return inputBeams
  }

  void addInputBeams(Object... args) {
    addInputBeams(Arrays.asList(args));
  }

  void addInputBeams(List<Object> args) {
    args.each { inputBeams.add(project.file(it)) }
  }

  private final List<File> inputBeams = new ArrayList<File>()

  /// -------------------------------------------------------------------------
  ///
  /// The output base directory
  ///
  /// -------------------------------------------------------------------------

  @Internal
  File getInstallBaseDir() {
    if(this.installBaseDir == null) {
      return new File("${project.buildDir}/install/erlang-lib")
    }
    project.file(this.installBaseDir)
  }

  void setInstallBaseDir(Object dir) {
    this.installBaseDir = dir
  }

  private Object installBaseDir

  /// -------------------------------------------------------------------------
  ///
  /// The output directory
  ///
  /// -------------------------------------------------------------------------

  @OutputDirectory
  File getOutputDirectory() {
    AppFile app = new AppFile(project)
    app.setAppFile(getInputAppFile())
    String dirName = app.getAppDirName()
    return new File(getInstallBaseDir(), dirName)
  }

  /// -------------------------------------------------------------------------
  ///
  /// The task action.
  ///
  /// -------------------------------------------------------------------------
  @TaskAction
  void install() {
    ///
    /// Build directory structure
    ///
    def base = getOutputDirectory()
    def ebin = new File(base, 'ebin')

    ebin.mkdirs()
    ///
    /// Install .app file
    ///
    File appSrc = getInputAppFile()
    project.copy {
      from appSrc
      into ebin
    }
    ///
    /// Install beam files
    ///
    getInputBeams().each {
      File beamSrc = it
      project.copy {
        from beamSrc
        into ebin
      }
    }
  }
}
