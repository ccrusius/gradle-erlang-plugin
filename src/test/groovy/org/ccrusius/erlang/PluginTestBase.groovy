package org.ccrusius.erlang

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification
import spock.lang.Shared

class PluginTestBase extends Specification {
  @Shared Properties props = new Properties()

  @Shared File defaultTestProjectDir

  File testProjectDir = null

  File testBuildDir = null

  File testCacheDir = null

  /// -------------------------------------------------------------------------
  ///
  /// Run before first test ("feature method")
  ///
  /// -------------------------------------------------------------------------
  def setupSpec() {
    props.load(
      getClass().classLoader
      .getResourceAsStream('org.ccrusius.erlang.test.properties'))

    def baseDir = new File(
      props.getProperty('gradleBuildDir'),
      'test-projects')

    defaultTestProjectDir = new File(baseDir, getClass().simpleName)
    if(defaultTestProjectDir.exists()) {
      println "Removing existing default test project directory $defaultTestProjectDir"
      assert defaultTestProjectDir.deleteDir()
    }
  }

  /// -------------------------------------------------------------------------
  ///
  /// Run before every test ("feature method")
  ///
  /// -------------------------------------------------------------------------
  def setup () {
    testProjectDir = new File(defaultTestProjectDir.absolutePath)

    setTestBuildDir(new File(testProjectDir, "build"))

    testCacheDir = new File(testProjectDir, ".gradle")
    testCacheDir.mkdirs()
  }

  /// -------------------------------------------------------------------------
  ///
  /// The test project directory.
  /// This is where 'gradle' is going to be executed from
  ///
  /// -------------------------------------------------------------------------

  void setTestProjectDir(File newDir) {
    testProjectDir = newDir
  }

  /// -------------------------------------------------------------------------
  ///
  /// The build directory
  ///
  /// -------------------------------------------------------------------------

  void setTestBuildDir(File newDir) {
    if(testBuildDir && testBuildDir.exists()) {
      println "Removing old test build directory $testBuildDir"
      testBuildDir.deleteDir()
    }
    testBuildDir = newDir
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
