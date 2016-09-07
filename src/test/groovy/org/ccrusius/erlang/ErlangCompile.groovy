package org.ccrusius.erlang

import org.ccrusius.erlang.PluginTestBase

import static org.gradle.testkit.runner.TaskOutcome.*

class ErlangCompileTest extends PluginTestBase {

  def "hello, world"() {
    def ebin = dir('hello_world/ebin')
    def erl = emptyProjectFile('hello_world/src/hello_world.erl')

    given:

    erl << """
      -module(hello_world).
      -export([hello_world/0]).
      hello_world() -> io:format(\"hello, world!~n\").
    """

    emptyBuildFile() << """
      plugins {
        id 'org.ccrusius.erlang'
      }

      task erlc(type: org.ccrusius.erlang.Erlc) {
        setSourceFile '${FileUtils.getAbsolutePath(erl)}'
        setOutputDir '${FileUtils.getAbsolutePath(ebin)}'
      }

      task run << {
        println erlang.eval(
          'hello_world:hello_world().',
          [ '-pa', '${FileUtils.getAbsolutePath(ebin)}' ])
      }
      run.dependsOn erlc
    """

    when:

    def result = runGradleTask('run')

    then:

    result.task(':erlc').outcome == SUCCESS
    new File(ebin.absolutePath, 'hello_world.beam').exists()
    result.task(':run').outcome == SUCCESS
    result.output.contains("hello, world!")
  }
}
