package com.enttrac.backend.repository;

import java.util.List;

public interface MediaRepository<T> {
    void save(T item);
    T findById(String userId, String id);
    List<T> findAll(String userId);
    void delete(String userId, String id);
}