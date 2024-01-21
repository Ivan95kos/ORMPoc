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

        try (Repository repository = new DefaultRepository(dataSource)) {
            System.out.println("First call to DB");
            repository.findById(Person.class, 1L).ifPresent(System.out::println);
            repository.findById(Note.class, 1L).ifPresent(System.out::println);


            System.out.println("Second call to DB");
            repository.findById(Person.class, 1L).ifPresent(System.out::println);
            repository.findById(Note.class, 1L).ifPresent(System.out::println);

            System.out.println("Dirty Checking");
            repository.findById(Person.class, 1L).ifPresent(person -> person.setLastName("New LastName"));
            repository.findById(Note.class, 1L).ifPresent(note -> note.setBody("New Body"));
        }

        try (Repository repository = new DefaultRepository(dataSource)) {
            System.out.println("Call to DB after Dirty Checking");
            repository.findById(Person.class, 1L).ifPresent(System.out::println);
            repository.findById(Note.class, 1L).ifPresent(System.out::println);
        }
    }

    public static DataSource initDB() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/postgres");
        dataSource.setDatabaseName("postgres");
        dataSource.setUser("user");
        dataSource.setPassword("password");
        return dataSource;
    }
}
