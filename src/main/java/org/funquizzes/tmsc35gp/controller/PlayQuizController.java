package org.funquizzes.tmsc35gp.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.funquizzes.tmsc35gp.entity.*;
import org.funquizzes.tmsc35gp.service.QuestionService;
import org.funquizzes.tmsc35gp.service.QuizService;
import org.funquizzes.tmsc35gp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/quizzes")
public class PlayQuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    // Мапа для хранения текущих сессий игры
    private final Map<String, GameSession> activeGameSessions = new HashMap<>();

    // детали викторины
    @GetMapping("/details/{id}")
    public String viewQuizFullDetails(@PathVariable Long id,
                                      Model model,
                                      Authentication authentication) {
        try {
            Quiz quiz = quizService.getPublicQuizById(id);

            if (quiz == null) {
                String encodedMessage = URLEncoder.encode("Викторина не найдена или недоступна", StandardCharsets.UTF_8);
                return "redirect:/quizzes?message=" + encodedMessage;
            }

            if (authentication != null && authentication.isAuthenticated()) {
                User currentUser = (User) userService.loadUserByUsername(authentication.getName());
                model.addAttribute("currentUser", currentUser);
            }

            return getQuizDetailsPage(quiz, model);

        } catch (Exception e) {
            e.printStackTrace();
            String encodedMessage = URLEncoder.encode("Ошибка при загрузке викторины", StandardCharsets.UTF_8);
            return "redirect:/quizzes?message=" + encodedMessage;
        }
    }

    // старт игры
    @GetMapping("/play/{id}")
    public String startQuiz(@PathVariable Long id,
                            Authentication authentication,
                            Model model,
                            HttpServletRequest request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                String encodedMessage = URLEncoder.encode("Для игры в викторину необходимо авторизоваться", StandardCharsets.UTF_8);
                return "redirect:/users/log-in?redirect=/quiz/play/" + id + "&message=" + encodedMessage;
            }

            Quiz quiz = quizService.getPublicQuizById(id);

            if (quiz == null) {
                String encodedMessage = URLEncoder.encode("Викторина не найдена или недоступна", StandardCharsets.UTF_8);
                return "redirect:/quizzes?message=" + encodedMessage;
            }

            User user = (User) userService.loadUserByUsername(authentication.getName());

            // создаем новую сессию игры
            String sessionId = generateSessionId(user.getId(), quiz.getId());
            GameSession gameSession = new GameSession(sessionId, user, quiz);
            activeGameSessions.put(sessionId, gameSession);

            // сохраняем sessionId в HTTP сессии
            request.getSession().setAttribute("gameSessionId", sessionId);

            System.out.println("Создана сессия игры: " + sessionId + " для пользователя: " + user.getUsername());

            // сохраняем sessionId в модели для первого вопроса
            model.addAttribute("gameSessionId", sessionId);

            return "redirect:/quizzes/play/" + id + "/question/1?session=" + sessionId;

        } catch (Exception e) {
            e.printStackTrace();
            String encodedMessage = URLEncoder.encode("Ошибка при запуске викторины", StandardCharsets.UTF_8);
            return "redirect:/quizzes?message=" + encodedMessage;
        }
    }

    // отображение вопроса
    @GetMapping("/play/{quizId}/question/{questionNumber}")
    public String showQuestion(@PathVariable Long quizId,
                               @PathVariable Integer questionNumber,
                               @RequestParam(required = false) String session,
                               HttpServletRequest request,
                               Authentication authentication,
                               Model model) {
        try {
            String sessionId = session;
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = (String) request.getSession().getAttribute("gameSessionId");
            }

            if (sessionId == null || sessionId.isEmpty()) {
                String encodedMessage = URLEncoder.encode("Сессия игры не найдена", StandardCharsets.UTF_8);
                return "redirect:/quizzes/play/" + quizId + "?message=" + encodedMessage;
            }

            GameSession gameSession = activeGameSessions.get(sessionId);

            if (gameSession == null || !gameSession.isValid()) {
                String encodedMessage = URLEncoder.encode("Сессия игры истекла или не найдена", StandardCharsets.UTF_8);
                return "redirect:/quizzes/play/" + quizId + "?message=" + encodedMessage;
            }

            // Проверяем общее время
            if (gameSession.isTotalTimeExpired()) {
                // Время викторины истекло, переходим к результатам
                return "redirect:/quizzes/play/" + quizId + "/results?session=" + sessionId + "&timeout=true";
            }

            Quiz quiz = gameSession.getQuiz();
            List<Question> questions = questionService.getQuestionsByQuizId(quizId);

            if (questions == null || questions.isEmpty()) {
                String encodedMessage = URLEncoder.encode("В этой викторине нет вопросов", StandardCharsets.UTF_8);
                return "redirect:/quizzes/play/" + quizId + "?message=" + encodedMessage;
            }

            if (questionNumber < 1 || questionNumber > questions.size()) {
                String encodedMessage = URLEncoder.encode("Вопрос не найден", StandardCharsets.UTF_8);
                return "redirect:/quizzes/play/" + quizId + "?message=" + encodedMessage;
            }

            Question currentQuestion = questions.get(questionNumber - 1);

            if (gameSession.getCurrentQuestion() == null) {
                gameSession.startQuestion(currentQuestion);
            }

            // Рассчитываем оставшееся время для вопроса
            int timeRemaining = gameSession.getTimeRemainingForQuestion();
            if (timeRemaining <= 0) {
                // Время вышло, переходим к следующему вопросу
                return handleTimeout(quizId, questionNumber, sessionId, gameSession);
            }

            // Получаем оставшееся общее время
            int totalTimeRemaining = gameSession.getRemainingTotalTimeMinutes();

            // Подготавливаем модель для отображения вопроса
            prepareQuestionModel(model, quiz, currentQuestion, questionNumber,
                    questions.size(), timeRemaining, totalTimeRemaining, sessionId);

            return "quizzes/play";

        } catch (Exception e) {
            e.printStackTrace();
            String encodedMessage = URLEncoder.encode("Ошибка при загрузке вопроса: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/quizzes?message=" + encodedMessage;
        }
    }

    // обработчик ответов на вопрос
    @PostMapping("/play/{quizId}/question/{questionNumber}")
    public String processAnswer(@PathVariable Long quizId,
                                @PathVariable Integer questionNumber,
                                @RequestParam(required = false) String session,
                                @RequestParam(required = false) String action,
                                @RequestParam Map<String, String> allParams,
                                HttpServletRequest request,
                                Authentication authentication,
                                Model model) {

        System.out.println("=== ОБРАБОТКА ОТВЕТА ===");
        System.out.println("session из параметра: " + session);

        try {
            String sessionId = session;
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = (String) request.getSession().getAttribute("gameSessionId");
                System.out.println("session из HTTP сессии: " + sessionId);
            }

            if (sessionId == null || sessionId.isEmpty()) {
                System.out.println("Сессия не найдена!");
                String encodedMessage = URLEncoder.encode("Сессия игры не найдена", StandardCharsets.UTF_8);
                return "redirect:/quizzes/play/" + quizId + "?message=" + encodedMessage;
            }

            GameSession gameSession = activeGameSessions.get(sessionId);

            if (gameSession == null || !gameSession.isValid()) {
                System.out.println("Игровая сессия не найдена или невалидна: " + sessionId);
                String encodedMessage = URLEncoder.encode("Сессия игры истекла", StandardCharsets.UTF_8);
                return "redirect:/quizzes/play/" + quizId + "?message=" + encodedMessage;
            }

            // Проверяем общее время
            if (gameSession.isTotalTimeExpired()) {
                // Время викторины истекло, переходим к результатам
                return "redirect:/quizzes/play/" + quizId + "/results?session=" + sessionId + "&timeout=true";
            }


            System.out.println("Найдена игровая сессия: " + sessionId);
            System.out.println("Пользователь: " + gameSession.getUser().getUsername());
            System.out.println("Викторина: " + gameSession.getQuiz().getId());

            Quiz quiz = gameSession.getQuiz();
            List<Question> questions = questionService.getQuestionsByQuizId(quizId);

            if (questionNumber < 1 || questionNumber > questions.size()) {
                return "redirect:/quizzes/play/" + quizId + "?message=Ошибка: вопрос не найден";
            }


            Question currentQuestion = questions.get(questionNumber - 1);

            // нажата кнопка "Пропустить"
            if ("skip".equals(action)) {
                // результат как неправильный
                gameSession.addAnswerResult(questionNumber, false, 0, Collections.emptyList());
                gameSession.completeQuestion();

                // это последний вопрос, переходим к результатам
                if (questionNumber >= questions.size()) {
                    return "redirect:/quizzes/play/" + quizId + "/results?session=" + session;
                }
                // переходим к следующему вопросу
                return "redirect:/quizzes/play/" + quizId + "/question/" + (questionNumber + 1) + "?session=" + session;
            }

            // Извлекаем ответы пользователя
            List<String> userAnswers = extractUserAnswers(allParams, currentQuestion);

            // Проверяем ответы
            boolean isCorrect = checkAnswer(currentQuestion, userAnswers);
            // Сохраняем результат
            gameSession.addAnswerResult(questionNumber, isCorrect,
                    currentQuestion.getPoints(), userAnswers);
            // Завершаем текущий вопрос
            gameSession.completeQuestion();

            // Если это последний вопрос, переходим к результатам
            if (questionNumber >= questions.size()) {
                return "redirect:/quizzes/play/" + quizId + "/results?session=" + session;
            }
            // переходим к следующему вопросу
            return "redirect:/quizzes/play/" + quizId + "/question/" + (questionNumber + 1) + "?session=" + session;

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
            String encodedMessage = URLEncoder.encode("Ошибка при обработке ответа", StandardCharsets.UTF_8);
            return "redirect:/quizzes?message=" + encodedMessage;
        }
    }

    // результаты игры
    @GetMapping("/play/{quizId}/results")
    public String showResults(@PathVariable Long quizId,
                              @RequestParam String session,
                              @RequestParam(required = false) String timeout,
                              Authentication authentication,
                              Model model,
                              HttpServletRequest request) {
        try {
            System.out.println("=== ПОКАЗ РЕЗУЛЬТАТОВ ===");
            System.out.println("Quiz ID: " + quizId);
            System.out.println("Session ID: " + session);

            GameSession gameSession = activeGameSessions.get(session);

            if (gameSession == null) {
                System.out.println("Игровая сессия не найдена!");
                String encodedMessage = URLEncoder.encode("Результаты не найдены", StandardCharsets.UTF_8);
                return "redirect:/quizzes?message=" + encodedMessage;
            }

            // информация о таймауте
            if ("true".equals(timeout)) {
                model.addAttribute("timeoutMessage", "Время викторины истекло!");
            }

            Quiz quiz = gameSession.getQuiz();
            List<Question> questions = quiz.getQuestions();

            // итоговые результаты
            int totalQuestions = questions.size();
            int correctAnswers = gameSession.getCorrectAnswersCount();
            int totalScore = gameSession.getTotalScore();
            int maxPossibleScore = questions.stream().mapToInt(Question::getPoints).sum();

            // Сохраняем результаты в статистику
            String username = authentication.getName();
            quizService.recordQuizPlay(username, totalScore, correctAnswers, totalQuestions);
            quizService.incrementPlaysCount(quizId);

            // Устанавливаем флаг, что викторина только что завершена
            request.getSession().setAttribute("quiz_completed_" + quizId, true);

            // Передаем sessionId в модель для модального окна
            model.addAttribute("sessionId", session);

            System.out.println("Session ID передан в модель: " + session);

            // Подготавливаем модель для отображения результатов
            prepareResultsModel(model, quiz, gameSession, totalQuestions,
                    correctAnswers, totalScore, maxPossibleScore);

            return "quizzes/results";

        } catch (Exception e) {
            e.printStackTrace();
            String encodedMessage = URLEncoder.encode("Ошибка при отображении результатов", StandardCharsets.UTF_8);
            return "redirect:/quizzes?message=" + encodedMessage;
        }
    }

    @GetMapping("/check-completion/{quizId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkQuizCompletion(
            @PathVariable Long quizId,
            Authentication authentication,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("completed", false);
                return ResponseEntity.ok(response);
            }

            String username = authentication.getName();
            User user = (User) userService.loadUserByUsername(username);

            // Проверяем, есть ли активная сессия для этой викторины
            String sessionId = (String) request.getSession().getAttribute("gameSessionId");
            if (sessionId != null) {
                GameSession gameSession = activeGameSessions.get(sessionId);
                if (gameSession != null && gameSession.getQuiz().getId().equals(quizId)) {
                    // Проверяем, завершена ли викторина (есть ли результаты)
                    boolean isCompleted = gameSession.getAnswerHistory() != null &&
                            !gameSession.getAnswerHistory().isEmpty();
                    response.put("completed", isCompleted);
                    response.put("sessionId", sessionId);
                    return ResponseEntity.ok(response);
                }
            }

            response.put("completed", false);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("completed", false);
            return ResponseEntity.ok(response);
        }
    }

    // Вспомогательные методы
    private String getQuizDetailsPage(Quiz quiz, Model model) {
        // Собираем статистику викторины
        Map<String, Object> quizStats = calculateQuizStats(quiz);

        // Сортируем вопросы по индексу
        List<Question> sortedQuestions = quiz.getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getQuestionIndex))
                .collect(Collectors.toList());

        // Вычисляем общее время
        int totalTime = quiz.getTimeLimitMinutes() != null ? quiz.getTimeLimitMinutes() :
                (int) Math.ceil(sortedQuestions.stream()
                        .mapToInt(q -> q.getTimeLimitSeconds() != null ? q.getTimeLimitSeconds() : 30)
                        .sum() / 60.0);

        model.addAttribute("quiz", quiz);
        model.addAttribute("quizStats", quizStats);
        model.addAttribute("questions", sortedQuestions);
        model.addAttribute("totalTime", totalTime);

        return "quizzes/details";
    }

    private Map<String, Object> calculateQuizStats(Quiz quiz) {
        Map<String, Object> stats = new HashMap<>();

        // Вычисляем среднее время прохождения
        if (quiz.getPlaysCount() != null && quiz.getPlaysCount() > 0 &&
                quiz.getTimeLimitMinutes() != null && quiz.getTimeLimitMinutes() > 0) {
            double avgCompletionTime = quiz.getTimeLimitMinutes() * 0.7;
            stats.put("avgCompletionTime", String.format("%.1f мин", avgCompletionTime));
        } else {
            stats.put("avgCompletionTime", "Нет данных");
        }

        // Процент успешных прохождений
        if (quiz.getPlaysCount() != null && quiz.getPlaysCount() > 0 &&
                quiz.getAverageRating() != null && quiz.getAverageRating() > 0) {
            double successRate = (quiz.getAverageRating() / 5.0) * 100;
            stats.put("successRate", String.format("%.0f%%", successRate));
        } else {
            stats.put("successRate", "Нет данных");
        }

        // Сложность в числовом виде
        if (quiz.getDifficultyLevel() != null) {
            switch (quiz.getDifficultyLevel()) {
                case EASY:
                    stats.put("difficultyValue", 1);
                    break;
                case MEDIUM:
                    stats.put("difficultyValue", 2);
                    break;
                case HARD:
                    stats.put("difficultyValue", 3);
                    break;
                default:
                    stats.put("difficultyValue", 2);
            }
        } else {
            stats.put("difficultyValue", 2);
        }

        return stats;
    }

    private void prepareQuestionModel(Model model, Quiz quiz, Question question,
                                      int questionNumber, int totalQuestions,
                                      int timeRemaining, int totalTimeRemaining, String sessionId) {
        model.addAttribute("quiz", quiz);
        model.addAttribute("question", question);
        model.addAttribute("questionNumber", questionNumber);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("timeRemaining", timeRemaining);
        model.addAttribute("totalTimeRemaining", totalTimeRemaining);
        model.addAttribute("gameSessionId", sessionId);

        // Для разных типов вопросов нужна разная подготовка
        if (question.getType() == QuestionType.TRUE_FALSE) {
            model.addAttribute("options", Arrays.asList("Правда", "Ложь"));
        }
    }

    private void prepareResultsModel(Model model, Quiz quiz, GameSession gameSession,
                                     int totalQuestions, int correctAnswers,
                                     int totalScore, int maxPossibleScore) {
        model.addAttribute("quiz", quiz);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("incorrectAnswers", totalQuestions - correctAnswers);
        model.addAttribute("totalScore", totalScore);
        model.addAttribute("maxPossibleScore", maxPossibleScore);
        model.addAttribute("percentage", (int) ((double) correctAnswers / totalQuestions * 100));
        model.addAttribute("scorePercentage", (int) ((double) totalScore / maxPossibleScore * 100));
        model.addAttribute("answerHistory", gameSession.getAnswerHistory());

        // Определяем сообщение в зависимости от результата
        String message;
        if (correctAnswers == totalQuestions) {
            message = "Отличный результат! Вы ответили на все вопросы правильно! 🎉";
        } else if (correctAnswers >= totalQuestions * 0.7) {
            message = "Хороший результат! Вы отлично справились! 👍";
        } else if (correctAnswers >= totalQuestions * 0.5) {
            message = "Неплохо! Есть куда стремиться! 💪";
        } else {
            message = "Попробуйте ещё раз! Уверен, в следующий раз получится лучше! 🔄";
        }
        model.addAttribute("resultMessage", message);
    }

    private List<String> extractUserAnswers(Map<String, String> allParams, Question question) {
        List<String> answers = new ArrayList<>();

        switch (question.getType()) {
            case SINGLE_CHOICE:
                String singleAnswer = allParams.get("answer");
                if (singleAnswer != null) {
                    answers.add(singleAnswer);
                }
                break;

            case MULTIPLE_CHOICE:
                for (String key : allParams.keySet()) {
                    if (key.startsWith("answer_")) {
                        answers.add(allParams.get(key));
                    }
                }
                break;

            case TRUE_FALSE:
                String tfAnswer = allParams.get("answer");
                if (tfAnswer != null) {
                    answers.add(tfAnswer.equals("true") ? "0" : "1");
                }
                break;

            case TEXT_INPUT:
                String textAnswer = allParams.get("textAnswer");
                if (textAnswer != null && !textAnswer.trim().isEmpty()) {
                    answers.add(textAnswer.trim());
                }
                break;
        }

        return answers;
    }

    private boolean checkAnswer(Question question, List<String> userAnswers) {
        if (userAnswers.isEmpty()) {
            return false;
        }

        List<String> correctAnswers = question.getCorrectAnswers();

        switch (question.getType()) {
            case SINGLE_CHOICE:
            case TRUE_FALSE:
                if (userAnswers.size() != 1) return false;
                return correctAnswers.contains(userAnswers.getFirst());

            case MULTIPLE_CHOICE:
                if (userAnswers.size() != correctAnswers.size()) return false;
                return new HashSet<>(userAnswers).containsAll(correctAnswers) &&
                        new HashSet<>(correctAnswers).containsAll(userAnswers);

            case TEXT_INPUT:
                if (userAnswers.size() != 1) return false;
                String userAnswer = userAnswers.getFirst();
                String correctAnswer = question.getCorrectTextAnswer();

                if (question.getCaseSensitive() != null && question.getCaseSensitive()) {
                    return userAnswer.equals(correctAnswer);
                } else {
                    return userAnswer.equalsIgnoreCase(correctAnswer);
                }

            default:
                return false;
        }
    }

    private String handleTimeout(Long quizId, Integer questionNumber,
                                 String session, GameSession gameSession) {
        // Сохраняем результат как неправильный (время вышло)
        gameSession.addAnswerResult(questionNumber, false, 0, Collections.emptyList());
        gameSession.completeQuestion();

        Quiz quiz = gameSession.getQuiz();
        List<Question> questions = quiz.getQuestions();

        // это последний вопрос, переходим к результатам
        if (questionNumber >= questions.size()) {
            return "redirect:/quizzes/play/" + quizId + "/results?session=" + session;
        }

        // Иначе к следующему вопросу
        return "redirect:/quizzes/play/" + quizId + "/question/" + (questionNumber + 1) + "?session=" + session;
    }

    private String generateSessionId(Long userId, Long quizId) {
        return userId + "_" + quizId + "_" + System.currentTimeMillis();
    }

    // класс для хранения сессии игры
    private static class GameSession {
        @Getter
        private final String sessionId;
        @Getter
        private final User user;
        @Getter
        private final Quiz quiz;
        private final LocalDateTime startedAt;
        @Getter
        private final int totalTimeLimitMinutes;
        private LocalDateTime quizStartedAt;
        @Getter
        private Question currentQuestion;
        private LocalDateTime questionStartedAt;

        @Getter
        private final Map<Integer, AnswerResult> answerHistory;

        public GameSession(String sessionId, User user, Quiz quiz) {
            this.sessionId = sessionId;
            this.user = user;
            this.quiz = quiz;
            this.startedAt = LocalDateTime.now();
            this.quizStartedAt = LocalDateTime.now();
            this.totalTimeLimitMinutes = calculateTotalTimeBasedOnQuestions(quiz);
            this.answerHistory = new HashMap<>();
        }

        // Метод для расчета времени на основе вопросов
        private int calculateTotalTimeBasedOnQuestions(Quiz quiz) {
            if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
                // Если вопросов нет, возвращаем значение из базы или дефолтное
                return quiz.getTimeLimitMinutes() != null ? quiz.getTimeLimitMinutes() : 10;
            }

            // Суммируем время всех вопросов
            int totalSeconds = 0;
            for (Question question : quiz.getQuestions()) {
                // Учитываем дефолтное время 30 секунд, если не установлено
                int questionTime = question.getTimeLimitSeconds() != null ? question.getTimeLimitSeconds() : 30;
                totalSeconds += questionTime;
            }

            // Конвертируем секунды в минуты (округление вверх)
            int totalMinutes = (int) Math.ceil(totalSeconds / 60.0);

            System.out.println("Рассчитано общее время викторины: " + totalSeconds + " сек = " + totalMinutes + " мин");
            System.out.println("Количество вопросов: " + quiz.getQuestions().size());
            System.out.println("Поле timeLimitMinutes из базы: " + quiz.getTimeLimitMinutes());

            return totalMinutes;
        }

        public boolean isValid() {
            // Сессия действительна 2 часа
            return Duration.between(startedAt, LocalDateTime.now()).toHours() < 2;
        }

        public void startQuestion(Question question) {
            this.currentQuestion = question;
            this.questionStartedAt = LocalDateTime.now();
        }

        public void completeQuestion() {
            this.currentQuestion = null;
            this.questionStartedAt = null;
        }

        public int getTimeRemainingForQuestion() {
            if (currentQuestion == null || questionStartedAt == null) {
                return currentQuestion != null ? currentQuestion.getTimeLimitSeconds() : 30;
            }

            long elapsedSeconds = Duration.between(questionStartedAt, LocalDateTime.now()).getSeconds();
            int timeLimit = currentQuestion.getTimeLimitSeconds();
            return Math.max(0, timeLimit - (int) elapsedSeconds);
        }

        public void addAnswerResult(int questionNumber, boolean isCorrect, int points, List<String> userAnswers) {
            AnswerResult result = new AnswerResult(questionNumber, isCorrect,
                    isCorrect ? points : 0, userAnswers);
            answerHistory.put(questionNumber, result);
        }

        public int getCorrectAnswersCount() {
            return (int) answerHistory.values().stream()
                    .filter(AnswerResult::isCorrect)
                    .count();
        }

        public int getTotalScore() {
            return answerHistory.values().stream()
                    .mapToInt(AnswerResult::getPointsEarned)
                    .sum();
        }

        public int getRemainingTotalTimeMinutes() {
            if (quizStartedAt == null) {
                return totalTimeLimitMinutes;
            }

            long elapsedMinutes = Duration.between(quizStartedAt, LocalDateTime.now()).toMinutes();
            int remaining = (int) (totalTimeLimitMinutes - elapsedMinutes);
            return Math.max(0, remaining);
        }

        public boolean isTotalTimeExpired() {
            return getRemainingTotalTimeMinutes() <= 0;
        }
    }

    public void cleanupGameSession(String sessionId) {
        activeGameSessions.remove(sessionId);
    }

    // класс для хранения результата ответа
    private static class AnswerResult {
        // Getters
        @Getter
        private final int questionNumber;
        @Getter
        private final boolean isCorrect;
        @Getter
        private final int pointsEarned;
        @Getter
        private final List<String> userAnswers;

        public AnswerResult(int questionNumber, boolean isCorrect, int pointsEarned, List<String> userAnswers) {
            this.questionNumber = questionNumber;
            this.isCorrect = isCorrect;
            this.pointsEarned = pointsEarned;
            this.userAnswers = userAnswers;
        }
    }
}