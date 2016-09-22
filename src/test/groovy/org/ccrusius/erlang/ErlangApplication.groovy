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

  def "non-standard"() {
    def baseDir = new File(getResourcesDir(), 'non-standard')
    def buildDir = new File("${testBuildDir.parentFile}", 'non-standard')

    given:
    testProjectDir = baseDir
    setTestBuildDir(buildDir)

    when:
    showAllGradleTasks()
    def result1 = runGradleTask('installCustomizedApplication')
    def result2 = runGradleTask('installCustomized2Application')

    then:
    result1.task(':installCustomizedApplication').outcome == SUCCESS
    result2.task(':installCustomized2Application').outcome == SUCCESS
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
