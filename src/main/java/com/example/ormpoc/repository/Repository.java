package com.example.ormpoc.repository;

import java.util.Optional;

public interface Repository {

    <T, ID> Optional<T> findById(Class<T> clazz, ID id);

}
