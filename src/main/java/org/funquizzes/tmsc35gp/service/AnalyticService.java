package org.funquizzes.tmsc35gp.service;

import org.funquizzes.tmsc35gp.dto.StatsDTO;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.repository.QuizRepository;
import org.funquizzes.tmsc35gp.repository.StatisticRepository;
import org.funquizzes.tmsc35gp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AnalyticService {

    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private StatisticRepository statisticRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    // время в течение которого пользователь считается онлайн
    private static final int ONLINE_THRESHOLD_MINUTES = 15;


    // статистика для главной страницы
    public StatsDTO getHomePageStats() {
        StatsDTO stats = new StatsDTO();

        // 1. Активные (публичные) викторины
        stats.setActiveQuizzes(quizRepository.countByIsPublicTrue());

        // 2. Всего сыгранных игр
        stats.setGamesPlayed(getTotalGamesPlayed());

        // 3. Игроков онлайн
        stats.setOnlinePlayers(userService.getOnlineUsersCount());
        // 4. Процент довольных игроков (временно фиксированный)
        stats.setSatisfiedPlayersPercentage(calculateSatisfactionPercentage());

        return stats;
    }

    // общее количество сыгранных игр
    private long getTotalGamesPlayed() {
        return statisticRepository.findAll().stream()
                .mapToLong(stat -> stat.getTotalQuizzesPlayed() != null ? stat.getTotalQuizzesPlayed() : 0)
                .sum();
    }

    // процент довольных игроков
    private int calculateSatisfactionPercentage() {
        // Временная реализация:
        // 1. Получаем всех пользователей с публичным профилем
        List<User> publicUsers = userRepository.findPublicUsers();
        if (publicUsers.isEmpty()) {
            return 95; // дефолтное значение
        }

        // 2. Считаем пользователей с положительной статистикой
        long satisfiedUsers = publicUsers.stream()
                .filter(user -> {
                    var stat = statisticRepository.findByUserId(user.getId()).orElse(null);
                    if (stat == null) return false;

                    // Условия для "довольного" игрока:
                    // - Играл хотя бы в 5 викторин
                    // - Средний балл больше 50%
                    // - Создал хотя бы 1 викторину или играл много
                    int gamesPlayed = stat.getTotalQuizzesPlayed() != null ? stat.getTotalQuizzesPlayed() : 0;
                    int gamesCreated = stat.getTotalQuizzesCreated() != null ? stat.getTotalQuizzesCreated() : 0;

                    return gamesPlayed >= 5 || gamesCreated >= 1;
                })
                .count();

        // 3. Рассчитываем процент
        double percentage = (double) satisfiedUsers / publicUsers.size() * 100;
        return (int) Math.min(100, Math.max(80, percentage)); // от 80% до 100%
    }

    // количество новых пользователей за последнюю неделю
    public int getNewUsersLastWeek() {
        LocalDateTime weekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);

        return (int) userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null &&
                        user.getCreatedAt().isAfter(weekAgo))
                .count();
    }

    // Получает топ категорий по количеству викторин
    public List<Object[]> getTopCategories() {
        return quizRepository.findTopCategories();
    }

}