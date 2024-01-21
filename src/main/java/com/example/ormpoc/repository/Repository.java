package com.example.ormpoc.repository;

import java.io.Closeable;
import java.util.Optional;

public interface Repository extends Closeable {

    <T> Optional<T> findById(Class<T> clazz, Object id);

    @Override
    void close();

}
