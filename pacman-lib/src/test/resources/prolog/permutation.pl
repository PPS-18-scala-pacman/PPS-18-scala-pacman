member([H|T],H,T).
member([H|T],E,[H|T2]):- member(T,E,T2).
permutation([],[]).
permutation(L,[H|TP]) :- member(L,H,T), permutation(T,TP).
