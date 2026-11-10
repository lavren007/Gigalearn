package ru.gigalearn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigalearn.entity.QuizSubmission;

import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    List<QuizSubmission> findByQuizId(Long quizId);

    List<QuizSubmission> findByStudentId(Long studentId);
}

