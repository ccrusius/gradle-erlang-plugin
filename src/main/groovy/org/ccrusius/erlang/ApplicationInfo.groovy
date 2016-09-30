package org.ccrusius.erlang

import org.gradle.api.GradleException
import org.gradle.api.Project

import org.ccrusius.erlang.utils.FileUtils

/// ---------------------------------------------------------------------------
///
/// Information about an Erlang application being produced
///
/// ---------------------------------------------------------------------------
class ApplicationInfo {
  private final Project project

  ApplicationInfo(Project project) {
    this.project = project
  }

  /// -------------------------------------------------------------------------
  ///
  /// The application source base directory
  ///
  /// -------------------------------------------------------------------------

  File getSourceDir() {
    sourceDir ? project.file(sourceDir) : project.projectDir
  }

  void setSourceDir(Object dir) {
    this.sourceDir = dir
  }

  private Object sourceDir

  /// -------------------------------------------------------------------------
  ///
  /// The application resource file
  ///
  /// -------------------------------------------------------------------------

  File getResourceFile() {
    if(resourceFile == null) { resourceFile = findResourceFile() }
    if(resourceFile == null) { return null }
    return project.file(resourceFile)
  }

  void setResourceFile(Object file) {
    resourceFile = file
  }

  private Object resourceFile = null

  private File findResourceFile() {
    def dir = new File(getSourceDir(), 'ebin')
    def all = dir.listFiles()
    def candidates = all.findAll { FileUtils.getExtension(it) == '.app' }
    switch(candidates.size()) {
      case { it == 0 }:
        project.logger.info("No .app file in '${dir.absolutePath}'")
        return null
      case { it > 1 }:
        project.logger.info("Too many .app files in '${dir.absolutePath}'")
        return null
    }
    return candidates[0]
  }

  /// -------------------------------------------------------------------------
  ///
  /// The application name
  ///
  /// -------------------------------------------------------------------------

  String getName() {
    if(name) { return name }

    def appFile = FileUtils.getAbsolutePath(getResourceFile())
    def escript = project.extensions.erlang.installation.getEscript()
    def result = escript.eval("""
      {ok,[{application,Name,_}]}=file:consult(\"${appFile}\"),
      io:format("~w",[Name]).
    """)
    return result
  }

  void setName(String name) {
    this.name = name
  }

  private String name = null

  /// -------------------------------------------------------------------------
  ///
  /// The application version
  ///
  /// -------------------------------------------------------------------------

  String getVersion() {
    if(this.version) { return this.version }

    def appFile = FileUtils.getAbsolutePath(getResourceFile())
    def escript = project.extensions.erlang.installation.getEscript()
    def result = escript.eval("""
      {ok,[{application,_,Props}]}=file:consult(\"${appFile}\"),
      Vsn = proplists:get_value(vsn, Props),
      if
        is_atom(Vsn) -> io:format(\"undefined\");
        true -> io:format(\"~s\",[Vsn])
      end.
    """)
    if(result == 'undefined') {
      result = project.version
      if(result == 'unspecified') {
        try { result = project.ext.version }
        catch(all) { }
      }
    }
    return result
  }

  void setVersion(String version) {
    this.version = version
  }

  private String version = null

  /// -------------------------------------------------------------------------
  ///
  /// The application directory name
  ///
  /// -------------------------------------------------------------------------

  String getDirName() {
    dirName ? dirName : "${getName()}-${getVersion()}"
  }

  void setDirName(String dirName) {
    this.dirName = dirName
  }

  private String dirName = null
}
