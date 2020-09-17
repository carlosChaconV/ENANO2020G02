:- use_module(library(http/websocket)).
:- use_module(library(http/thread_httpd)).
:- use_module(library(http/http_dispatch)).

:- http_handler(root(echo),
                http_upgrade_to_websocket(echo, []),
                [spawn([])]).

echo(WebSocket) :- 
    writeln('Server got message'),
    ws_receive(WebSocket, Message),
    writeln(Message),
    (   Message.opcode == close
    ->  true
    ;   ws_send(WebSocket, Message),
        echo(WebSocket)
    ).
    
server(Port) :-                                    
        http_server(http_dispatch, [port(Port)]).
:- initialization 
   current_prolog_flag(argv, [SPort | _]),
   atom_number(SPort, Port),
   server(Port)
.