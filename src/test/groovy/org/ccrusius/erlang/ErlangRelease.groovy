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
  }
}
