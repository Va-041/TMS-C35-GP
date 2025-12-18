package org.funquizzes.tmsc35gp.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.funquizzes.tmsc35gp.dto.ChangePasswordDto;
import org.funquizzes.tmsc35gp.dto.UpdateProfileDto;
import org.funquizzes.tmsc35gp.entity.Role;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.entity.UserStatistic;
import org.funquizzes.tmsc35gp.repository.StatisticRepository;
import org.funquizzes.tmsc35gp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.funquizzes.tmsc35gp.controller.UserController.logger;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(11);
    @Autowired
    private StatisticRepository statisticRepository;

    // Мапа для хранения времени последней активности по username
    private final Map<String, LocalDateTime> userActivityMap = new ConcurrentHashMap<>();

    // Время в минутах, после которого пользователь считается оффлайн
    private static final int OFFLINE_THRESHOLD_MINUTES = 3;

    // Текущее количество онлайн пользователей
    @Getter
    private volatile int onlineUsersCount = 0;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> byUsername = userRepository.findByUsername(username);
        if (byUsername.isPresent()) {
            User user = byUsername.get();
            user.setStatistic(getOrCreateStatistic(user));

            return user;
        }
        throw new UsernameNotFoundException("User not found");
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //get or create user statistic
    private UserStatistic getOrCreateStatistic(User user) {
        return statisticRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserStatistic newStatistic = new UserStatistic(user);
                    return statisticRepository.save(newStatistic);
                });
    }

    public UserStatistic getUserStatistics(String username) {
        User user = (User) loadUserByUsername(username);
        return getOrCreateStatistic(user);
    }

    @Transactional
    public void updateLastActivity(String username) {
        User user = (User) loadUserByUsername(username);

        // Обновляем в БД
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Обновляем в памяти
        userActivityMap.put(username, LocalDateTime.now());

        // Пересчитываем онлайн пользователей
        recalculateOnlineUsers();
    }

    // пересчёт онлайна
    private void recalculateOnlineUsers() {
        LocalDateTime threshold = LocalDateTime.now().minus(OFFLINE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);

        long onlineCount = userActivityMap.entrySet().stream()
                .filter(entry -> entry.getValue().isAfter(threshold))
                .count();

        this.onlineUsersCount = (int) onlineCount;
    }

    // статистика онлайн пользователей
    public Map<String, Object> getOnlineStats() {
        LocalDateTime threshold = LocalDateTime.now().minus(OFFLINE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);

        Map<String, Object> stats = new HashMap<>();
        stats.put("onlineCount", onlineUsersCount);
        stats.put("totalActive", userActivityMap.size());
        stats.put("thresholdMinutes", OFFLINE_THRESHOLD_MINUTES);

        return stats;
    }

    // чистка неактивных каждую минуту
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cleanupInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minus(OFFLINE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);

        // Удаляем неактивных из мапы
        userActivityMap.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));

        // Пересчитываем онлайн пользователей
        recalculateOnlineUsers();

        System.out.println("Cleaned up inactive users. Online: " + onlineUsersCount);
    }

    // загрузка при иницаизации пользователй которые были активны недавно.
    @PostConstruct
    @Transactional
    public void initOnlineUsers() {
        LocalDateTime threshold = LocalDateTime.now().minus(OFFLINE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);

        // Загружаем всех пользователей, у которых lastLoginAt был недавно
        List<User> recentlyActiveUsers = userRepository.findAll().stream()
                .filter(user -> user.getLastLoginAt() != null &&
                        user.getLastLoginAt().isAfter(threshold))
                .collect(Collectors.toList());

        // Добавляем их в мапу
        for (User user : recentlyActiveUsers) {
            userActivityMap.put(user.getUsername(), user.getLastLoginAt());
        }

        recalculateOnlineUsers();
        System.out.println("Initialized online users: " + onlineUsersCount);
    }

    // create user
    @Transactional
    public void create(User user) {
        logger.info("=== UserService.create() ВЫЗВАН ===");
        logger.info("Username: {}", user.getUsername());

        try {
            // 1. Проверка существования пользователя
            logger.info("Проверка существования пользователя...");
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                logger.error("Пользователь уже существует: {}", user.getUsername());
                throw new IllegalArgumentException("Пользователь с таким именем уже существует");
            }
            logger.info("Пользователь не существует, можно создавать");

            // 2. Подготовка пользователя
            logger.info("Шифрование пароля...");
            user.setPassword(encoder.encode(user.getPassword()));
            user.getRoles().add(Role.ROLE_USER);
            logger.info("Роль добавлена: ROLE_USER");

            // 3. Сохраняем пользователя
            logger.info("Сохранение пользователя в БД...");
            User savedUser = userRepository.save(user);
            logger.info("User saved with ID: {}", savedUser.getId());

            // 4. Создаем статистику
            logger.info("Создание статистики...");
            UserStatistic statistic = new UserStatistic(savedUser);
            statisticRepository.save(statistic);
            logger.info("Statistic created");

            logger.info("=== ПОЛЬЗОВАТЕЛЬ УСПЕШНО СОЗДАН ===");

        } catch (Exception e) {
            logger.error("ОШИБКА в create(): {}", e.getMessage(), e);
            throw e;
        }
    }

    //update profile
    @Transactional
    public void updateProfile(String username, UpdateProfileDto dto) {
        User user =  (User) loadUserByUsername(username);

        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setBiography(dto.getBiography());
        user.setPublicProfile(dto.isPublicProfile());

        // Обновляем аватарку, если она была загружена
        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        userRepository.save(user);
    }

    //delete profile
    @Transactional
    public void deleteProfile(Long id) {
        //delete statistic
        Optional<UserStatistic> statistic = statisticRepository.findByUserId(id);
        statistic.ifPresent(statisticRepository::delete);
        //delete user
        userRepository.deleteById(id);
    }

    //change password
    @Transactional
    public boolean changePassword(String username, ChangePasswordDto dto) {
        User user =  (User) loadUserByUsername(username);

        if(!encoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            return false;
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return false;
        }
        user.setPassword(encoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    //update login time (время входа, типа когда был в сети)
    @Transactional
    public void updateLoginTime(String username) {
        User user =  (User) loadUserByUsername(username);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    //сколько викторин сыграл всего
    @Transactional
    public void incrementQuizzesPlayed(String username) {
        UserStatistic statistic = getOrCreateStatistic((User) loadUserByUsername(username));
        statistic.setTotalQuizzesPlayed(statistic.getTotalQuizzesPlayed() + 1);
        statisticRepository.save(statistic);
    }

    //сколько викторин создал
    @Transactional
    public void incrementQuizzesCreated(String username) {
        UserStatistic statistic = getOrCreateStatistic((User) loadUserByUsername(username));
        statistic.setTotalQuizzesCreated(statistic.getTotalQuizzesCreated() + 1);
        statisticRepository.save(statistic);
    }

    //добавление рейтинга в профиль
    @Transactional
    public void addScore(String username, int score) {
        UserStatistic statistic = getOrCreateStatistic((User) loadUserByUsername(username));
        statistic.setTotalScore(statistic.getTotalScore() + score);

        //обновляем лучший результат
        if (score > statistic.getBestScore()) {
            statistic.setBestScore(score);
        }

        //обновляем серию побед
        if (score > 0) {
            statistic.setWinStreak(statistic.getWinStreak() + 1);
            if (statistic.getWinStreak() > statistic.getLongestWinStreak()) {
                statistic.setLongestWinStreak(statistic.getWinStreak());
            }
        } else {
            statistic.setWinStreak(0);
        }

        statisticRepository.save(statistic);
    }

    @Transactional
    public void addCorrectAnswer(String username) {
        UserStatistic statistic = getOrCreateStatistic((User) loadUserByUsername(username));
        statistic.setTotalCorrectAnswers(statistic.getTotalCorrectAnswers() + 1);
        statisticRepository.save(statistic);
    }

    @Transactional
    public void addCorrectQuestion(String username) {
        UserStatistic statistic = getOrCreateStatistic((User) loadUserByUsername(username));
        statistic.setTotalCorrectQuestionsAnswers(statistic.getTotalCorrectQuestionsAnswers() + 1);
        statisticRepository.save(statistic);
    }

    @Transactional
    public void calculateAverageScore(String username) {
        UserStatistic statistic = getOrCreateStatistic((User) loadUserByUsername(username));
        if (statistic.getTotalQuizzesPlayed() > 0) {
            int average = statistic.getTotalScore() / statistic.getTotalQuizzesPlayed();
            statistic.setAverageScore(average);
            statisticRepository.save(statistic);
        }
    }

    @Transactional
    public void addFriend(String username, Long friendId) {
        User user = (User) loadUserByUsername(username);
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        user.getFriends().add(friend);
        userRepository.save(user);
    }

    @Transactional
    public void removeFriend(String username, Long friendId) {
        User user = (User) loadUserByUsername(username);
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        user.getFriends().remove(friend);
        userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> searchUsers(String query) {
        List<User> users = userRepository.findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(query, query);
        //загурзка статистики для каждого пользователя
        users.forEach(user -> user.setStatistic(getOrCreateStatistic(user)));
        return users;
    }

    public List<UserStatistic> getTopUsersByScore(int limit) {
        return statisticRepository.findTopByOrderByTotalScoreDesc(limit);
    }

}