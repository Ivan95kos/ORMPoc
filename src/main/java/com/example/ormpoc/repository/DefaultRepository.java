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
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultRepository<T, ID> implements Repository<T, ID> {
    private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found by id: %s.";
    private static final String FIND_BY_ID_TEMPLATE = "SELECT * FROM %s WHERE %s = ?";

    private final DataSource dataSource;
    private final Class<T> clazz;

    @Override
    public Optional<T> findById(ID id) {
        String selectQuery = FIND_BY_ID_TEMPLATE.formatted(
                EntityUtil.getTableName(clazz),
                EntityUtil.getFieldIdName(clazz));

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setObject(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next() ? prepareEntity(resultSet) : Optional.empty();
        } catch (Exception e) {
            throw new NotFoundEntityException(ENTITY_NOT_FOUND_MESSAGE.formatted(id), e);
        }
    }

    @SneakyThrows
    private Optional<T> prepareEntity(ResultSet resultSet) {
        T entity = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = EntityUtil.getFieldName(field);
            Object value = resultSet.getObject(fieldName);

            field.setAccessible(true);
            field.set(entity, value);
        }

        return Optional.of(entity);
    }
}
