package com.guilherme.finance_api.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categories")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Cada categoria pertence a um usuário específico.
    // A coluna user_id garante isolamento entre contas.
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

}
