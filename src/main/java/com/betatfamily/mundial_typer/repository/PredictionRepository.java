package com.betatfamily.mundial_typer.repository;

import com.betatfamily.mundial_typer.entity.Prediction;
import com.betatfamily.mundial_typer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    List<Prediction> findByUserUsername(User user);

    List<Prediction> findByMatchId(Long matchId);

    Optional<Prediction> findByUserUsernameAndMatchId(String username, Long matchId);
}
