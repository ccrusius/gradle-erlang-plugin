package org.ccrusius.erlang

import org.gradle.api.GradleException
import org.gradle.api.Project

class ErlangExtension {

  final Project project

  ErlangInstallation installation

  utils.AppFile appFile

  ErlangExtension(Project project) {
    this.project = project
    this.installation = new ErlangInstallation(project)
    this.appFile = new utils.AppFile(project)
  }

  String eval(String command, List erlArgs = []) {
    return installation.getEscript().eval(command, erlArgs)
  }
}
