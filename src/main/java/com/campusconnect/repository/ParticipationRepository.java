package com.campusconnect.repository;

import com.campusconnect.entity.Activity;
import com.campusconnect.entity.Participation;
import com.campusconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByUser(User user);
    Optional<Participation> findByUserAndActivity(User user, Activity activity);
}
