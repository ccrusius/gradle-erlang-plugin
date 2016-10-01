package org.ccrusius.erlang.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

/// ===========================================================================
///
/// PrecompiledApplication
///
/// Tells the system about an application that was compiled in some
/// non-Gradle way. The application should have an OTP-compatible
/// directory structure.
///
/// task somebody_elses_app(type: PrecompiledApplication) {
///   baseDir 'blah'
/// }
///
/// ===========================================================================

@ParallelizableTask
class PrecompiledApplication extends DefaultTask {

  PrecompiledApplication() {
  }

  /// -------------------------------------------------------------------------
  ///
  /// Build the application
  ///
  /// -------------------------------------------------------------------------

  @TaskAction
  void build() {
    // All the work is done via dependencies that the user must specify.
  }

  /// -------------------------------------------------------------------------
  ///
  /// The base directory. This is the root of the OTP application source tree.
  ///
  /// -------------------------------------------------------------------------

  @InputDirectory
  File getBaseDir() {
    project.file(this.baseDir)
  }

  void setBaseDir(Object baseDir) {
    this.baseDir = baseDir
  }

  private Object baseDir
}
