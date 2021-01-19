package it.unibo.scalapacman.lobby.dao;

import io.reactivex.Single;

import java.util.List;

public interface Dao<T, ID> {

  Single<T> get(ID id);

  Single<List<T>> getAll();

  Single<List<T>> getAll(T searchParam);

  Single<T> create(T t);

  Single<T> update(ID id, T t);

  Single<T> delete(ID id);
}
