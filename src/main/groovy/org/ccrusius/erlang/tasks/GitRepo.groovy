package org.ccrusius.erlang.tasks

import org.gradle.api.Task
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.ResetOp

import org.ccrusius.erlang.utils.FileUtils

/// ===========================================================================
///
/// Get sources from a Git repository
///
/// ===========================================================================
@ParallelizableTask
class GitRepo extends DefaultTask {

  GitRepo() {
  }

  /// -------------------------------------------------------------------------
  ///
  /// The git repository
  ///
  /// -------------------------------------------------------------------------

  @Input
  String getGitRepo() {
    this.gitRepo
  }

  void setGitRepo(String gitRepo) {
    this.gitRepo = gitRepo
  }

  String gitRepo = null

  /// -------------------------------------------------------------------------
  ///
  /// The git commit, in its various disguises
  ///
  /// -------------------------------------------------------------------------

  @Internal
  void setGitTag(String tag) {
    this.gitRef = new Tuple2('tags', tag)
  }

  @Input
  String getGitRefType() {
    def (type, val) = this.gitRef
    return type
  }

  @Input
  String getGitRefValue() {
    def (type, val) = this.gitRef
    return val
  }

  Tuple2 gitRef

  /// -------------------------------------------------------------------------
  ///
  /// The directory where to check things out at
  ///
  /// -------------------------------------------------------------------------

  @Input
  File getDirectory() {
    if(this.directory) {
      return project.file(this.directory)
    }
    def name = getGitProjectName()
    def ref = getGitRefValue()
    return project.file("${project.buildDir}/erlang/ext/${name}-${ref}")
  }

  void setDirectory(Object directory) {
    this.directory = directory
  }

  private Object directory = null

  /// -------------------------------------------------------------------------
  ///
  /// The file indicating whether or not we need to fetch something
  ///
  /// -------------------------------------------------------------------------
  @OutputFile
  File getGitRefFile() {
    def dir = getDirectory()
    def type = getGitRefType()
    def ref = getGitRefValue()

    if(type == 'tags') {
      return new File("${dir}/.git/refs/tags/${ref}")
    }
  }

  /// -------------------------------------------------------------------------
  /// -------------------------------------------------------------------------
  @Internal
  String getGitProjectName() {
    if(!this.gitRepo) { return null }
    FileUtils.getBaseName(new File(this.gitRepo))
  }

  /// -------------------------------------------------------------------------
  ///
  /// Task action: fetch it, update it, etc
  ///
  /// -------------------------------------------------------------------------

  @TaskAction
  void build() {
    def repo = getGitRepo()
    def dir = getDirectory()
    def ref = getGitRefValue()

    ///
    /// No, I don't like deleting the directory, but
    /// Gradle's @OutputFile creates parent directories, which causes
    /// the git clone to fail. I try to remove it only if it is
    /// Gradle's fault.
    ///
    def head = new File("${dir}/.git/HEAD")
    if(dir.exists() && !head.exists()) {
      assert dir.deleteDir()
    }
    def git = dir.exists() ?
      Grgit.init(dir: dir) :
      Grgit.clone(dir: dir, uri: repo)
    git.fetch()
    git.reset(commit: ref, mode: ResetOp.Mode.HARD)
  }
}
