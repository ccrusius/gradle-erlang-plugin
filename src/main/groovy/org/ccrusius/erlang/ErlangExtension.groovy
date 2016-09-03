package org.ccrusius.erlang

import org.gradle.api.GradleException
import org.gradle.api.Project

class ErlangExtension {
  String version = "1.0"

  String groovyDslVersion = "1.0.0.preview2"

  final Project project

  ErlangInstallation installation

  ErlangExtension(Project project) {
    this.project = project
    this.installation = new ErlangInstallation(project)
  }

  String eval(String command, List erlArgs = []) {
    return installation.getEscript().eval(command, erlArgs)
  }
}
