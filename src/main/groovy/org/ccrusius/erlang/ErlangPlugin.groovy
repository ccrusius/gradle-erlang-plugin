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
  static final String ERLANG_BUILD_DIR_NAME = 'ebuildDir'
  static final String ERLANG_BUILD_LIB_DIR_NAME = 'ebuildLibDir'
  static final String ERLANG_BUILD_APP_DIR_NAME = 'ebuildAppDir'

  static final String ERLANG_RELTOOL_TASK_NAME = 'reltool'
  static final String ERLANG_RELTOOL_CONFIG_FILE = 'reltoolConfigFile'
  static final String ERLANG_BUILD_REL_DIR_NAME = 'reltoolBuildDir'

  void apply(Project project) {
    project.apply(plugin: 'base')
    project.logger.info("[Erlang] Applying plugin for project ${project.name}")

    configureExtension(project)
    configureProperties(project)
    configureApplication(project)
    configureRelease(project)
  }

  private void configureExtension(Project project) {
    def extension = project.extensions.create(
      ERLANG_EXTENSION_NAME,
      ErlangExtension,
      project)
  }

  private void configureProperties(Project project) {
    def dir = new File(project.buildDir, 'erlang')
    project.extensions.add(ERLANG_BUILD_DIR_NAME, dir)

    def libDir = new File(dir, 'lib')
    project.extensions.add(ERLANG_BUILD_LIB_DIR_NAME, libDir)

    def relDir = new File(dir, 'rel')
    project.extensions.add(ERLANG_BUILD_REL_DIR_NAME, relDir)
  }

  private void configureApplication(Project project) {
    def ext = project.extensions.erlang

    if(ext.appInfo.resourceFile) {
      def fqdn = ext.appInfo.dirName
      def dir = new File(project.extensions.ebuildLibDir, fqdn)
      project.extensions.add(ERLANG_BUILD_APP_DIR_NAME, dir)
    }

    tasks.Application app = project.getTasks().create(
      ERLANG_BUILD_TASK_NAME,
      tasks.Application.class)
    app.createSubTasks(project)

    if(ext.appInfo.resourceFile) {
      app.setDescription("Compile the OTP applications.")
    }
    else {
      app.setDescription("Compile OTP applications defined in sub-projects.")
    }
  }

  private void configureRelease(Project project) {
    def candidates = project.projectDir.listFiles().findAll {
      utils.FileUtils.getExtension(it) == '.config'
    }

    if(candidates.size() == 1) {
      project.extensions.add(ERLANG_RELTOOL_CONFIG_FILE, candidates[0])

      tasks.RelTool reltool = project.getTasks().create(
        ERLANG_RELTOOL_TASK_NAME,
        tasks.RelTool.class)
      reltool.setDescription("Produce a release with 'reltool'.")
      reltool.dependsOn project.tasks.withType(tasks.Application)
    }
  }
}
