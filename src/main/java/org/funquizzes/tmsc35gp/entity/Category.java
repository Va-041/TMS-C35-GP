package org.funquizzes.tmsc35gp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quiz_categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String icon;
    private String color;

    @OneToMany(mappedBy = "category")
    private List<Quiz> quizzes = new ArrayList<>();

    private boolean active = true;

    public Category (String name, String description) {
        this.name = name;
        this.description = description;
    }

}
