package org.ccrusius.erlang

import org.ccrusius.erlang.PluginTestBase

import static org.gradle.testkit.runner.TaskOutcome.*

class ErlangReleaseTest extends PluginTestBase {

  def "erlcount (From 'Learn Yourself Some Erlang for Great Good')" () {

    given:
    testProjectDir = new File(getResourcesDir(), 'lyse-erlcount')

    when:
    def result = runGradleTask(':reltool')

    then:
    result.task(':reltool').outcome == SUCCESS

    new File("${testBuildDir}/erlang/rel/erlcount/releases/2.2.2/erlcount.boot").exists()
  }

  def "omnibus" () {
    def baseDir = new File(getResourcesDir(), 'omnibus')
    def buildDir = new File("${testBuildDir.parentFile}", 'omnibus')

    given:
    setTestProjectDir(baseDir)
    setTestBuildDir(buildDir)

    when:
    def result = runGradleTask('sysconfig')

    then:
    result.task(':sysconfig').outcome == SUCCESS
    new File("${testBuildDir}/erlang/rel/omnibus-1.0.0/lib/echoer-1.0.0.ez").exists()
    new File("${testBuildDir}/erlang/rel/omnibus-1.0.0/lib/cranky-1.0.0.ez").exists()
    new File("${testBuildDir}/erlang/rel/omnibus-1.0.0/releases/1.0.0/omnibus.boot").exists()
    new File("${testBuildDir}/erlang/rel/omnibus-1.0.0/releases/1.0.0/sys.config").exists()
  }
}
