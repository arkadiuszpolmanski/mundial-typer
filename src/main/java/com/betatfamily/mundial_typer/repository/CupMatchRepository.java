package com.betatfamily.mundial_typer.repository;

import com.betatfamily.mundial_typer.entity.CupMatch;
import com.betatfamily.mundial_typer.entity.CupStage;
import com.betatfamily.mundial_typer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CupMatchRepository extends JpaRepository<CupMatch, Long> {

    List<CupMatch> findByStageOrderByBracketPosition(CupStage stage);
    List<CupMatch> findByPlayer1OrPlayer2(User user, User user1);
}
