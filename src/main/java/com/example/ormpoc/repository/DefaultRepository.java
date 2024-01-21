package com.example.ormpoc.repository;

import com.example.ormpoc.exception.NotFoundEntityException;
import com.example.ormpoc.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class DefaultRepository implements Repository {
    private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found by id: %s.";
    private static final String FIND_BY_ID_TEMPLATE = "SELECT * FROM %s WHERE %s = ?";

    private Map<EntityKey<?>, Object> cashEntities = new ConcurrentHashMap<>();

    private final DataSource dataSource;

    @Override
    public <T, ID> Optional<T> findById(Class<T> clazz, ID id) {
        EntityKey<T> entityKey = new EntityKey<>(clazz, id);
        return Optional.ofNullable(clazz.cast(cashEntities.computeIfAbsent(entityKey, this::find)));
    }

    private <T> T find(EntityKey<T> entityKey) {
        String selectQuery = FIND_BY_ID_TEMPLATE.formatted(
                EntityUtil.getTableName(entityKey.getClazz()),
                EntityUtil.getFieldIdName(entityKey.getClazz()));
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setObject(1, entityKey.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("Query : '" + selectQuery + "' is executed");

            return resultSet.next() ? prepareEntity(resultSet, entityKey.getClazz()) : null;
        } catch (Exception e) {
            throw new NotFoundEntityException(ENTITY_NOT_FOUND_MESSAGE.formatted(entityKey.getId()), e);
        }
    }

    @SneakyThrows
    private <T> T prepareEntity(ResultSet resultSet, Class<T> clazz) {
        T entity = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = EntityUtil.getFieldName(field);
            Object value = resultSet.getObject(fieldName);

            field.setAccessible(true);
            field.set(entity, value);
        }

        return entity;
    }
}
