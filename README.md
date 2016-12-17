[![Build Status](https://travis-ci.org/ccrusius/gradle-erlang-plugin.svg?branch=master)](https://travis-ci.org/ccrusius/gradle-erlang-plugin)

# Erlang Plugin for Gradle

<!-- markdown-toc start - Don't edit this section. Run M-x markdown-toc-generate-toc again -->
**Table of Contents**

- [Erlang Plugin for Gradle](#erlang-plugin-for-gradle)
- [Basic Use](#basic-use)
- [Specifying which Erlang to Use](#specifying-which-erlang-to-use)
- [Compiling and Installing OTP Applications](#compiling-and-installing-otp-applications)
- [Producing Releases with Reltool](#producing-releases-with-reltool)
- [Using Precompiled Applications](#using-precompiled-applications)
- [Dependencies](#dependencies)
- [Using Rebar to Compile Dependencies](#using-rebar-to-compile-dependencies)
- [Evaluating Erlang Code](#evaluating-erlang-code)

<!-- markdown-toc end -->

This Gradle plugin provides basic Erlang building functionality for
Gradle. It can not replace `rebar` yet (and maybe it will never be
able to), but it is good enough for compiling OTP applications, and
generating `reltool` releases.
One plugin usage example is the
[gen_c_server](https://github.com/ccrusius/gen_c_server) project,
which uses this to build both Erlang and C sources, and test the
packages using Erlang's
[Common Test](http://erlang.org/doc/man/ct.html) framework.

The main reason behind this plugin was the need to incorporate Erlang
into multi-language build environments: while `rebar` does its job
well with Erlang, as soon as you get out of it you start running into
trouble. Native binaries from C/C++, for example, are hard to pull
off. The choices were: I could either wait for `rebar` to add support
for all other languages (Java, C/C++, etc) in all platforms (Windows,
Unix, Mac, etc); or I could add Erlang support to a build system that
already has all the other things taken care of. The second option was
obviously the correct one.

The next decision was to which build system to add Erlang support to,
and there were not many around that can cover all the cases. Google's
Bazel did not have proper Windows support when I looked at it. CMake
supported C well, but everything else was clunky. At the end of the
day, Gradle was the one that ticked most boxes, and the one I went
with.

# Basic Use

1. Install this plugin,
2. Configure path to Erlang installation (if needed),
3. Add your tasks,
4. `gradle ebuild`, or `gradle reltool`, etc.

# Specifying which Erlang to Use

The Erlang installation to be used is pointed to by the `erlangRoot`
project property. The default uses whatever you have
on your `$PATH`, so in most cases you do not need to touch it. Here's
an example of what you would do to override it:

```groovy
plugins {
  id 'org.ccrusius.erlang'
}

ext {
  erlangRoot = '/opt/erlang/r16b03-1'
}
```

# Compiling and Installing OTP Applications

The `Application` task builds an application
based on a source tree complying with the OTP standard. The
application name and version will be obtained from the `.app` file
inside the `ebin` directory. The task properties are as follows:

* `baseDir` (required): Path to the root of the application source
  tree. Inside `baseDir` there should be at least the `ebin`
  directory, with one `.app` file inside. All `.erl` files inside the
  `src` directory will be compiled into their respective `.beam`s.
* `version` (optional): If specified, overrides the version specified
  in the `.app` file. This allows you to set the application version
  from the build script, without having to modify the `.app`
  file. This is especially useful when used in conjunction with other
  plugins, such as the
  [git versioning plugin](https://plugins.gradle.org/plugin/com.zoltu.git-versioning).
* `outDir` (optional): The output directory for the application. Leave
  it alone unless you really need to change it. The default is
  `$buildDir/erlang/lib/$appName-$appVersion`.
* `addCompilerOpts` (optional method): Call it with a list of strings
  to add to the arguments passed in to `erlc` when compiling the
  `.beam` files.

Example `build.gradle`:
```groovy
plugins {
  id 'org.ccrusius.erlang'
}

import org.ccrusius.erlang.tasks.Application

task hello_world(type: Application) {
  version '1.0.0'
  baseDir '.'
  addCompilerOpts('-Werror')
}
```

When the Erlang plugin is applied, it will create an `ebuild` task,
which will depend on all the application tasks for that project. This
means you can `./gradlew ebuild` and have all your applications
compiled.

# Producing Releases with Reltool

The `RelTool` task builds a release
based on a given `reltool` configuration file.
The task properties are as follows:

* `configFile` (required): The `reltool` configuration file for the
  release.
* `version` (optional): If specified, overrides the version specified
  in the configuration file. This allows you to set the release
  version from the build script, without having to modify the configuration
  file. This is especially useful when used in conjunction with other
  plugins, such as the
  [git versioning plugin](https://plugins.gradle.org/plugin/com.zoltu.git-versioning).
* `outDir` (optional): The output directory for the release. Leave
  it alone unless you really need to change it. The default is
  `$buildDir/erlang/rel/$relName-$relVersion`.

Example `build.gradle`:
```groovy
plugins {
  id 'org.ccrusius.erlang'
}

import org.ccrusius.erlang.tasks.Application
import org.ccrusius.erlang.tasks.RelTool

task hello_world(type: Application) {
  version '1.0.0'
  baseDir '.'
}

task release(type: RelTool) {
  dependsOn hello_world
  version '1.0.0'
  configFile 'hello_world.config'
}
```

# Using Precompiled Applications

Precompiled OTP applications (any application that was compiled by a
task other than `Application`) can be declared with the
`PrecompiledApplication` task. This task takes only one parameter,
`baseDir`, which points to the root of the OTP application. One usage
example is
the [spoken-code project](https://github.com/ccrusius/spoken-code),
which compiles dependencies using `rebar`, and then declares them as
`PrecompiledApplication`s.

# Dependencies

Both the `Application` and `RelTool` tasks do the right thing
depending on the tasks they depend on:

* The `Application` task will add the correct include paths (`-pa`
  arguments to the Erlang compiler) to the applications, precompiled
  or not, that the task was declared to `dependOn` (even if
  indirectly).
* The `RelTool` task will add the proper library directories
  (`lib_dir` statement in the `reltool` configuration file) to the
  applications, precompiled or not, that the task was declared to
  `dependOn`.

# Using Rebar to Compile Dependencies

A lot of Erlang applications are distributed with a `rebar` build
configuration. If you need any as a dependency, download them (using
`GrGit` or something similar, see
the [spoken-code project](https://github.com/ccrusius/spoken-code)
for an example), and use the `Rebar` task to compile them:
```groovy
import org.ccrusius.erlang.tasks.Rebar

task rebar(type: Rebar) {
  setVersion '3.1.3' /// The rebar version
  setTarget 'compile'
  setDirectory 'path/to/dependency/source/tree'
  outputs.file('generated/file')
}
```

You must specify one output file for the task: the system does not
know what `rebar` will generate, so you have to tell it explicitly.

# Evaluating Erlang Code

You can get the output of an arbitrary Erlang expression evaluation by
using the `eval` function:
```groovy
def two = erlang.eval('io:format("~w",[1+1]).')
```
The `erlang.eval` function is just a shortcut for
`erlang.installation.escript.eval`.
