package org.ccrusius.erlang

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class PluginTestBase extends Specification {
  @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

  def setup () {
  }

  File file(String path) {
    File f = new File(testProjectDir.root, path)
    if(!f.exists()) {
      f.parentFile.mkdirs()
      return testProjectDir.newFile(path)
    }
    return f
  }

  File dir(String path) {
    File f = new File(testProjectDir.root, path)
    if(!f.exists()) {
      return testProjectDir.newFolder(path.split('/'))
    }
    return f
  }

  String unixPath(File file) {
    return file.absolutePath.replaceAll("\\\\","/")
  }

  File getBuildFile() {
    return file('build.gradle')
  }

  GradleRunner getGradle() {
    GradleRunner.create()
    .withProjectDir(testProjectDir.root)
    .withPluginClasspath()
  }

  BuildResult runGradleTask(String task) {
    getGradle().withArguments(task).build()
  }

  File getResourcesDir() {
    new File(getClass().classLoader
             .getResource('README.md')
             .toURI())
    .parentFile
  }

}
