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
    def result = runGradleTask('installHello_worldApplication')

    then:
    result.task(':installHello_worldApplication').outcome == SUCCESS
  }

  def "non-standard"() {
    def baseDir = new File(getResourcesDir(), 'non-standard')
    def buildDir = new File("${testBuildDir.parentFile}", 'non-standard')

    given:
    testProjectDir = baseDir
    setTestBuildDir(buildDir)

    when:
    def result = runGradleTask('installCustomizedApplication')

    then:
    result.task(':installCustomizedApplication').outcome == SUCCESS
  }

  def "erlcount (From 'Learn Yourself Some Erlang for Great Good')" () {
    def baseDir = new File(getResourcesDir(), 'lyse-erlcount')
    def buildDir = new File("${testBuildDir.parentFile}", 'erlcount')

    given:
    testProjectDir = baseDir
    setTestBuildDir(buildDir)

    when:
    def ppool = runGradleTask(':ppool:installPpoolApplication')
    def erlcount = runGradleTask(':erlcount:installErlcountApplication')

    then:
    ppool.task(':ppool:installPpoolApplication').outcome == SUCCESS
    erlcount.task(':erlcount:installErlcountApplication').outcome == SUCCESS
  }
}
