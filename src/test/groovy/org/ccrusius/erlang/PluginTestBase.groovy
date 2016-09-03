package org.ccrusius.erlang

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification

class PluginTestBase extends Specification {
  Properties props = new Properties()

  File testProjectDir

  def setup () {
    props.load(
      getClass().classLoader
      .getResourceAsStream('org.ccrusius.erlang.test.properties'))

    def baseDir = new File(
      props.getProperty('gradleBuildDir'),
      'test-projects')
    baseDir.mkdirs()

    testProjectDir = new File(baseDir, getClass().simpleName)
    if(testProjectDir.exists()) {
      assert testProjectDir.deleteDir()
    }
    testProjectDir.mkdirs()
  }

  File file(String path) {
    File f = new File(testProjectDir, path)
    f.parentFile.mkdirs()
    return f
  }

  File dir(String path) {
    File f = new File(testProjectDir, path)
    f.mkdirs()
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
    .withProjectDir(testProjectDir)
    .withPluginClasspath()
    .forwardOutput()
  }

  BuildResult runGradleTask(String task) {
    getGradle().withArguments("--info", task).build()
  }

  File getResourcesDir() {
    new File(getClass().classLoader
             .getResource('README.md')
             .toURI())
    .parentFile
  }

}
