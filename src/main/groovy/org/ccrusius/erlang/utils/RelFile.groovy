package org.ccrusius.erlang.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.ccrusius.erlang.ErlangInstallation

class RelFile {

  private final Project project

  private final File file

  private ErlangInstallation erlang

  RelFile(Project project, Object file) {
    this.project = project
    this.file = project.file(file)
    this.erlang = new ErlangInstallation(project)
  }

  /// -------------------------------------------------------------------------
  ///
  /// The release name
  ///
  /// -------------------------------------------------------------------------
  String getRelName() {
    if(!this.relName) {
      this.relName = erlang.getEscript().eval("""
      {ok,[{sys,Props}]} = file:consult(\"${FileUtils.getAbsolutePath(file)}\"),
      {rel, Name, _, _} = proplists:lookup(rel, Props),
      io:format(\"~s\",[Name]).
    """)
    }
    return this.relName
  }

  private String relName

  /// -------------------------------------------------------------------------
  ///
  /// The release version
  ///
  /// -------------------------------------------------------------------------
  String getRelVersion() {
    if(!this.relVersion) {
      def escript = project.extensions.erlang.installation.getEscript()
      this.relVersion = erlang.getEscript().eval("""
      {ok,[{sys,Props}]} = file:consult(\"${FileUtils.getAbsolutePath(file)}\"),
      {rel, _, Vsn, _} = proplists:lookup(rel, Props),
      io:format(\"~s\",[Vsn]).
    """)
    }
    return this.relVersion
  }

  private String relVersion
}
