package com.betatfamily.mundial_typer.repository;

import com.betatfamily.mundial_typer.entity.CupMatch;
import com.betatfamily.mundial_typer.entity.CupStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CupMatchRepository extends JpaRepository<CupMatch, Long> {

    List<CupMatch> findByStageOrderByBracketPosition(CupStage stage);
}
