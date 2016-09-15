package org.ccrusius.erlang.utils

import org.gradle.api.GradleException

class FileUtils {

  static String getUnixPath(String path) {
    path.replaceAll("\\\\","/")
  }

  static String getAbsolutePath(File file) {
    getUnixPath(file.absolutePath)
  }

  /**
   * Split a file name into basic components.
   *
   * @returns A tuple (name, path, extension).
   */
  static Tuple parse(File file) {
    String name = file.path

    String path = null
    def idx = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'))
    if(idx > 0) {
      path = name.substring(0, idx+1)
      name = name.substring(idx+1)
    }

    String extension = null
    idx = name.lastIndexOf('.')
    if(idx > 0) {
      extension = name.substring(idx)
      name = name.substring(0, idx)
    }

    return new Tuple(name, path, extension)
  }

  static String getExtension(File file) {
    def (_name, _path, ext) = parse(file)
    return ext
  }

  static String getBaseName(File file) {
    def (name, _path, _ext) = parse(file)
    return name
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
