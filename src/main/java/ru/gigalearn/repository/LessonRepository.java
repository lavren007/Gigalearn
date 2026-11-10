package ru.gigalearn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigalearn.entity.Lesson;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByModuleId(Long moduleId);
}

