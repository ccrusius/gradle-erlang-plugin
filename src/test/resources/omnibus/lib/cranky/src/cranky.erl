-module(cranky).
-behaviour(echoer).

-export([echo/1]).

echo(Message) ->
    io:format("I have been told to say:~n~s~nBut I don't feel like it.~n",
              [Message]),
    ok.
