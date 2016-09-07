package org.ccrusius.erlang

import org.ccrusius.erlang.PluginTestBase

import static org.gradle.testkit.runner.TaskOutcome.*

class ErlangEvalTest extends PluginTestBase {
  def "erlang eval"() {
    given:
    emptyBuildFile() << """
      plugins {
        id 'org.ccrusius.erlang'
      }

      task eval << {
        println erlang.eval('io:format(\"1+1=~w\",[1+1]).')
      }
    """

    when:
    def result = runGradleTask('eval')

    then:
    result.output.contains('1+1=2')
    result.task(':eval').outcome == SUCCESS
  }
}
