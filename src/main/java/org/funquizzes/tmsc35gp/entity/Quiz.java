package org.funquizzes.tmsc35gp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    private String headImageUrl;
    private Integer maxQuestions = 30;
    private Integer timeLimitMinutes; // общее время на викторину (сумма времени на все вопросы)

    //quiz statistic
    private Integer playsCount = 0; // сколько раз играли в викторину

    @Column
    private Double averageRating = 0.0; // рейтинг викторины

    private Integer ratingCounts = 0; // количество оценок викторины

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();


    @CreationTimestamp
    private LocalDateTime createdAt;


    public boolean isPublic() {
        return isPublic != null && isPublic ;
    }
}
