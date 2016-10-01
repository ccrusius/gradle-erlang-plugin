package org.ccrusius.erlang.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.ccrusius.erlang.ErlangInstallation

class AppFile {

  private final Project project

  private final File file

  private ErlangInstallation erlang

  AppFile(Project project, Object file) {
    this.project = project
    this.file = project.file(file)
    this.erlang = new ErlangInstallation(project)
  }

  /// -------------------------------------------------------------------------
  /// -------------------------------------------------------------------------
  String getAppName() {
    if(!this.appName) {
      this.appName = erlang.getEscript().eval("""
      {ok,[{application,Name,_}]}=file:consult(\"${FileUtils.getAbsolutePath(file)}\"),
      io:format(\"~s\",[Name]).
    """)
    }
    return this.appName
  }

  private String appName

  /// -------------------------------------------------------------------------
  /// -------------------------------------------------------------------------
  String getAppVersion() {
    if(!this.appVersion) {
      def escript = project.extensions.erlang.installation.getEscript()
      this.appVersion = erlang.getEscript().eval("""
      {ok,[{application,_,Props}]}=file:consult(\"${FileUtils.getAbsolutePath(file)}\"),
      Vsn = proplists:get_value(vsn, Props),
      io:format(\"~s\",[Vsn]).
    """)
    }
    return this.appVersion
  }

  private String appVersion
}
