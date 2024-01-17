package com.example.ormpoc.model;

import com.example.ormpoc.annotation.Column;
import com.example.ormpoc.annotation.Entity;
import com.example.ormpoc.annotation.Id;
import com.example.ormpoc.annotation.Table;
import lombok.ToString;

@Entity
@Table(name = "persons")
@ToString
public class Person {

    @Id
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "surname")
    private String surname;
}
