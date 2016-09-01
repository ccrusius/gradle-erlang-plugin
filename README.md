# Erlang Plugin for Gradle

This Gradle plugin provides basic Erlang building functionality for
Gradle. It can not replace `rebar` yet (and maybe it will never be
able to), but it is good enough for basic uses. One example is the
[gen_c_server](https://github.com/ccrusius/gen_c_server) project,
which uses this to build both Erlang and C sources, and test the
packages using Erlang's
[Common Test](http://erlang.org/doc/man/ct.html) framework.
