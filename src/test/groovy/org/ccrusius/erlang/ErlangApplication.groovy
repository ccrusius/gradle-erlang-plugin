package org.ccrusius.erlang

import org.ccrusius.erlang.PluginTestBase

import static org.gradle.testkit.runner.TaskOutcome.*

class ErlangApplicationTest extends PluginTestBase {

  def "hello, world"() {
    def baseDir = new File(getResourcesDir(), 'hello_world_app')
    def buildDir = new File("${testBuildDir.parentFile}", 'hello_world')

    given:
    setTestProjectDir(baseDir)
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
    setTestProjectDir(baseDir)
    setTestBuildDir(buildDir)

    when:
    showAllGradleTasks()
    def result1 = runGradleTask('installApp1Application')
    def result2 = runGradleTask('installApp2Application')
    def result3 = runGradleTask('installApp3Application')

    then:
    result1.task(':installApp1Application').outcome == SUCCESS
    result2.task(':installApp2Application').outcome == SUCCESS
    result3.task(':installApp3Application').outcome == SUCCESS

    new File("${buildDir}/dir1/app1-1.1.1/ebin/app1.app").exists()
    new File("${buildDir}/dir2/app2-1.2.3.4-pre3/ebin/app2.app").exists()
    new File("${buildDir}/dir3/dir3/ebin/app3.app").exists()
  }

  def "erlcount (From 'Learn Yourself Some Erlang for Great Good')" () {
    def baseDir = new File(getResourcesDir(), 'lyse-erlcount')
    def buildDir = new File("${testBuildDir.parentFile}", 'erlcount')

    given:
    setTestProjectDir(baseDir)
    setTestBuildDir(buildDir)

    when:
    def ppool = runGradleTask(':ppool:installPpoolApplication')
    def erlcount = runGradleTask(':erlcount:installErlcountApplication')

    then:
    ppool.task(':ppool:installPpoolApplication').outcome == SUCCESS
    erlcount.task(':erlcount:installErlcountApplication').outcome == SUCCESS
  }
}
