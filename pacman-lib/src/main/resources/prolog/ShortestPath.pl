%:- consult("Dijkstra.pl").
%:- consult("Maps.pl").

% Ricerca del percorso più breve dal vertice di partenza a quello di destinazione
%
% shortest_path(+Graph, +Start, +End, -Path)
shortest_path(Graph, Start, End, Path):-
   list(Graph), !,
   calculate_path(Graph, Start, End, Path).
% shortest_path(+Start, +End, -Path) basato sulla mappa classica di Pacman
shortest_path(Identifier, Start, End, Path):-
   map(Identifier, Graph),
   calculate_path(Graph, Start, End, Path).

calculate_path(Graph, Start, End, Path):-
   dijkstra(Graph, [], [Start-0-[Start]], End, _-_-ReversePath),
   reverse(ReversePath, Path).
