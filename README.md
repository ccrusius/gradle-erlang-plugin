[![Build Status](https://travis-ci.org/ccrusius/gradle-erlang-plugin.svg?branch=master)](https://travis-ci.org/ccrusius/gradle-erlang-plugin)

> **Major refactoring is now taken place towards version 2.0**

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
- [Evaluating Erlang Code](#evaluating-erlang-code)
- [Compiling Erlang Code](#compiling-erlang-code)

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

The main reason behind this plugin is the need to incorporate Erlang
into multi-language build environments: while `rebar` does its job
well with Erlang, as soon as you get out of it you start running into
trouble. Native binaries from C/C++, for example, are hard to pull
off. The choices are: either one waits for `rebar` to add support for
all other languages (Java, C/C++, etc) in all platforms (Windows,
Unix, Mac, etc); or one adds Erlang support to a build system that
already has all the other things taken care of. The second option is
obviously the correct one.

The next decision is to which build system to add Erlang support to,
and there are not many around that can cover all the cases. Google's
Bazel does not have proper Windows support yet. CMake supports C well,
but everything else is clunky. At the end of the day, Gradle is the
one that ticks most boxes, and that's the one I went with.

# Basic Use

1. Install this plugin,
2. Configure path to Erlang installation (if needed),
3. Add your tasks,
4. `gradle ebuild`, or `gradle reltool`, etc.

# Specifying which Erlang to Use

The Erlang installation to be used is given by the `installation`
property of the `erlang` extension. The default uses whatever you have
on your `$PATH`. If you need to specify the path to an installation,
you need to set the `erlangRoot` project property before applying the
plugin. Example:
```groovy
plugins {
  id 'org.ccrusius.erlang'
}

ext {
  erlangRoot = '/opt/erlang/r16b03-1'
}
```

# Compiling and Installing OTP Applications

The `org.ccrusius.erlang.tasks.Application` task builds an application
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

Example `build.gradle`:
```groovy
plugins {
  id 'org.ccrusius.erlang'
}

import org.ccrusius.erlang.tasks.Application

task hello_world(type: Application) {
  version '1.0.0'
  baseDir '.'
}

hello_world.finalize()
```

> **IMPORTANT** After you create your `Application` task, you _must_
> call its `finalize()` method. This will create all the necessary
> subtasks and set up proper dependencies between them. The reason for
> this is that this is the only way I could find to make Gradle create
> sub-tasks for me. The reason why I want to create sub-tasks is to
> get full compilation parallelization by creating one separate task
> for each `erlc` invocation. To see what gets done, look at the
> created tasks with `./gradlew tasks --all`.

When the Erlang plugin is applied, it will create an `ebuild` task,
which will depend on all the application tasks for that project. This
means you can `./gradlew ebuild` and have all your applications
compiled.

# Producing Releases with Reltool

The `org.ccrusius.erlang.tasks.RelTool` task builds a release
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

The `reltool` library directory search path will be determined
automatically from the task's `Application` dependencies.

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

hello_world.finalize()

task release(type: RelTool) {
  version '1.0.0'
  configFile 'hello_world.config'
}

release.dependsOn hello_world
release.finalize()
```

> **IMPORTANT** After you create your `RelTool` task, you _must_
> call its `finalize()` method. This will create all the necessary
> subtasks and set up proper dependencies between them. The reason for
> this is that this is the only way I could find to make Gradle create
> sub-tasks for me.


# Using Precompiled Applications

Precompiled OTP applications (any application that was compiled by a
task other than `Application`) can be declared with the
`PrecompiledApplication` task. This task takes only one parameter,
`baseDir`, which points to the root of the OTP application.

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

# Evaluating Erlang Code

You can get the output of an arbitrary Erlang expression evaluation by
using the `eval` function:
```groovy
def two = erlang.eval('io:format("~w",[1+1]).')
```
The `erlang.eval` function is just a shortcut for
`erlang.installation.escript.eval`.

# Compiling Erlang Code

The plugin provides a `Compile` task with a few parameters, settable via
the following functions:

* `setSourceFile`: Specifies the path to the Erlang source file to be
  compiled.
* `setOutputDir`: Specifies the folder where the compiled file should
  be placed. The name of the file will be the same as that of the
  source file, with the appropriate extension. The default is to place
  it in the same directory as the source file.
* `setArguments`: Specifies extra arguments to pass to the Erlang
  compiler.

Example:
```groovy
task hello_world_beam(type: org.ccrusius.erlang.tasks.Compile) {
  setSourceFile 'src/hello_world.erl'
  setOutputDir 'ebin'
  setArguments '-DMY_MACRO'
}
```

Normally one will not have to use this task, unless Erlang is being
compiled outside of an OTP application structure.
