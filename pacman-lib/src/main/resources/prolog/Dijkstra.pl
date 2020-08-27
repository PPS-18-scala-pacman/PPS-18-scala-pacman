% Ricerca del percorso più breve dal vertice di partenza a quello di destinazione
%
% shortest_path(+Graph, +Start, +End, -Path)
shortest_path(Graph, Start, End, Path):-
   dijkstra(Graph, [], [Start-0-[Start]], End, _-_-ReversePath),
   reverse(ReversePath, Path).
% shortest_path(+Start, +End, -Path) basato sulla mappa classica di Pacman
shortest_path(Start, End, Path):-
   classicMap(Graph),
   dijkstra(Graph, [], [Start-0-[Start]], End, _-_-ReversePath),
   reverse(ReversePath, Path).

% Implementazione dell'algoritmo di Dijkstra per il calcolo del percorso minore fino ad un dato vertice
% Graph è il grafo di riferimento
% ClosedVertices sono i vertici già visitati
% OpenVertices sono i vertici da visitare
% End è il vertice al quale si vuole arrivare
% Path è il percorso restituito dall'algoritmo
%
% dijkstra(+Graph, +ClosedVertices, +OpenVertices, +End, -Path)
dijkstra(_, _, Open, End, End-D-P):-
   next_best_vertex(Open, End-D-P, _), !.
dijkstra(Graph, Closed, Open, End, MinDist):-
   next_best_vertex(Open, V-D-P, RestOpen),
   neighbourhood(Graph, V, NB),
   prune_neighboors(NB, Closed, NewNB),
   concat_path(NewNB, P, NBP),
   merge(NBP, RestOpen, D, NewOpen),
   dijkstra(Graph, [V-D-P|Closed], NewOpen, End, MinDist).

% Seleziona il prossimo vertice, scegliendo quello con distanza minore
% Se ci sono più vertici con distanza minore, prende il primo
%
% next_best_vertex(+OpenVertices, -VertexToExpand, -RestOpenVertices)
next_best_vertex([H|T], MinV, Rest):-
   next_best_vertex_min(T, H, MinV, Rest).
next_best_vertex_min([], MinV, MinV, []).
next_best_vertex_min([H|T], LocalMin, MinV, [LocalMin|Rest]):-
   nearest(H, LocalMin), !,
   next_best_vertex_min(T, H, MinV, Rest).
next_best_vertex_min([H|T], LocalMin, MinV, [H|Rest]):-
   next_best_vertex_min(T, LocalMin, MinV, Rest).

% nearest(A, B) restituisce true se la distanza del primo elemento è inferiore a quella del secondo
% A e B sono elementi così composti Vertex-Distance-Path
%
% nearest(+A, +B)
nearest(_-Dx-_, _-Dy-_):- Dx < Dy.

% Recupera la lista dei vertici (con l'informazione sulla distanza) a cui il vertice passato è collegato
%
% neighbourhood(+Graph, +Vertex, -Neighbourhood)
neighbourhood(Graph, V, NB):-
   member(V-NB, Graph).

% Ritorna la lista di vertici passata in ingresso filtrata dei vertici già visitati
%
% prune_neighboors(+ListOfVertices, +Closed, -ListOfNonClosedVertices)
prune_neighboors([], _, []).
prune_neighboors([H|T], Closed, NewT):-
   H = V-_,
   member(V-_-_, Closed), !,
   prune_neighboors(T, Closed, NewT).
prune_neighboors([H|T], Closed, [H|NewT]):- prune_neighboors(T, Closed, NewT).

% Ritorna la lista dei vicini arricchita del path passato in ingresso
%
% concat_path(+Neighbourhood, +Path, -NeighbourhoodWithPath)
concat_path([], _, []).
concat_path([V-D|T], P, [V-D-[V|P]|NewT]):-
    concat_path(T, P, NewT).

% Ritorna la lista di tutti i vertici aperti creata a partire dalla lista di vertici in esame (i vicini)
% e la lista attuale dei vertici aperti
% Se un elemento in ListOfVertices ha lo stesso vertice di un elemento in OldOpenVertices, l'elemento
% con la distanza minore verrà aggiunto ad AllOpenVertices
%
% merge(+ListOfVertices, +OldOpenVertices, +Distance, -AllOpenVertices)
merge([], L, _, L).
merge([V1-D1-_|T], Open, D, [V1-D2-P2|SubOpen]):-
   remove(Open, V1-D2-P2, RestOpen),
   integer(D2), D2 < D+D1, !,
   merge(T, RestOpen, D, SubOpen).
merge([V1-D1-P1|T], Open, D, [V1-(D+D1)-P1|SubOpen]):-
   merge(T, Open, D, SubOpen).

% Rimuove un elemento da una lista, ritornando l'elemento rimosso e la lista filtrata
% Se l'elemento non è presente nella lista, ritorna la lista invariata
%
% remove(+List, ?ElementToRemove, -Rest)
remove([], _, []):- !.
remove([H|T], H, T).
remove([H|T], X, [H|NT]):-
   H \= X,
   remove(T, X, NT).

% Rappresentazione della mappa classica con i soli incroci (tutte le zone dei GhostSpawn sono considerati incroci)
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
