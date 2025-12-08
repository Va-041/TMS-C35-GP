package org.funquizzes.tmsc35gp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_statistics")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    private User user;

    private Integer totalScore;
    private Integer totalQuizzesCreated;
    private Integer totalQuizzesPlayed;
    private Integer totalCorrectAnswers;
    private Integer totalCorrectQuestionsAnswers;

    private Integer bestScore;
    private Integer averageScore;
    private Integer winStreak;
    private Integer longestWinStreak;

    public UserStatistic(User user) {
        this.user = user;
        this.totalScore = 0;
        this.totalQuizzesCreated = 0;
        this.totalQuizzesPlayed = 0;
        this.totalCorrectAnswers = 0;
        this.totalCorrectQuestionsAnswers = 0;
        this.bestScore = 0;
        this.averageScore = 0;
        this.winStreak = 0;
        this.longestWinStreak = 0;
    }
}
