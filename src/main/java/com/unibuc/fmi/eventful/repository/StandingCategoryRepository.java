package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.StandingCategory;
import com.unibuc.fmi.eventful.model.ids.StandingCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandingCategoryRepository extends JpaRepository<StandingCategory, StandingCategoryId> {

}
