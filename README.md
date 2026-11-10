# Учебная платформа для онлайн курса

Итоговый проект «Учебная платформа для онлайн курса» по дисциплине "ORM-фреймворки для Java".

## Описание

Данный проект представляет собой web-приложение для управления онлайн-курсами на базе Spring Boot и Hibernate (JPA). Приложение позволяет создавать курсы со структурой (модули, уроки), записывать студентов, назначать задания, проводить тестирование и оценивать результаты. Проект демонстрирует работу с JPA/Hibernate, различные типы связей (1-1, 1-M, M-M), ленивую загрузку (а также проблемы с ней) и каскадные операции.

## Стек технологий

- **Java 21**.
- **Spring Boot 3.5.7**.
  - Spring Web (REST API).
  - Spring Data JPA (Hibernate).
  - Validation (для валидации и ограничений).
  - Testcontainers (для интеграционных тестов).
  - Lombok (для уменьшения boilerplate кода).
- **PostgreSQL 16** (СУБД)
- **Maven** (сборка, запуск и тестирование через `./mvnw`).

## Предусловия

- Java 21.
- Docker и Docker Compose (для PostgreSQL в dev-режиме)
- WSL2/Ubuntu (рекомендуется) или любая Unix-подобная система (PS. в теории можно запускать и на Windows, однако способ не проверялся).

## Запуск приложения (dev-профиль)

### 1. Запустить PostgreSQL

```bash
docker compose up -d
```

Это поднимет PostgreSQL 16 на `localhost:5433` с базой `gigalearn_dev` в фоновом процессе (креды для входа можно найти в `application-dev.yml` файле).

### 2. Запустить само приложение

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

При этом обязательно нужно указать профиль `dev`, ведь именно в нем у нас настроена `gigalearn_dev` база, запущенная на прошлом шаге.

При первом запуске:
- Hibernate создаст схему БД (16 таблиц).
- `DevDataLoader` загрузит демо-данные (1 курс, 2 модуля, 4 урока, тест и т.д.).
- При повторных запусках демо-данные не дублируются (проверка пустоты курсов гарантирует идемпотентность).

После запуска приложение становится доступно на `http://localhost:8080`.

### Важные настройки

- `spring.jpa.open-in-view=false` - явное управление транзакциями.
- `spring.jpa.hibernate.ddl-auto=update` (только dev профиль) - автообновление схемы.
- Демо-данные загружаются только в профиле `dev` (с помощью аннотации `@Profile("dev")`).

## Запуск тестов

```bash
./mvnw -DskipTests=false test
```

- **Написано 47 интеграционных тестов** (service + controller + application).
- Тесты используют **Testcontainers** с PostgreSQL 16-alpine.
- Профиль `test` настроен таким образом, что схема создается с нуля (`ddl-auto=create`). При этом после завершения схема, конечно, сбрасывается.
- Контейнер поднимается и останавливается автоматически (работа Testcontainers).

## REST API

Приложение предоставляет REST API для основных операций.

### Курсы и контент

**Создание курса:**
```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -d '{
  "title": "Основы Java",
  "description": "Курс для начинающих",
  "categoryId": 1,
  "teacherId": 1,
  "tagIds": [1, 2]
}'

# Ответ: 201 Created
# {
#   "id": 2,
#   "title": "Основы Java",
#   "description": "Курс для начинающих",
#   "duration": null,
#   "startDate": null,
#   "categoryName": "Программирование",
#   "teacherName": "Иван Петров",
#   "tagNames": ["Java", "ORM"]
# }
```

**Получение курса:**
```bash
curl http://localhost:8080/api/courses/1

# Ответ: 200 OK
# {
#    "id":1,
#    "title": "Основы Hibernate и JPA",
#    "description":"Изучение объектно-реляционного отображения в Java",
#    "duration":"8 недель",
#    "startDate":"2026-11-12",
#    "categoryName":"Программирование",
#    "teacherName":"Иван Петров",
#    "tagNames":["Java","ORM"]
# }
```

**Добавление модуля:**
```bash
curl -X POST http://localhost:8080/api/courses/1/modules \
  -H "Content-Type: application/json" \
  -d '{
  "title": "Модуль 3",
  "description": "Описание третьего модуля",
  "orderIndex": 3
}'

# Ответ: 201 Created
# {
#   "id": 3,
#   "title": "Модуль 3",
#   "description": "Описание третьего модуля",
#   "orderIndex": 3,
#   "courseId": 1,
#   "courseTitle": "Основы Hibernate и JPA"
# }
```

**Добавление урока:**
```bash
curl -X POST http://localhost:8080/api/modules/3/lessons \
  -H "Content-Type: application/json" \
  -d '{
  "title": "Урок 1",
  "content": "Контент урока",
  "videoUrl": "https://example.com/video"
}'

# Ответ: 201 Created
# {
#   "id": 5,
#   "title": "Урок 1",
#   "content": "Контент урока",
#   "videoUrl": "https://example.com/video",
#   "moduleId": 3,
#   "moduleTitle": "Модуль 3"
# }
```

### Запись на курс

**Записать студента:**
```bash
curl -X POST "http://localhost:8080/api/courses/1/enroll?userId=2"

# Ответ: 201 Created
# {
#   "id": 1,
#   "studentId": 2,
#   "studentName": "Анна Смирнова",
#   "courseId": 1,
#   "courseTitle": "Основы Hibernate и JPA",
#   "enrollDate": "2026-11-12",
#   "status": "Active"
# }
# При повторной попытке: 409 Conflict
```

**Отписать студента:**
```bash
curl -X DELETE "http://localhost:8080/api/courses/1/enroll?userId=2"

# Ответ: 204 No Content
```

### Задания и решения

**Создание задания:**
```bash
curl -X POST http://localhost:8080/api/lessons/1/assignments \
  -H "Content-Type: application/json" \
  -d '{
  "title": "Домашнее задание",
  "description": "Опишите концепции ORM",
  "maxScore": 100
}'

# Ответ: 201 Created
# {
#   "id": 2,
#   "title": "Домашнее задание",
#   "description": "Опишите концепции ORM",
#   "maxScore": 100,
#   "lessonId": 1,
#   "lessonTitle": "Что такое ORM?"
# }
```

**Сдача задания:**
```bash
curl -X POST http://localhost:8080/api/assignments/2/submit \
  -H "Content-Type: application/json" \
  -d '{
  "studentId": 2,
  "content": "Моё решение..."
}'

# Ответ: 201 Created
# {
#   "id": 1,
#   "studentId": 2,
#   "studentName": "Анна Смирнова",
#   "assignmentId": 2,
#   "assignmentTitle": "Домашнее задание",
#   "content": "Моё решение...",
#   "submittedAt": "2026-11-12T11:00:00Z",
#   "score": null,
#   "feedback": null
# }
# При повторной попытке: 409 Conflict
```

**Оценивание:**
```bash
curl -X POST http://localhost:8080/api/submissions/1/grade \
  -H "Content-Type: application/json" \
  -d '{
  "score": 85,
  "feedback": "Отличная работа!"
}'

# Ответ: 204 No Content
# Если score > maxScore: 400 Bad Request
```

### Квизы (тестирование знаний)

**Создание квиза:**
```bash
curl -X POST http://localhost:8080/api/modules/2/quiz \
  -H "Content-Type: application/json" \
  -d '{
  "title": "Тест по модулю",
  "timeLimitSeconds": 1800
}'

# Ответ: 201 Created
# {
#   "id": 2,
#   "title": "Тест по модулю",
#   "timeLimit": 1800,
#   "moduleId": 2,
#   "moduleTitle": "Продвинутые возможности Hibernate"
# }

# Примечание: модуль может иметь только один квиз (связь 1-1 optional).
# Для примера используется модуль 2, т.к. модуль 1 уже имеет квиз из демо-данных.
```

**Добавление вопроса:**
```bash
curl -X POST http://localhost:8080/api/quizzes/2/questions \
  -H "Content-Type: application/json" \
  -d '{
  "text": "Что такое ORM?"
}'

# Ответ: 201 Created
# {
#   "id": 3,
#   "text": "Что такое ORM?",
#   "quizId": 2,
#   "quizTitle": "Тест по модулю"
# }
```

**Добавление варианта ответа:**
```bash
curl -X POST http://localhost:8080/api/questions/3/options \
  -H "Content-Type: application/json" \
  -d '{
  "text": "Object-Relational Mapping",
  "isCorrect": true
}'

# Ответ: 201 Created
# {
#   "id": 7,
#   "text": "Object-Relational Mapping",
#   "isCorrect": true,
#   "questionId": 3,
#   "questionText": "Что такое ORM?"
# }
```

**Прохождение теста:**
```bash
curl -X POST http://localhost:8080/api/quizzes/1/take \
  -H "Content-Type: application/json" \
  -d '{
  "studentId": 2,
  "answersByQuestion": {
    "1": [1],
    "2": [5]
  }
}'

# Ответ: 201 Created
# {
#   "id": 1,
#   "studentId": 2,
#   "studentName": "Анна Смирнова",
#   "quizId": 1,
#   "quizTitle": "Тест по введению в ORM",
#   "score": 2,
#   "totalQuestions": 2,
#   "takenAt": "2026-11-12T08:00:00Z"
# }
```

Балл подсчитывается автоматически: вопрос считается верным, если выбранные варианты точно совпадают с правильными.

### Обработка ошибок

- **400 Bad Request** - ошибки валидации, не найдено.
- **409 Conflict** - нарушение уникальности (повторная запись/сдача).
- **500 Internal Server Error** - внутренние ошибки.

Пример ответа при ошибке:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-11-12T15:00:00Z",
  "fieldErrors": {
    "title": "Title is required"
  }
}
```

### Быстрый мануальный прогон

**Примечание**: Все сценарии выполняются на свежей базе данных, заполненной через `DevDataLoader` (после запуска приложения в dev-профиле).

### Сценарий 1: Создание курса с контентом

```bash
# 1. Создать курс (нужны categoryId=1, teacherId=1 из демо-данных)
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -d '{"title":"Новый курс","categoryId":1,"teacherId":1}'
# Ответ: {"id":2,"title":"Новый курс","description":null,"duration":null,"startDate":null,"categoryName":"Программирование","teacherName":"Иван Петров","tagNames":[]}
```

```bash
# 2. Добавить модуль
curl -X POST http://localhost:8080/api/courses/2/modules \
  -H "Content-Type: application/json" \
  -d '{"title":"Модуль 1","orderIndex":1}'
# Ответ: {"id":3,"title":"Модуль 1","description":null,"orderIndex":1,"courseId":2,"courseTitle":"Новый курс"}
```

```bash
# 3. Добавить урок
curl -X POST http://localhost:8080/api/modules/3/lessons \
  -H "Content-Type: application/json" \
  -d '{"title":"Урок 1","content":"Контент"}'
# Ответ: {"id":5,"title":"Урок 1","content":"Контент","videoUrl":null,"moduleId":3,"moduleTitle":"Модуль 1"}
```

### Сценарий 2: Задание и оценка

```bash
# 1. Создать задание (используем урок из демо-данных)
curl -X POST http://localhost:8080/api/lessons/1/assignments \
  -H "Content-Type: application/json" \
  -d '{"title":"Задание 1","maxScore":100}'
# Ответ: {"id":2,"title":"Задание 1","description":null,"maxScore":100,"lessonId":1,"lessonTitle":"Что такое ORM?"}
```

```bash
# 2. Студент сдает задание
curl -X POST http://localhost:8080/api/assignments/2/submit \
  -H "Content-Type: application/json" \
  -d '{"studentId":2,"content":"Мое решение"}'
# Ответ: {"id":1,"studentId":2,"studentName":"Анна Смирнова","assignmentId":2,"assignmentTitle":"Задание 1","content":"Мое решение","submittedAt":"2026-11-12T08:00:00Z","score":null,"feedback":null}
```

```bash
# 3. Оценить сдачу
curl -X POST http://localhost:8080/api/submissions/1/grade \
  -H "Content-Type: application/json" \
  -d '{"score":90,"feedback":"Хорошо!"}'
# Ответ: 204 No Content
```

### Сценарий 3: Квиз

```bash
# 1. Создать квиз для модуля (используем модуль 2, т.к. модуль 1 уже имеет квиз из демо-данных)
curl -X POST http://localhost:8080/api/modules/2/quiz \
  -H "Content-Type: application/json" \
  -d '{"title":"Тест","timeLimitSeconds":1800}'
# Ответ: {"id":2,"title":"Тест","timeLimit":1800,"moduleId":2,"moduleTitle":"Продвинутые возможности Hibernate"}
```

```bash
# 2. Добавить вопрос
curl -X POST http://localhost:8080/api/quizzes/2/questions \
  -H "Content-Type: application/json" \
  -d '{"text":"Что такое LAZY?"}'
# Ответ: {"id":3,"text":"Что такое LAZY?","quizId":2,"quizTitle":"Тест"}
```

```bash
# 3. Добавить варианты (правильный и неправильный)
curl -X POST http://localhost:8080/api/questions/3/options \
  -H "Content-Type: application/json" \
  -d '{"text":"Ленивая загрузка","isCorrect":true}'
# Ответ: {"id":7,"text":"Ленивая загрузка","isCorrect":true,"questionId":3,"questionText":"Что такое LAZY?"}

curl -X POST http://localhost:8080/api/questions/3/options \
  -H "Content-Type: application/json" \
  -d '{"text":"Немедленная загрузка","isCorrect":false}'
# Ответ: {"id":8,"text":"Немедленная загрузка","isCorrect":false,"questionId":3,"questionText":"Что такое LAZY?"}
```

```bash
# 4. Студент проходит тест (вопрос 1 из демо-данных: правильный вариант = 1)
curl -X POST http://localhost:8080/api/quizzes/1/take \
  -H "Content-Type: application/json" \
  -d '{"studentId":2,"answersByQuestion":{"1":[1],"2":[5]}}'
# Ответ: {"id":1,"studentId":2,"studentName":"Анна Смирнова","quizId":1,"quizTitle":"Тест по введению в ORM","score":2,"totalQuestions":2,"takenAt":"2025-11-12T08:00:00Z"}
```

## CI/CD

В проекте настроен GitHub Actions workflow (`.github/workflows/ci.yml`):
- Триггеры: push и pull_request на ветку `main`.
- Окружение: Ubuntu + Java 21 (Temurin) + Maven cache.
- Автоматический запуск всех тестов с Testcontainers.
- При ошибках сохраняются test reports как артефакты.

## Структура кода проекта

```
src/main/java/ru/gigalearn/
├── config/          DevDataLoader для загрузки демо-данных (для профиля dev)
├── controller/      REST контроллеры
├── dto/             Request/Response DTO с валидацией
├── entity/          JPA-сущности
├── exception/       Обработка ошибок
├── repository/      Spring Data JPA репозитории
└── service/         Бизнес-логика

src/test/java/ru/gigalearn/
├── controller/      REST-тесты с MockMvc
├── service/         Service-тесты с Testcontainers
└── GigalearnApplicationTests.java (контекстный тест).
```

**Ключевые слои:**
- **Entity** - доменная модель (User, Course, Module, Lesson, Assignment, Quiz и др.).
- **Repository** - доступ к данным через Spring Data JPA.
- **Service** - транзакционная бизнес-логика приложения.
- **Controller** - REST API эндпоинты.
- **DTO** - Data Transfer Object'ы.
- **Exception** - классы для обработки ошибок.
---
**Разработчик:** [Лаврентьев Антон]  
**Контакты:** [email: anton_lavren@mail.ru]
