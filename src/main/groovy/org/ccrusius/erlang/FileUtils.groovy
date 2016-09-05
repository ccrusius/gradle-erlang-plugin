package org.ccrusius.erlang

import org.gradle.api.GradleException

class FileUtils {

  static String getUnixPath(String path) {
    path.replaceAll("\\\\","/")
  }

  static String getAbsolutePath(File file) {
    getUnixPath(file.absolutePath)
  }

  static String getExtension(File file) {
    def name = file.name
    def idx = name.lastIndexOf('.')
    if(idx > 0) { return name.substring(idx) }
    null
  }

  static String getBaseName(File file) {
    def name = file.name
    def idx = name.lastIndexOf('.')
    if(idx > 0) { return name.substring(0,idx) }
    name
  }

  static String getCompiledExtension(File file) {
    def inp = getExtension(file)
    if(inp == ".erl" || inp == ".S" || inp == ".core") {
      return ".beam"
    }
    if(inp == ".yrl") { return ".erl" }
    if(inp == ".mib") { return ".bin" }
    if(inp == ".bin") { return ".hrl" }
    throw new GradleException('Erlang source file has unsupported extension.')
  }

  static String getCompiledName(File file) {
    getBaseName(file) + getCompiledExtension(file)
  }

}
