package com.example.ormpoc.model;

import com.example.ormpoc.annotation.Column;
import com.example.ormpoc.annotation.Entity;
import com.example.ormpoc.annotation.Id;
import com.example.ormpoc.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "notes")
@ToString
@Setter
@Getter
public class Note {

    @Id
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

}
