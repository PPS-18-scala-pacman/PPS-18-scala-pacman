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
% min_dist(+Start,+End,-Path) based on the classic map of pacman
min_dist(Start,End,Path):-
   classicMap(Graph),
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


% map for the version of pacman
classicMap([
    t(6,1)-[t(1,5)-9, t(6,5)-4, t(12,5)-10], t(21,1)-[t(15,5)-10, t(21,5)-4, t(26,5)-9],
    t(1,5)-[t(6,1)-9, t(6,5)-5, t(6,8)-8], t(6,5)-[t(6,1)-4, t(1,5)-5, t(9,5)-3, t(6,8)-3], t(9,5)-[t(6,5)-3, t(12,5)-3, t(12,11)-9], t(12,5)-[t(6,1)-10, t(9,5)-3, t(15,5)-3], t(15,5)-[t(21,1)-10, t(12,5)-3, t(18,5)-3], t(18,5)-[t(15,5)-3, t(15,11)-9, t(21,5)-3], t(21,5)-[t(21,1)-4, t(18,5)-3, t(21,8)-3, t(26,5)-5], t(26,5)-[t(21,1)-9, t(21,5)-5, t(21,8)-8],
    t(6,8)-[t(6,5)-3, t(1,5)-8, t(6,14)-6], t(21,8)-[t(21,5)-3, t(21,14)-6, t(26,5)-8],
    t(12,11)-[t(9,5)-9, t(9,14)-6, t(13,11)-1], t(13,11)-[t(12,11)-1, t(14,11)-1], t(14,11)-[t(13,11)-1, t(15,11)-1], t(15,11)-[t(18,5)-9, t(14,11)-1, t(18,14)-6],
    t(6,14)-[t(6,8)-6, t(21,14)-13, t(6,20)-6, t(9,14)-3], t(9,14)-[t(12,11)-6, t(6,14)-3, t(9,17)-3], t(18,14)-[t(15,11)-6, t(18,17)-3, t(21,14)-3], t(21,14)-[t(21,8)-6, t(18,14)-3, t(21,20)-6, t(6,14)-13],
    t(9,17)-[t(9,14)-3, t(9,20)-3, t(18,17)-9], t(18,17)-[t(18,14)-3, t(9,17)-9, t(18,20)-3],
    t(6,20)-[t(6,14)-6, t(3,26)-13, t(6,23)-3, t(9,20)-3], t(9,20)-[t(9,17)-3, t(6,20)-3, t(12,23)-6], t(18,20)-[t(18,17)-3, t(15,23)-6, t(21,20)-3], t(21,20)-[t(21,14)-6, t(18,20)-3, t(21,23)-3, t(24,26)-13],
    t(6,23)-[t(6,20)-3, t(3,26)-6, t(9,23)-3], t(9,23)-[t(6,23)-3, t(12,29)-9, t(12,23)-3], t(12,23)-[t(9,20)-6, t(9,23)-3, t(15,23)-3], t(15,23)-[t(18,20)-6, t(12,23)-3, t(18,23)-3], t(18,23)-[t(15,23)-3, t(15,29)-9, t(21,23)-3], t(21,23)-[t(21,20)-3, t(18,23)-3, t(24,26)-6],
    t(3,26)-[t(6,20)-13, t(12,29)-15, t(6,23)-6], t(24,26)-[t(21,20)-13, t(21,23)-6, t(15,29)-15],
    t(12,29)-[t(9,23)-9, t(3,26)-15, t(15,29)-3], t(15,29)-[t(18,23)-9, t(12,29)-3, t(24,26)-15],
    % ghost spawn area
    t(13,12)-[t(13,11)-1], t(14,12)-[t(14,11)-1],
    t(13,13)-[t(13,12)-1], t(14,13)-[t(14,12)-1],
    t(13,14)-[t(13,13)-1], t(14,14)-[t(14,13)-1],
    t(13,15)-[t(13,14)-1], t(14,15)-[t(14,14)-1],
    t(11,14)-[t(12,14)-1], t(12,14)-[t(13,14)-1],
    t(15,14)-[t(14,14)-1]
]).

