-module(echoer).

-callback echo(Message :: term()) ->
    ok |
    {error, Reason :: term()}.
