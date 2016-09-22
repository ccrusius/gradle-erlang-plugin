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
    def dir = new File(project.projectDir, 'ebin')
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
      io:format("~s",[proplists:get_value(vsn, Props)]).
    """)
    if(result == 'undefined') {
      result = project.version
      if(result == 'unspecified') {
        result = project.ext.version
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
    "${getName()}-${getVersion()}"
  }

  /// -------------------------------------------------------------------------
  ///
  /// Write the app file in a normalized version. Fills in values that are
  /// not present.
  ///
  /// -------------------------------------------------------------------------
  void write(File file) {
    String vsn = getVersion()
    String name = getName()
    String appFile = FileUtils.getAbsolutePath(getResourceFile())
    def escript = project.extensions.erlang.installation.getEscript()

    if(file.exists()) { assert file.delete() }
    file.parentFile.mkdirs()

    escript.eval("""
      {ok,[{application,AppName,Props}]}=file:consult(\"${appFile}\"),
      SortedProps = orddict:from_list(Props),
      PropsWithVsn = orddict:merge(fun(K,V1,V2) -> V1 end,
                                   [{vsn, \"${vsn}\"}],
                                   SortedProps),
      ok = file:write_file(\"${FileUtils.getAbsolutePath(file)}\",
                 io_lib:fwrite(\"~p.~n\",
                               [{application,${name},PropsWithVsn}])).
    """)

    assert file.exists()
  }
}
