package org.ccrusius.erlang

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 *
 * @author Cesar Crusius
 */
class EscriptExe {

  private final Project project

  private final String exe

  EscriptExe(Project project, String exe) {
    this.project = project
    this.exe = exe
  }

  String unixPath(Object path) {
    project.file(path).absolutePath.replaceAll("\\\\","/")
  }

  String run(File script) {
    def cmdline = [ exe, script.absolutePath ]
    project.logger.debug("EscriptExe.run(${cmdline.join(' ')})")

    def process = new ProcessBuilder(cmdline).start()
    process.waitFor()
    if(process.exitValue() != 0) {
      throw new GradleException('escript failed.')
    }
    def result = process.text.toString().trim()
    project.logger.debug("EscriptExe.run -> ${result}")
    return result
  }

  String eval(String command, List erlArgs = []) {
    final String args = erlArgs.size() > 0 ? "%%! ${erlArgs.join(' ')}\n" : ""
    final String contents = "%% -*- erlang -*-\n${args}main(_) ->\n${command}"

    File script = File.createTempFile("temp",".erl")
    script.deleteOnExit()
    script.write(contents)

    project.logger.debug("EscriptExe.eval:\n${contents}")

    return run(script)
  }

}
