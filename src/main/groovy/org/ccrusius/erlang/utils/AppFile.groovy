package org.ccrusius.erlang.utils

import org.gradle.api.GradleException
import org.gradle.api.Project

class AppFile {
  final Project project

  private static class Cache {
    File appFile
    String appName
    String appVsn
  }

  private Cache cache

  AppFile(Project project) {
    this.project = project
    this.cache = new Cache()
  }

  /// -------------------------------------------------------------------------
  ///
  /// The source application file
  ///
  /// -------------------------------------------------------------------------
  File getAppFile() {
    if(cache.appFile) return project.file(cache.appFile)

    def dir = new File(project.projectDir, "ebin")
    def all = dir.listFiles()
    def candidates = all.findAll { FileUtils.getExtension(it) == '.app' }
    if(candidates.size() == 0) {
      project.logger.info("No .app file in '${dir.absolutePath}'")
    }
    if(candidates.size() > 1) {
      project.logger.info("Too many .app files in '${dir.absolutePath}'")
    }
    else {
      cache.appFile = candidates[0]
    }
    return cache.appFile
  }

  void setAppFile(Object appFile) {
    cache = new Cache()
    cache.appFile = appFile
  }

  /// -------------------------------------------------------------------------
  ///
  /// The application name, as parsed from the app file
  ///
  /// -------------------------------------------------------------------------
  String getAppName() {
    if(cache.appName) { return cache.appName }
    def appFile = FileUtils.getAbsolutePath(getAppFile())
    def escript = project.extensions.erlang.installation.getEscript()
    cache.appName = escript.eval("""
      {ok,[{application,AppName,_}]}=file:consult(\"${appFile}\"),
      io:format("~w",[AppName]).
    """)
    return cache.appName
  }

  /// -------------------------------------------------------------------------
  ///
  /// The application version, either from the app file or from
  /// project properties.
  ///
  /// -------------------------------------------------------------------------
  String getAppVsn() {
    if(cache.appVsn) { return cache.appVsn }
    def appFile = FileUtils.getAbsolutePath(getAppFile())
    def escript = project.extensions.erlang.installation.getEscript()
    cache.appVsn = escript.eval("""
      {ok,[{application,_,Props}]}=file:consult(\"${appFile}\"),
      io:format("~s",[proplists:get_value(vsn, Props)]).
    """)
    if(cache.appVsn == 'undefined') {
      cache.appVsn = project.version
      if(cache.appVsn == 'unspecified') {
        cache.appVsn = project.ext.version
      }
    }
    return cache.appVsn
  }

  /// -------------------------------------------------------------------------
  ///
  /// The application directory name
  ///
  /// -------------------------------------------------------------------------
  String getAppDirName() {
    "${getAppName()}-${getAppVsn()}"
  }

  /// -------------------------------------------------------------------------
  ///
  /// Write the app file in a normalized version. Fills in values that are
  /// not present.
  ///
  /// -------------------------------------------------------------------------
  void write(File file) {
    String vsn = getAppVsn()
    String appFile = FileUtils.getAbsolutePath(getAppFile())
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
                               [{application,AppName,PropsWithVsn}])).
    """)

    assert file.exists()
  }
}
