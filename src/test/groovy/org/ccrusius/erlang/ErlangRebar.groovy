package org.ccrusius.erlang

import org.ccrusius.erlang.PluginTestBase

import static org.gradle.testkit.runner.TaskOutcome.*

class ErlangRebarTest extends PluginTestBase {

  def "podcast-rename"() {
    def baseDir = new File(getResourcesDir(), 'podcast-rename')
    def buildDir = new File("${testBuildDir.parentFile}", 'podcast-rename')

    given:
    setTestProjectDir(baseDir)
    setTestBuildDir(buildDir)

    when:
    showAllGradleTasks()
    def result = runGradleTask('podcast_rename')

    then:
    result.task(':podcast_rename').outcome == SUCCESS

    new File("${baseDir}/ebin/podcast_rename.app").exists()
    new File("${baseDir}/ebin/podcast_rename.beam").exists()
  }
}
