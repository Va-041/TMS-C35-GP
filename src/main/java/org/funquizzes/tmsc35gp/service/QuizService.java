package org.funquizzes.tmsc35gp.service;

import org.funquizzes.tmsc35gp.dto.CreateQuizDto;
import org.funquizzes.tmsc35gp.dto.UpdateQuizDto;
import org.funquizzes.tmsc35gp.entity.*;
import org.funquizzes.tmsc35gp.repository.CategoryRepository;
import org.funquizzes.tmsc35gp.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private QuestionService questionService;

    // path to upload images
    private final String uploadImagesDir = "src/main/resources/static/images/";

    @Transactional
    public Quiz createQuiz(CreateQuizDto dto, User creator) {

        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setIsPublic(dto.isPublic());
        quiz.setCreator(creator);
        quiz.setDifficultyLevel(dto.getDifficultyLevel());
        quiz.setHeadImageUrl(dto.getHeadImage());
        quiz.setMaxQuestions(dto.getMaxQuestions());
        quiz.setTimeLimitMinutes(dto.getTimeLimitMinutes());

        //give category
        if (dto.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(dto.getCategoryId());
            quiz.setCategory(category);
        }

        //create questions
        List<Question> questions = questionService.createQuestionsFromDto(dto.getQuestions(), quiz);

        quiz.setQuestions(questions);

        //check amount of questions
        if (questions.size() < 10 || questions.size() > 30) {
            throw new IllegalArgumentException("Количество вопросов в викторине должно быть от 10 до 30");
        }

        // устанавливаем порядок вопросов (пока просто по индексу, потом может рандомно)
        for(int i = 0; i < questions.size(); i++) {
            questions.get(i).setQuestionIndex(i + 1);
        }
        Quiz saveQuiz = quizRepository.save(quiz);

        // Обновляем статистику пользователя
        userService.incrementQuizzesCreated(creator.getUsername());

        return saveQuiz;
    }

    @Transactional
    public Quiz updateQuiz(UpdateQuizDto dto, Quiz existingQuiz, User updater) {
        // пользователь является создателем
        if (!existingQuiz.getCreator().getId().equals(updater.getId())) {
            throw new SecurityException("Только создатель может редактировать викторину");
        }

        // обновляем основные поля
        existingQuiz.setTitle(dto.getTitle());
        existingQuiz.setDescription(dto.getDescription());
        existingQuiz.setIsPublic(dto.isPublic());
        existingQuiz.setDifficultyLevel(dto.getDifficultyLevel());
        existingQuiz.setHeadImageUrl(dto.getHeadImage());
        existingQuiz.setMaxQuestions(dto.getMaxQuestions());
        existingQuiz.setTimeLimitMinutes(dto.getTimeLimitMinutes());

        // обновляем категорию
        if (dto.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(dto.getCategoryId());
            existingQuiz.setCategory(category);
        } else {
            existingQuiz.setCategory(null);
        }

        // удаляем старые вопросы
        existingQuiz.getQuestions().clear();

        // создаем и добавляем новые вопросы
        List<Question> updatedQuestions = questionService.createQuestionsFromDto(dto.getQuestions(), existingQuiz);
        existingQuiz.getQuestions().addAll(updatedQuestions);

        // проверяем количество вопросов
        if (updatedQuestions.size() < 10 || updatedQuestions.size() > 30) {
            throw new IllegalArgumentException("Количество вопросов в викторине должно быть от 10 до 30");
        }

        // устанавливаем порядок вопросов
        for (int i = 0; i < updatedQuestions.size(); i++) {
            updatedQuestions.get(i).setQuestionIndex(i + 1);
        }

        return quizRepository.save(existingQuiz);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }

    @Transactional
    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public Quiz findById(Long id) {
        Optional<Quiz> quiz = quizRepository.findById(id);
        return quiz.orElse(null);
    }

    public Quiz getPublicQuizById(Long id) {
        // Используем специальный метод репозитория для загрузки с вопросами
        Optional<Quiz> quiz = quizRepository.findByIdWithQuestions(id);
        if (quiz.isPresent() && quiz.get().isPublic()) {
            return quiz.get();
        }
        return null;
    }

    public List<Quiz> findByCreator(User creator) {
        return quizRepository.findByCreator(creator);
    }

    public Page<Quiz> findPublicQuizzes(Pageable pageable) {
        return quizRepository.findByIsPublicTrue(pageable);
    }

    public Page<Quiz> findPublicQuizzesByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryService.getCategoryById(categoryId);
        return quizRepository.findByCategoryAndIsPublicTrue(category, pageable);
    }

    public Page<Quiz> findPublicQuizzesByDifficulty(DifficultyLevel difficulty, Pageable pageable) {
        return quizRepository.findByDifficultyLevelAndIsPublicTrue(difficulty, pageable);
    }

    public Page<Quiz> searchPublicQuizzes(String search, Pageable pageable) {
        return quizRepository.searchPublicQuizzes(search, pageable);
    }

    public Page<Quiz> findPopularQuizzes(Pageable pageable) {
        return quizRepository.findPopularQuizzes(pageable);
    }

    public Page<Quiz> findTopRatedQuizzes(Pageable pageable) {
        return quizRepository.findTopRatedQuizzes(pageable);
    }

    public Page<Quiz> findPublicQuizzesByCategories(List<Long> categoryIds, Pageable pageable) {
        return quizRepository.findPublicQuizzesByCategories(categoryIds, pageable);
    }

    public Page<Quiz> findPublicQuizzesWithFilters(List<Long> categoryIds, List<DifficultyLevel> difficulties, Pageable pageable) {
        return quizRepository.findPublicQuizzesWithFilters(categoryIds, difficulties, pageable);
    }

    // Новые методы для проверки существования викторин
    public boolean existsByCategoryIdAndPublicTrue(Long categoryId) {
        return quizRepository.existsByCategoryIdAndPublicTrue(categoryId);
    }

    public long countByCategoryIdAndPublicTrue(Long categoryId) {
        return quizRepository.countByCategoryIdAndPublicTrue(categoryId);
    }

    public boolean existsByCategoryIdsAndPublicTrue(List<Long> categoryIds) {
        return quizRepository.existsByCategoryIdsAndPublicTrue(categoryIds);
    }

    @Transactional
    public void incrementPlaysCount(Long quizId) {
        Quiz quiz = findById(quizId);
        if (quiz != null) {
            quiz.setPlaysCount(quiz.getPlaysCount() + 1);
            quizRepository.save(quiz);
        }
    }

    @Transactional
    public void updateQuizRating(Long quizId, Double newRating) {
        Quiz quiz = findById(quizId);
        if (quiz != null) {
            double currentAverage = quiz.getAverageRating();
            int currentCount = quiz.getRatingCounts();

            // пересчёт рейтинга
            double newAverageRating = ((currentAverage * currentCount) + newRating) / (currentCount + 1);
            // округление до 1 знака
            quiz.setAverageRating(Math.round(newAverageRating * 10.0) / 10.0);
            quiz.setRatingCounts(currentCount + 1);

            quizRepository.save(quiz);
        }
    }

    @Transactional
    public void recordQuizPlay(String username, int score, int correctAnswers, int totalQuestions) {
        userService.incrementQuizzesPlayed(username);
        userService.addScore(username, score);

        //update correct answers stat
        for (int i = 0; i < correctAnswers; i++) {
            userService.addCorrectAnswer(username);
        }

        if (correctAnswers == totalQuestions) {
            userService.addCorrectQuestion(username);
        }
        // подсчёт среднего балла счёта
        userService.calculateAverageScore(username);
    }

    // временно для загрузки изображений
    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // директория если нет
        File directory  = new File (uploadImagesDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // генерация имени файла
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));

        }

        String filename = UUID.randomUUID().toString() + fileExtension;
        Path path = Paths.get(uploadImagesDir + filename);

        // save
        Files.copy(file.getInputStream(), path);

        return "/images/" + filename;
    }
}