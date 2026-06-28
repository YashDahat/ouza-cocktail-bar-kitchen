package com.ouzacocktailbarkitchen.repository;

import com.ouzacocktailbarkitchen.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByCategoryName(String categoryName);
    List<MenuItem> findAllByIsAvailableTrue();
}