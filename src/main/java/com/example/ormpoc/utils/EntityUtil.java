package com.example.ormpoc.utils;

import com.example.ormpoc.annotation.Column;
import com.example.ormpoc.annotation.Id;
import com.example.ormpoc.annotation.Table;
import com.example.ormpoc.exception.NotFoundIdException;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

@UtilityClass
public class EntityUtil {

    private static final String ID_NOT_DEFINED_MESSAGE = "There is no id defined in the class: %s.";

    public static String getTableName(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(Table.class))
                .map(Table::name)
                .orElse(clazz.getSimpleName().toLowerCase());
    }

    public static String getFieldName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .orElse(field.getName().toLowerCase());
    }

    public static String getFieldIdName(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(field -> field.getName().toLowerCase())
                .findFirst()
                .orElseThrow(() -> new NotFoundIdException(String.format(ID_NOT_DEFINED_MESSAGE, clazz.getSimpleName())));
    }
}
