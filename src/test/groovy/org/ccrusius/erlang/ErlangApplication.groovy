package org.ccrusius.erlang

import org.ccrusius.erlang.PluginTestBase

import static org.gradle.testkit.runner.TaskOutcome.*

class ErlangApplicationTest extends PluginTestBase {

  def "hello, world"() {
    def ebin = dir('hello_world/ebin')
    def baseDir = new File(getResourcesDir(), 'hello_world_app')

    given:

    testProjectDir = baseDir

    when:

    def result = runGradleTask('compileOtpApplication')

    then:

    result.task(':compileOtpApplication').outcome == SUCCESS
  }
}
