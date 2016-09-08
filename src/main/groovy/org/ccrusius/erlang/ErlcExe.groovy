package org.ccrusius.erlang

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 *
 * @author Cesar Crusius
 */
class ErlcExe {

  private final Project project

  private final String exe

  ErlcExe(Project project, String exe) {
    this.project = project
    this.exe = exe
  }

  void run(File source, File outDir) {
    project.logger.info('Compiling ' + source.name)

    if(outDir == null) { outDir = source.parentFile }

    outDir.mkdirs()

    def command = [
      exe,
      "-o", utils.FileUtils.getUnixPath(outDir.toString() + "/"),
      source.toString()
    ]

    project.logger.debug("ErlcExe: ${command.join(' ')}")
    def process = new ProcessBuilder(command).start()
    process.inputStream.eachLine { println it }
    process.waitFor()
    if(process.exitValue() != 0) {
      throw new GradleException('erlc failed.')
    }
  }

}
