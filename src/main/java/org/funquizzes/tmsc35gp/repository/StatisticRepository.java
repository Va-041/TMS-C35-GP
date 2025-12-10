package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.UserStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatisticRepository extends JpaRepository<UserStatistic, Long> {

    Optional<UserStatistic> findByUserId(Long userId);

    @Query("SELECT s FROM UserStatistic s WHERE s.user.username = :username")
    Optional<UserStatistic> findByUsername(@Param("username") String username);

    @Query("SELECT s FROM UserStatistic s WHERE s.user.isPublicProfile = true ORDER BY s.totalScore DESC")
    List<UserStatistic> findTopByOrderByTotalScoreDesc(@Param("limit") int limit);

    @Query("SELECT s FROM UserStatistic s WHERE s.user.isPublicProfile = true ORDER BY s.winStreak DESC")
    List<UserStatistic> findTopByWinStreakDesc(@Param("limit") int limit);

    @Query("SELECT s FROM UserStatistic s WHERE s.user.isPublicProfile = true ORDER BY s.bestScore DESC")
    List<UserStatistic> findTopByBestScoreDesc(@Param("limit") int limit);
}
