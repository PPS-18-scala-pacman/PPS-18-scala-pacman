% Is based on idea of neighbourhood and graph is represented as a list of vertices and its neighbourhood
% Calcola il peso del percorso più breve dal nodo di partenza +Start a tutti i nodi del grafo
% ?- min_dist([0-[1-1], 1-[2-1, 3-1], 2-[], 3-[]], 0, 3, X).
% Result: X = [0, 1, 3]    X / [0,1,3]

% edge(Graph,V1,V2,Value):-
%    member(V1-NB-[V1],Graph),
%    member(V2-Value-[V2],NB).

neighbourhood(Graph,V,NB):-
   member(V-NB,Graph).

% min_dist(+Graph,+Start,+End,-Path)
min_dist(Graph,Start,End,Path):-
   dijkstra(Graph,[],[Start-0-[Start]],MinDist),
   member(End-_-ReversePath, MinDist),
   reverse(ReversePath, Path).

% dijkstra(+Graph,+ClosedVertices,+OpenVertices,+End,-Path)
dijkstra(_,MinDist,[],MinDist).
dijkstra(Graph,Closed,Open,MinDist):-
   choose_v(Open,V-D-P,RestOpen),
   print(V-D-P), nl,
   neighbourhood(Graph,V,NB),  % NB is a list of adjacent vertices+distance to V
   diff(NB,Closed,NewNB),
   add_path(NewNB, P, NBP),	   % NBP is a list of adjacent vertices+distance and path to V
   merge(NBP,RestOpen,D,NewOpen),
   dijkstra(Graph,[V-D-P|Closed],NewOpen,MinDist).

% add_path(+Neighbourhood,+Path,-NeighbourhoodWithPath)
add_path([], _, []).
add_path([V-D|T], P, [V-D-[V|P]|NewT]):-
    add_path(T, P, NewT).

% choose_v(+OpenVertices,-VertexToExpand,-RestOpenVertices)
choose_v([H|T],MinV,Rest):-
   choose_minv(T,H,MinV,Rest).
choose_minv([],MinV,MinV,[]).
choose_minv([H|T],M,MinV,[H2|Rest]):-
   H=V1-D1-P1, M=V-D-P,
   (D1<D -> NextM=H,H2=M
          ; NextM=M,H2=H),
   choose_minv(T,NextM,MinV,Rest).

% diff(+ListOfVertices,+Closed,-ListOfNonClosedVertices)
diff([],_,[]).
diff([H|T],Closed,L):-
   H=V-D,
   (member(V-_-_,Closed) -> L=NewT ; L=[H|NewT]),
   diff(T,Closed,NewT).

% merge(+ListOfVertices,+OldOpenVertices,+Distance,-AllOpenVertices)
merge([],L,_,L).
merge([V1-D1-P1|T],Open,D,NewOpen):-
   (remove(Open,V1-D2-P2,RestOpen)
      -> (D2<D+D1 -> VD=D2, VP=P2 ; VD=D+D1, VP=P1)  % VP deve prendere il valore in base a VD
       ; (RestOpen=Open,VD is D+D1, VP=P1) ),
   NewOpen=[V1-VD-VP|SubOpen],
   merge(T,RestOpen,D,SubOpen).

% remove(+List, +ElementToRemove, -Rest)
remove([H|T],H,T).
remove([H|T],X,[H|NT]):-
   H\=X,
   remove(T,X,NT).

% reverse(+List, -RevertedList)
reverse(L1,L2):- reverse(L1,L2,[]).
reverse([],Z,Z).
reverse([H|T],Z,Acc) :- reverse(T,Z,[H|Acc]).
