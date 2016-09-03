/** Gradle Erlang plugin.

    Basic structure inspired by the asciidoctor Gradle plugin.
*/

package org.ccrusius.erlang

import org.gradle.api.Plugin
import org.gradle.api.Project

class ErlangPlugin implements Plugin<Project> {
  static final String ERLANG = 'erlang'

  void apply(Project project) {
    project.apply(plugin: 'base')

    ErlangExtension extension = project.extensions.create(
      ERLANG,
      ErlangExtension,
      project)

    project.logger.info("[Erlang] ${extension.version}")
    project.logger.info("[Erlang] groovy-dsl: ${extension.groovyDslVersion}")
  }
}
