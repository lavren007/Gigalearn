package ru.gigalearn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigalearn.entity.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizId(Long quizId);
}

