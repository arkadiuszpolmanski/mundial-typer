package com.betatfamily.mundial_typer.repository;

import com.betatfamily.mundial_typer.entity.CupSeed;
import com.betatfamily.mundial_typer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CupSeedRepository extends JpaRepository<CupSeed, Long> {

    List<CupSeed> findAllByOrderBySeedAsc();

    boolean existsByUser(User user);
}
