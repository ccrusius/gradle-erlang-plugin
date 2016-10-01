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
    showAllGradleTasks()
    def result = runGradleTask('ebuild')

    then:
    result.task(':ebuild').outcome == SUCCESS
    new File("${buildDir}/erlang/lib/hello_world-1.0.0/ebin/hello_world.app").exists()
  }

  def "non-standard"() {
    def baseDir = new File(getResourcesDir(), 'non-standard')
    def buildDir = new File("${testBuildDir.parentFile}", 'non-standard')

    given:
    setTestProjectDir(baseDir)
    setTestBuildDir(buildDir)

    when:
    showAllGradleTasks()
    def result = runGradleTask('ebuild')

    then:
    result.task(':ebuild').outcome == SUCCESS

    new File("${buildDir}/dir1-1.1.1/ebin/hello_world.app").exists()
    new File("${buildDir}/dir2-2.2.2/ebin/hello_world.app").exists()
    new File("${buildDir}/erlang/lib/hello_world-3.3.3/ebin/hello_world.app").exists()
  }

  def "erlcount (From 'Learn Yourself Some Erlang for Great Good')" () {
    def baseDir = new File(getResourcesDir(), 'lyse-erlcount')
    def buildDir = new File("${testBuildDir.parentFile}", 'erlcount')

    given:
    setTestProjectDir(baseDir)
    setTestBuildDir(buildDir)

    when:
    def result = runGradleTask('ebuild')

    then:
    result.task(':ebuild').outcome == SUCCESS
    new File("${buildDir}/erlang/lib/ppool-1.0.0/ebin/ppool.app").exists()
    new File("${buildDir}/erlang/lib/erlcount-1.0.0/ebin/erlcount.app").exists()
  }
}
