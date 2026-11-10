package ru.gigalearn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigalearn.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}

