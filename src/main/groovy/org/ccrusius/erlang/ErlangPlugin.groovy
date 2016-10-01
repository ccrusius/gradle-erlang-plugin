/** Gradle Erlang plugin.

    Basic structure inspired by the asciidoctor Gradle plugin.
*/

package org.ccrusius.erlang

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ErlangPlugin implements Plugin<Project> {
  static final String ERLANG_EXTENSION_NAME = 'erlang'

  static final String ERLANG_BUILD_TASK_NAME = 'ebuild'

  void apply(Project project) {
    project.apply(plugin: 'base')
    project.logger.info("[Erlang] Applying plugin for project ${project.name}")

    configureExtension(project)
    configureTasks(project)
  }

  private void configureExtension(Project project) {
    def extension = project.extensions.create(
      ERLANG_EXTENSION_NAME,
      ErlangExtension,
      project)
  }

  private void configureTasks(Project project) {
    def ebuild = project.getTasks().create(
      ERLANG_BUILD_TASK_NAME,
      DefaultTask.class)
    ebuild.setDescription('Build all Erlang applications defined in this project.')
  }
}
