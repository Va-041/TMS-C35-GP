-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(255),
                                     username VARCHAR(255) UNIQUE NOT NULL,
                                     password VARCHAR(255) NOT NULL,
                                     email VARCHAR(255),
                                     biography TEXT DEFAULT '',
                                     avatar_url VARCHAR(500) DEFAULT '/images/default-avatar.png',
                                     is_public_profile BOOLEAN DEFAULT true,
                                     receive_notifications BOOLEAN DEFAULT true,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     last_login_at TIMESTAMP
);

-- Таблица для ролей пользователей
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                          roles VARCHAR(50) NOT NULL,
                                          PRIMARY KEY (user_id, roles)
);

-- Таблица статистики пользователей
CREATE TABLE IF NOT EXISTS user_statistics (
                                               id BIGSERIAL PRIMARY KEY,
                                               user_id BIGINT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                                               total_score INTEGER DEFAULT 0,
                                               total_quizzes_created INTEGER DEFAULT 0,
                                               total_quizzes_played INTEGER DEFAULT 0,
                                               total_correct_answers INTEGER DEFAULT 0,
                                               total_correct_questions_answers INTEGER DEFAULT 0,
                                               best_score INTEGER DEFAULT 0,
                                               average_score INTEGER DEFAULT 0,
                                               win_streak INTEGER DEFAULT 0,
                                               longest_win_streak INTEGER DEFAULT 0
);

-- Таблица категорий викторин
CREATE TABLE IF NOT EXISTS quiz_categories (
                                               id BIGSERIAL PRIMARY KEY,
                                               name VARCHAR(255) NOT NULL UNIQUE,
                                               description TEXT,
                                               icon VARCHAR(255),
                                               color VARCHAR(50),
                                               active BOOLEAN DEFAULT true
);

-- Таблица викторин
CREATE TABLE IF NOT EXISTS quizzes (
                                       id BIGSERIAL PRIMARY KEY,
                                       title VARCHAR(255) NOT NULL,
                                       description VARCHAR(500) NOT NULL,
                                       creator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       is_public BOOLEAN DEFAULT true,
                                       category_id BIGINT REFERENCES quiz_categories(id),
                                       difficulty_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
                                       head_image_url VARCHAR(500),
                                       max_questions INTEGER DEFAULT 30,
                                       time_limit_minutes INTEGER,
                                       plays_count INTEGER DEFAULT 0,
                                       average_rating DOUBLE PRECISION DEFAULT 0.0,
                                       rating_counts INTEGER DEFAULT 0,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица вопросов
CREATE TABLE IF NOT EXISTS questions (
                                         id BIGSERIAL PRIMARY KEY,
                                         image_url VARCHAR(500),
                                         text VARCHAR(1000) NOT NULL,
                                         type VARCHAR(50) NOT NULL DEFAULT 'SINGLE_CHOICE',
                                         correct_text_answer VARCHAR(500),
                                         case_sensitive BOOLEAN DEFAULT false,
                                         points INTEGER DEFAULT 100,
                                         time_limit_seconds INTEGER DEFAULT 30,
                                         question_index INTEGER DEFAULT 0,
                                         quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Таблица вариантов ответов
CREATE TABLE question_options (
                                  id BIGSERIAL PRIMARY KEY,
                                  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                                  option_text VARCHAR(500),
                                  option_order INTEGER DEFAULT 0
);

-- Таблица изображений для вариантов ответов
CREATE TABLE question_option_images (
                                        id BIGSERIAL PRIMARY KEY,
                                        question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                                        option_image VARCHAR(500),
                                        option_order INTEGER DEFAULT 0
);

-- Таблица правильных ответов
CREATE TABLE question_correct_answers (
                                          id BIGSERIAL PRIMARY KEY,
                                          question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                                          correct_answer VARCHAR(500),
                                          answer_order INTEGER DEFAULT 0
);

-- Таблица рейтингов викторин
CREATE TABLE IF NOT EXISTS quiz_rating (
                                           id BIGSERIAL PRIMARY KEY,
                                           quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
                                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           rating DOUBLE PRECISION NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
                                           comment TEXT,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           UNIQUE(quiz_id, user_id)
);

-- Таблица друзей пользователей
CREATE TABLE IF NOT EXISTS user_friends (
                                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                            friend_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            PRIMARY KEY (user_id, friend_id)
);


-- Индексы для таблицы users
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Индексы для таблицы user_roles
CREATE INDEX IF NOT EXISTS idx_users_roles_user_id ON user_roles(user_id);

-- Индексы для таблицы user_statistics
CREATE INDEX IF NOT EXISTS idx_user_statistics_user_id ON user_statistics(user_id);

-- Индексы для таблицы quizzes
CREATE INDEX IF NOT EXISTS idx_quizzes_creator_id ON quizzes(creator_id);
CREATE INDEX IF NOT EXISTS idx_quizzes_category_id ON quizzes(category_id);
CREATE INDEX IF NOT EXISTS idx_quizzes_difficulty ON quizzes(difficulty_level);
CREATE INDEX IF NOT EXISTS idx_quizzes_created_at ON quizzes(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_quizzes_average_rating ON quizzes(average_rating DESC);
CREATE INDEX IF NOT EXISTS idx_quizzes_is_public ON quizzes(is_public);

-- Индексы для таблицы questions
CREATE INDEX IF NOT EXISTS idx_questions_quiz_id ON questions(quiz_id);
CREATE INDEX IF NOT EXISTS idx_questions_type ON questions(type);
CREATE INDEX IF NOT EXISTS idx_questions_question_index ON questions(question_index);

-- Индексы для таблицы question_options
CREATE INDEX IF NOT EXISTS idx_question_options_question_id ON question_options(question_id);
CREATE INDEX IF NOT EXISTS idx_question_option_images_question_id ON question_option_images(question_id);

-- Индексы для таблицы question_correct_answers
CREATE INDEX IF NOT EXISTS idx_question_correct_answers_question_id ON question_correct_answers(question_id);

-- Индексы для таблицы quiz_rating
CREATE INDEX IF NOT EXISTS idx_quiz_rating_quiz_id ON quiz_rating(quiz_id);
CREATE INDEX IF NOT EXISTS idx_quiz_rating_user_id ON quiz_rating(user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_rating_rating ON quiz_rating(rating);
CREATE INDEX IF NOT EXISTS idx_quiz_rating_created_at ON quiz_rating(created_at DESC);

-- Индексы для таблицы user_friends
CREATE INDEX IF NOT EXISTS idx_user_friends_user_id ON user_friends(user_id);
CREATE INDEX IF NOT EXISTS idx_user_friends_friend_id ON user_friends(friend_id);

