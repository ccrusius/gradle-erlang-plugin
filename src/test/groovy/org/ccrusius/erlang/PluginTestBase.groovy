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
    testProjectDir.mkdirs()

    setTestBuildDir(new File(testProjectDir, "build"))

    testCacheDir = new File(testProjectDir, ".gradle")
    testCacheDir.mkdirs()
  }

  void setTestBuildDir(File newDir) {
    testBuildDir = newDir
    if(testBuildDir.exists()) {
      assert testBuildDir.deleteDir()
    }
    testBuildDir.mkdirs()
  }

  File emptyProjectFile(String path) {
    File f = new File(testProjectDir, path)
    f.parentFile.mkdirs()
    if(f.exists()) { f.delete() }
    return f
  }

  File dir(String path) {
    File f = new File(testProjectDir, path)
    f.mkdirs()
    return f
  }

  File emptyBuildFile() {
    return emptyProjectFile('build.gradle')
  }

  GradleRunner getGradle() {
    GradleRunner.create()
    .withProjectDir(testProjectDir)
    .withArguments(
      "--info",
      "--stacktrace",
      "--parallel",
      "-PbuildDir=${utils.FileUtils.getAbsolutePath(testBuildDir)}",
      "--project-cache-dir=${utils.FileUtils.getAbsolutePath(testCacheDir)}")
    .withPluginClasspath()
    .forwardOutput()
  }

  BuildResult runGradleTask(String task) {
    def gradle = getGradle()
    gradle.withArguments(gradle.getArguments() + task).build()
  }

  BuildResult showAllGradleTasks() {
    def gradle = getGradle()
    gradle.withArguments(gradle.getArguments() + ['tasks', '--all']).build()
  }

  File getResourcesDir() {
    new File(getClass().classLoader
             .getResource('README.md')
             .toURI())
    .parentFile
  }
}
