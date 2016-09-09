package org.ccrusius.erlang.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

import org.ccrusius.erlang.utils.FileUtils

/**
 * @author Cesar Crusius
 */
@ParallelizableTask
class RelTool extends DefaultTask {

  @TaskAction
  void build() {
    def escript = project.extensions.erlang.installation.escript
    def dir = getOutputDir()
    if(dir.exists()) { assert dir.deleteDir() }
    dir.mkdirs()

    escript.eval("""
io:format(\"reltool: Reading reltool configuration file~n\"),
{ok,[{sys, Props}]} = file:consult(\"${FileUtils.getAbsolutePath(getConfigFile())}\"),

io:format(\"reltool: Massaging reltool configuration~n\"),
Conf = {sys, [ { lib_dirs, [ \"${FileUtils.getAbsolutePath(project.ebuildLibDir)}\" ] } | Props ] },

io:format(\"reltool: Processing reltool configuration~n\"),
{ok, Spec} = reltool:get_target_spec([Conf]),

io:format(\"reltool: Producing release~n\"),
reltool:eval_target_spec(
    Spec,
    code:root_dir(),
    \"${FileUtils.getAbsolutePath(getOutputDir())}\"),

io:format(\"reltool: Done~n\").
""")
  }

  @InputFile
  File getConfigFile() { project.extensions.reltoolConfigFile }

  @OutputDirectory
  File getOutputDir() { project.extensions.reltoolBuildDir }
}
