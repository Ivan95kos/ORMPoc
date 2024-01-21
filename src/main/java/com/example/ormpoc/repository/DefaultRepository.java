package com.example.ormpoc.repository;

import com.example.ormpoc.exception.ExecuteUpdateQueryException;
import com.example.ormpoc.exception.NotFoundEntityException;
import com.example.ormpoc.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultRepository implements Repository {
    private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found by id: %s.";
    private static final String FIND_BY_ID_TEMPLATE = "SELECT * FROM %s WHERE %s = ?";
    private static final String UPDATE_ENTITY_TEMPLATE = "UPDATE %s SET %s WHERE %s = ?";
    private static final String FIELD_NAMES_FOR_UPDATE_TEMPLATE = "%s = ?";
    private static final String DELIMITER = ",";
    private static final String QUERY_EXECUTED_LOGGING = "Query : '%s' is executed \n";

    private final Map<EntityKey, Object> cashEntities = new ConcurrentHashMap<>();
    private final Map<EntityKey, Object[]> snapshot = new ConcurrentHashMap<>();

    private final DataSource dataSource;

    @Override
    public <T> Optional<T> findById(Class<T> clazz, Object id) {
        EntityKey entityKey = new EntityKey(clazz, id);
        return Optional.ofNullable(clazz.cast(cashEntities.computeIfAbsent(entityKey, this::find)));
    }

    @Override
    public void close() {
        dirtyCheckingUpdatedEntities();
        cashEntities.clear();
        snapshot.clear();
    }

    private void dirtyCheckingUpdatedEntities() {
        cashEntities.entrySet().stream()
                .filter(this::isUpdatedEntity)
                .forEach(this::updateEntity);
    }

    @SneakyThrows
    private boolean isUpdatedEntity(Map.Entry<EntityKey, Object> cashEntity) {
        Object[] entityValues = snapshot.get(cashEntity.getKey());
        Field[] declaredFields = cashEntity.getValue().getClass().getDeclaredFields();

        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            field.setAccessible(true);
            if (!Objects.equals(field.get(cashEntity.getValue()), entityValues[i])) {
                return true;
            }
        }

        return false;
    }

    private void updateEntity(Map.Entry<EntityKey, Object> cashEntity) {
        Object entity = cashEntity.getValue();
        String updateQuery = prepareUpdateQuery(entity);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            int indexOfField = 1;
            for (Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                preparedStatement.setObject(indexOfField++, field.get(entity));
            }
            preparedStatement.setObject(indexOfField, cashEntity.getKey().getId());

            preparedStatement.executeUpdate();
            System.out.printf(QUERY_EXECUTED_LOGGING, updateQuery);
        } catch (Exception e) {
            throw new ExecuteUpdateQueryException(e.getMessage(), e);
        }
    }

    private String prepareUpdateQuery(Object entity) {
        String fieldNamesForUpdate = Arrays.stream(entity.getClass().getDeclaredFields())
                .map(field -> FIELD_NAMES_FOR_UPDATE_TEMPLATE.formatted(EntityUtil.getFieldName(field)))
                .collect(Collectors.joining(DELIMITER));

        return UPDATE_ENTITY_TEMPLATE.formatted(
                EntityUtil.getTableName(entity.getClass()),
                fieldNamesForUpdate,
                EntityUtil.getFieldIdName(entity.getClass()));
    }

    private Object find(EntityKey entityKey) {
        String selectQuery = FIND_BY_ID_TEMPLATE.formatted(
                EntityUtil.getTableName(entityKey.getClazz()),
                EntityUtil.getFieldIdName(entityKey.getClazz()));

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setObject(1, entityKey.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.printf(QUERY_EXECUTED_LOGGING, selectQuery);

            return resultSet.next() ? makeSnapshot(entityKey, prepareEntity(resultSet, entityKey.getClazz())) : null;
        } catch (Exception e) {
            throw new NotFoundEntityException(ENTITY_NOT_FOUND_MESSAGE.formatted(entityKey.getId()), e);
        }
    }

    @SneakyThrows
    private Object prepareEntity(ResultSet resultSet, Class<?> clazz) {
        Object entity = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = EntityUtil.getFieldName(field);
            Object value = resultSet.getObject(fieldName);

            field.setAccessible(true);
            field.set(entity, value);
        }

        return entity;
    }

    @SneakyThrows
    private Object makeSnapshot(EntityKey entityKey, Object entity) {
        Field[] declaredFields = entity.getClass().getDeclaredFields();
        Object[] valuesSnapshot = new Object[declaredFields.length];

        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            field.setAccessible(true);
            valuesSnapshot[i] = field.get(entity);
        }
        snapshot.put(entityKey, valuesSnapshot);

        return entity;
    }
}
