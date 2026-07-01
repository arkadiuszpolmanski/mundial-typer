package com.betatfamily.mundial_typer.repository;

import com.betatfamily.mundial_typer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    List<User> findAllByOrderByTotalPointsDescCorrect3DescCorrect1Desc();
    List<User> findAllByOrderByFirstNameAscLastNameAsc();
}
