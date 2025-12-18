package org.funquizzes.tmsc35gp.dto;

import lombok.Data;

@Data
public class StatsDTO {
    private long activeQuizzes;
    private long gamesPlayed;
    private int onlinePlayers;
    private int satisfiedPlayersPercentage;
}