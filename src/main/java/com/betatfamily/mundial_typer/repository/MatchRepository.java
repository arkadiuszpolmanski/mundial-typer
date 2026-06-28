package com.betatfamily.mundial_typer.repository;

import com.betatfamily.mundial_typer.entity.Match;
import com.betatfamily.mundial_typer.entity.MatchRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByOrderByMatchTimeAsc();
    List<Match> findAllByMatchTimeBetweenOrderByMatchTimeAsc(LocalDateTime now, LocalDateTime in24h);
    List<Match> findTop4ByMatchTimeAfterOrderByMatchTimeAsc(LocalDateTime now);
    List<Match> findTop6ByMatchTimeAfterOrderByMatchTimeAsc(LocalDateTime now);
    List<Match> findByRoundInOrderByMatchTimeAsc(List<MatchRound> rounds);
}
