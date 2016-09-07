package org.ccrusius.erlang

import org.ccrusius.erlang.PluginTestBase

import static org.gradle.testkit.runner.TaskOutcome.*

class ErlangApplicationTest extends PluginTestBase {

  def "hello, world"() {
    def baseDir = new File(getResourcesDir(), 'hello_world_app')
    def buildDir = new File("${testBuildDir.parentFile}", 'hello_world')

    given:
    testProjectDir = baseDir
    setTestBuildDir(buildDir)

    when:
    def result = runGradleTask('ebuild')

    then:
    result.task(':ebuild').outcome == SUCCESS
  }

  def "erlcount (From 'Learn Yourself Some Erlang for Great Good')" () {
    def baseDir = new File(getResourcesDir(), 'lyse-erlcount')
    def buildDir = new File("${testBuildDir.parentFile}", 'erlcount')

    given:
    testProjectDir = baseDir
    setTestBuildDir(buildDir)

    when:
    def result = runGradleTask('ebuild')

    then:
    result.task(':ppool:ebuild').outcome == SUCCESS
    result.task(':erlcount:ebuild').outcome == SUCCESS
  }
}
