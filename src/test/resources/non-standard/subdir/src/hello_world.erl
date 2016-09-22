-module(hello_world).

-export([nag/0]).

nag() ->
    io:format("hello, world!~n").
