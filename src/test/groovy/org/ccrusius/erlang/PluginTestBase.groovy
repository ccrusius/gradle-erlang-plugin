package org.ccrusius.erlang

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification

class PluginTestBase extends Specification {
  Properties props = new Properties()

  File testProjectDir

  File testBuildDir

  File testCacheDir

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

    testBuildDir = new File(testProjectDir, "build")
    testBuildDir.mkdirs()

    testCacheDir = new File(testProjectDir, ".gradle")
    testCacheDir.mkdirs()
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
    .withArguments(
      "--info",
      "-PbuildDir=${unixPath(testBuildDir)}",
      "--project-cache-dir=${unixPath(testCacheDir)}")
    .withPluginClasspath()
    .forwardOutput()
  }

  BuildResult runGradleTask(String task) {
    def gradle = getGradle()
    gradle.withArguments(gradle.getArguments() + task).build()
  }

  File getResourcesDir() {
    new File(getClass().classLoader
             .getResource('README.md')
             .toURI())
    .parentFile
  }

}
