package it.unibo.scalapacman.lobby.dao;

import rx.Single;

import java.util.List;

public interface Dao<T> {

  Single<T> get(Integer id);

  Single<List<T>> getAll();

  Single<T> create(T t);

  Single<T> update(Integer id, T t);

  Single<T> delete(Integer id);
}
