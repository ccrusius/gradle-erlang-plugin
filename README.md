[![Build Status](https://travis-ci.org/ccrusius/gradle-erlang-plugin.svg?branch=master)](https://travis-ci.org/ccrusius/gradle-erlang-plugin)

# Erlang Plugin for Gradle

<!-- markdown-toc start - Don't edit this section. Run M-x markdown-toc-generate-toc again -->
**Table of Contents**

- [Erlang Plugin for Gradle](#erlang-plugin-for-gradle)
- [Basic Use](#basic-use)
- [Specifying which Erlang to Use](#specifying-which-erlang-to-use)
- [Compiling OTP Applications](#compiling-otp-applications)
- [Producing Releases with Reltool](#producing-releases-with-reltool)
- [Evaluating Erlang Code](#evaluating-erlang-code)
- [Compiling Erlang Code](#compiling-erlang-code)

<!-- markdown-toc end -->

This Gradle plugin provides basic Erlang building functionality for
Gradle. It can not replace `rebar` yet (and maybe it will never be
able to), but it is good enough for compiling OTP applications, and
generating `reltool` releases. One example is the
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
Bazel does not have proper Windows support. CMake supports C well,
but everything else is clunky. At the end of the day, Gradle is the
one that ticks most boxes, and that's the one I went with.

# Basic Use

1. Install this plugin,
2. Configure path to Erlang installation (if needed),
3. `gradle ebuild`, or `gradle reltool`, etc.

# Specifying which Erlang to Use

The Erlang installation to be used is given by the `installation`
property of the `erlang` extension. You can switch installations by
calling the `setRoot` method, as for example in
```groovy
erlang.installation.setRoot('/opt/erlang/r16b03-1')
```
The default value for the root will work if you have an Erlang
installation in your `$PATH`.

# Compiling OTP Applications

When the plugin is applied, it will create an `ebuild` task that
builds the OTP application in the current directory and the ones found
in sub-projects. The Erlang plugin will consider that an application
needs to be built when it finds an `.app` file in
`$projectDir/ebin`. It will compile the application based on
the sources in `$projectDir/src`. Individual tasks for the application's `.beam`
files will also be created.

# Producing Releases with Reltool

If there is a reltool `.config` file in the project root directory,
the plugin will create a `reltool` task that will generate a release
based on that configuration file.

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
