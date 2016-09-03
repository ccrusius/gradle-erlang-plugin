[![Build Status](https://travis-ci.org/ccrusius/gradle-erlang-plugin.svg?branch=master)](https://travis-ci.org/ccrusius/gradle-erlang-plugin)

# Erlang Plugin for Gradle

This Gradle plugin provides basic Erlang building functionality for
Gradle. It can not replace `rebar` yet (and maybe it will never be
able to), but it is good enough for basic uses. One example is the
[gen_c_server](https://github.com/ccrusius/gen_c_server) project,
which uses this to build both Erlang and C sources, and test the
packages using Erlang's
[Common Test](http://erlang.org/doc/man/ct.html) framework.

## Basic Use

1. Install this plugin
2. Configure path to Erlang installation
3. Compile and evaluate at will

## Specifying which Erlang to Use

The Erlang installation to be used is given by the `installation`
property of the `erlang` extension. You can switch installations by
calling the `setRoot` method, as for example in
```groovy
erlang.installation.setRoot('/opt/erlang/r16b03-1')
```
The default value for the root will work if you have an Erlang
installation in your `$PATH`.

## Evaluating Erlang Code

You can get the output of an arbitrary Erlang expression evaluation by
using the `eval` function:
```groovy
def erlangVersion = erlang.eval('io:format("~w",[1+1]).')
```
The `erlang.eval` function is a shortcut for
`erlang.installation.getEscript().eval`.

## Compiling Erlang Code

The plugin provides an `Erlc` task with a few parameters, settable via
the following functions:

* `setSourceFile`: Specifies the path to the Erlang source file to be
  compiled.
* `setOutputDir`: Specifies the folder where the compiled file should
  be placed. The name of the file will be the same as that of the
  source file, with the appropriate extension. The default is to place
  it in the same directory as the source file.

Example:
```groovy
task hello_world_beam(type: org.ccrusius.erlang.Erlc) {
  setSourceFile 'src/hello_world.erl'
  setOutputDir 'ebin'
}
```
