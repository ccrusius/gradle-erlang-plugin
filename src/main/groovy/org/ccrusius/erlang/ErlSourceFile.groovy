package org.ccrusius.erlang

import org.gradle.api.GradleException

/** An Erlang source file.
 *
 * This extends the File class, which arguably should not be done. But
 * it is the most convenient way.
 *
 * @author Cesar Crusius
 */
class ErlSourceFile extends File {

  String getExtension() {
    def name = getName()
    def idx = name.lastIndexOf('.')
    if(idx > 0) { return name.substring(idx) }
    null
  }

  String getBaseName() {
    def name = getName()
    def idx = name.lastIndexOf('.')
    if(idx > 0) { return name.substring(0,idx) }
    name
  }

  String getCompiledExtension() {
    def inp = getExtension()
    if(inp == ".erl" || inp == ".S" || inp == ".core") {
      return ".beam"
    }
    if(inp == ".yrl") { return ".erl" }
    if(inp == ".mib") { return ".bin" }
    if(inp == ".bin") { return ".hrl" }
    throw new GradleException('Erlang source file has unsupported extension.')
  }

  String getCompiledName() {
    getBaseName() + getCompiledExtension()
  }

  ErlSourceFile(File base) {
    super(base.absolutePath)
  }
}
