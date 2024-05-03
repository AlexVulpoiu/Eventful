package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.CategoryPrice;
import com.unibuc.fmi.eventful.model.ids.CategoryPriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryPriceRepository extends JpaRepository<CategoryPrice, CategoryPriceId> {

}
