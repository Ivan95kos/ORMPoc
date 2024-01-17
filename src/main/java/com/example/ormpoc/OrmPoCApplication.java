package com.example.ormpoc;

import com.example.ormpoc.model.Note;
import com.example.ormpoc.model.Person;
import com.example.ormpoc.repository.DefaultRepository;
import com.example.ormpoc.repository.Repository;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class OrmPoCApplication {
    public static void main(String[] args) {
        DataSource dataSource = initDB();

        Repository<Person, Long> personRepository = new DefaultRepository<>(dataSource, Person.class);
        Repository<Note, Long> noteRepository = new DefaultRepository<>(dataSource, Note.class);

        personRepository.findById(1L).ifPresent(System.out::println);
        noteRepository.findById(1L).ifPresent(System.out::println);
    }

    public static DataSource initDB() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/postgres");
        dataSource.setDatabaseName("postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("example");
        return dataSource;
    }
}
