package it.unibo.scalapacman.lobby.dao;

import rx.Single;

import java.util.List;

public interface Dao<T, ID> {

  Single<T> get(ID id);

  Single<List<T>> getAll();

  Single<T> create(T t);

  Single<T> update(ID id, T t);

  Single<T> delete(ID id);
}
