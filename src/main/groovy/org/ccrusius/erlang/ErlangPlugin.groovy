/** Gradle Erlang plugin.

    Basic structure inspired by the asciidoctor Gradle plugin.
*/

package org.ccrusius.erlang

import org.gradle.api.Plugin
import org.gradle.api.Project

class ErlangPlugin implements Plugin<Project> {
  static final String ERLANG_EXTENSION_NAME = 'erlang'
  static final String ERLANG_BUILD_DIR_NAME = 'ebuildDir'
  static final String ERLANG_BUILD_TASK_NAME = 'ebuild'

  void apply(Project project) {
    project.apply(plugin: 'base')
    project.logger.info("[Erlang] Applying plugin for project ${project.name}")

    configureExtension(project)
    configureProperties(project)
    configureApplication(project)
  }

  private void configureExtension(Project project) {
    def extension = project.extensions.create(
      ERLANG_EXTENSION_NAME,
      ErlangExtension,
      project)

    project.logger.info("[Erlang] ${extension.version}")
    project.logger.info("[Erlang] groovy-dsl: ${extension.groovyDslVersion}")
  }

  private void configureProperties(Project project) {
    project.extensions.add(
      ERLANG_BUILD_DIR_NAME,
      "${project.buildDir}/erlang")
  }

  private void configureApplication(Project project) {
    if(project.extensions.erlang.appFile.appFile) {
      tasks.Application app = project.getTasks().create(
        ERLANG_BUILD_TASK_NAME,
        tasks.Application.class)
      app.setDescription("Compile the main OTP application.")
      app.createSubTasks(project)
    }
  }
}
