package com.enttrac.backend.repository;

import java.util.List;

public interface MediaRepository<T> {
    void save(T item);
    T findById(String id);
    List<T> findAll();
    void delete(String id);
}