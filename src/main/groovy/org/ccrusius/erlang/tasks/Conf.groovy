package org.ccrusius.erlang.tasks

import org.gradle.api.Task
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

import org.ccrusius.erlang.utils.FileUtils

/// ===========================================================================
///
/// Read an Erlang "configuration file" (containing an Erlang term), perform
/// some replacements, write the result to a destination file.
///
/// ===========================================================================
@ParallelizableTask
class Conf extends DefaultTask {

  /// -------------------------------------------------------------------------
  ///
  /// The source file
  ///
  /// -------------------------------------------------------------------------

  @InputFile
  File getSource() {
    project.file(source)
  }

  void setSource(Object source) {
    this.source = source
  }

  private Object source

  /// -------------------------------------------------------------------------
  ///
  /// The output file
  ///
  /// -------------------------------------------------------------------------

  @OutputFile
  File getOutput() {
    project.file(this.output)
  }

  void setOutput(Object output) {
    this.output = output
  }

  private Object output

  /// -------------------------------------------------------------------------
  ///
  /// The replacement list
  ///
  /// -------------------------------------------------------------------------

  @Input
  List<String> getReplacementFroms() {
    replacements.collect {
      def (atom, repl) = it
      atom
    }
  }

  @Input
  List<String> getReplacementTos() {
    replacements.collect {
      def (atom, repl) = it
      repl
    }
  }

  List<Tuple2> getReplacements() {
    replacements
  }

  void setReplacements(List<Tuple2> replacements) {
    this.replacements.clear()
    this.replacements.addAll(replacements)
  }

  void addReplacement(String atom, String replacement) {
    replacements.add(new Tuple2(atom, replacement))
  }

  private final List<Tuple2> replacements = new ArrayList<Tuple2>()

  /// -------------------------------------------------------------------------
  ///
  /// Copy config file over, with suitable replacements
  ///
  /// -------------------------------------------------------------------------

  @TaskAction
  void build() {

    File script = File.createTempFile("temp",".erl")
    script.deleteOnExit()

    script.write("%% -*- erlang -*-\n")
    getReplacements().each {
      def (atom, repl) = it
      script.append("f($atom) -> $repl;\n")
    }
    script.append("""f([H|T]) -> [f(H)|f(T)];
f(T) when is_tuple(T) -> list_to_tuple(f(tuple_to_list(T)));
f(X) -> X.

main(_) ->\n
{ok,[Conf]} = file:consult(\"${FileUtils.getAbsolutePath(getSource())}\"),
NewConf = f(Conf),
ok = file:write_file(\"${FileUtils.getAbsolutePath(getOutput())}\",
       io_lib:fwrite(\"~p.~n\", [NewConf])).
""")

    def escript = project.extensions.erlang.installation.escript
    escript.run(script)
  }
}
