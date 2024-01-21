package com.example.ormpoc.repository;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class EntityKey<T> {
    private Class<T> clazz;
    private Object id;
}
