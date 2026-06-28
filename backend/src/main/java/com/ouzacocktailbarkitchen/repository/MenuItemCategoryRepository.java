package com.ouzacocktailbarkitchen.repository;

import com.ouzacocktailbarkitchen.model.MenuItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuItemCategoryRepository extends JpaRepository<MenuItemCategory, UUID> {
}