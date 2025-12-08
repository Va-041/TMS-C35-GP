package org.funquizzes.tmsc35gp.service;

import org.funquizzes.tmsc35gp.dto.ChangePasswordDto;
import org.funquizzes.tmsc35gp.dto.UpdateProfileDto;
import org.funquizzes.tmsc35gp.entity.Role;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.entity.UserStatistic;
import org.funquizzes.tmsc35gp.repository.StatisticRepository;
import org.funquizzes.tmsc35gp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(11);
    @Autowired
    private StatisticRepository statisticRepository;

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

    // create user
    @Transactional
    public void create(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.getRoles().add(Role.ROLE_USER);
        user.setCreatedAt(LocalDateTime.now());
        //save user
        User savedUser = userRepository.save(user);
        //create + save statistic
        UserStatistic statistic = new UserStatistic(savedUser);
        statisticRepository.save(statistic);
        //связываем user и statistic
        savedUser.setStatistic(statistic);
        userRepository.save(savedUser);
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