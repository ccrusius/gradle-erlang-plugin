[![Build Status](https://travis-ci.org/ccrusius/gradle-erlang-plugin.svg?branch=master)](https://travis-ci.org/ccrusius/gradle-erlang-plugin)

# Erlang Plugin for Gradle

This Gradle plugin provides basic Erlang building functionality for
Gradle. It can not replace `rebar` yet (and maybe it will never be
able to), but it is good enough for basic uses. One example is the
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
the obvious one.

The next decision is to which build system to add Erlang support to,
and there are not many around that can cover all the cases. Google's
Bazel does not have proper Windows support. CMake supports C well,
but everything else is clunky. At the end of the day, Gradle is the
one that ticks most boxes, and that's the one I went with.

## Basic Use

1. Install this plugin
2. Configure path to Erlang installation
3. `gradle ebuild`, or `gradle reltool`, etc.

## Specifying which Erlang to Use

The Erlang installation to be used is given by the `installation`
property of the `erlang` extension. You can switch installations by
calling the `setRoot` method, as for example in
```groovy
erlang.installation.setRoot('/opt/erlang/r16b03-1')
```
The default value for the root will work if you have an Erlang
installation in your `$PATH`.

## Compiling OTP Applications

When the plugin is applied, it will create an `ebuild` task if there
is a `.app` file in the `/ebin` directory. This task will compile an
OTP application based on the sources in `/src`. Individual task for
the application's `.beam` files will also be created.

## Producing Releases with Reltool

If there is a `.config` file in the project root directory, the plugin
will generate a `reltool` task that will generate a release according
to the configuration file. You have to declare which applications
are to be built for the release. This is a `build.gradle` example from
one of the tests:
```groovy
plugins {
  id 'org.ccrusius.erlang'
}

subprojects {
  apply plugin: 'org.ccrusius.erlang'
}

reltool.dependsOn ':erlcount:ebuild'
reltool.dependsOn ':ppool:ebuild'
```

## Evaluating Erlang Code

You can get the output of an arbitrary Erlang expression evaluation by
using the `eval` function:
```groovy
def two = erlang.eval('io:format("~w",[1+1]).')
```
(The `erlang.eval` function is a shortcut for
`erlang.installation.getEscript().eval`.)

## Compiling Erlang Code

The plugin provides an `Compile` task with a few parameters, settable via
the following functions:

* `setSourceFile`: Specifies the path to the Erlang source file to be
  compiled.
* `setOutputDir`: Specifies the folder where the compiled file should
  be placed. The name of the file will be the same as that of the
  source file, with the appropriate extension. The default is to place
  it in the same directory as the source file.

Example:
```groovy
task hello_world_beam(type: org.ccrusius.erlang.tasks.Compile) {
  setSourceFile 'src/hello_world.erl'
  setOutputDir 'ebin'
}
```
