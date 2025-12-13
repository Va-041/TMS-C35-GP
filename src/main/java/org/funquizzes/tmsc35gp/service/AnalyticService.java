//package org.funquizzes.tmsc35gp.service;
//
//import org.funquizzes.tmsc35gp.entity.Quiz;
//import org.funquizzes.tmsc35gp.entity.QuizRating;
//import org.funquizzes.tmsc35gp.repository.QuizRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class AnalyticService {
//
//    class CalculateQuizRating {
//        @Autowired
//        private QuizRatingRepository quizRatingRepository;
//
//        @Autowired
//        private QuizRepository quizRepository;
//
//        @Transactional
//        public void addRating(QuizRating quizRating) {
//            // Сохраняем оценку пользователя
//            quizRatingRepository.save(quizRating);
//
//            // Пересчитываем средний рейтинг для викторины
//            updateAverageRating(quizRating.getQuiz().getId());
//        }
//
//        public void updateAverageRating(Long quizId) {
//            Double average = quizRepository.calculateAverageRating(quizId);
//
//            // Если нет оценок, можно установить null или 0.0
//            if (average == null) {
//                average = 0.0;
//            }
//
//            Quiz quiz = quizRepository.findById(quizId).orElseThrow();
//            quiz.setAverageRating(average);
//            quizRepository.save(quiz);
//        }
//    }
//}
