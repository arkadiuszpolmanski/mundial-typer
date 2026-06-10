package com.betatfamily.mundial_typer.repository;

import com.betatfamily.mundial_typer.entity.TournamentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<TournamentConfig, Long> {
}
