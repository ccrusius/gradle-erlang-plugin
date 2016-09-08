package org.ccrusius.erlang

import org.gradle.api.GradleException
import org.gradle.api.Project

/** An Erlang installation tree.
 *
 * @author Cesar Crusius
 */
class ErlangInstallation {

  private final Project project

  ErlangInstallation(Project project) {
    this.project = project
  }

  File getRoot() {
    if(this.root == null) { return null }
    project.file(root)
  }

  void setRoot(Object r) {
    this.root = r
  }

  private Object root

  File getErts() {
    if(this.erts) return this.erts
    def r = getRoot()
    if(r == null) { return null }
    def candidates = r.listFiles().findAll { it.name.startsWith('erts-') }
    if(candidates.size() == 0) { return null }
    if(candidates.size() > 1) {
      throw new GradleException('More than one ERTS in the Erlang root!')
    }
    this.erts = candidates[0]
    return this.erts
  }

  private Object erts

  ErlcExe getErlc() {
    def e = getErts()
    return new ErlcExe(project, e == null ? "erlc" : utils.FileUtils.getUnixPath("$e/bin/erlc"))
  }

  EscriptExe getEscript() {
    def e = getErts()
    return new EscriptExe(project, e == null ? "escript" : utils.FileUtils.getUnixPath("$e/bin/escript"))
  }

}
