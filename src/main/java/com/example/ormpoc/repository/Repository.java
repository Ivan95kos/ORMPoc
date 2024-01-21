package com.example.ormpoc.repository;

import java.util.Optional;

public interface Repository {

    <T> Optional<T> findById(Class<T> clazz, Object id);

}
