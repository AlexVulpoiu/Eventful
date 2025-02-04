package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
