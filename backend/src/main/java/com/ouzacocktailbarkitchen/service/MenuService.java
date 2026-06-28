package com.ouzacocktailbarkitchen.service;

import com.ouzacocktailbarkitchen.dto.CreateMenuItemRequest;
import com.ouzacocktailbarkitchen.dto.MenuItemCategoryDto;
import com.ouzacocktailbarkitchen.dto.MenuItemDto;
import com.ouzacocktailbarkitchen.dto.UpdateMenuItemRequest;
import com.ouzacocktailbarkitchen.exception.ResourceNotFoundException;
import com.ouzacocktailbarkitchen.model.MenuItem;
import com.ouzacocktailbarkitchen.model.MenuItemCategory;
import com.ouzacocktailbarkitchen.repository.MenuItemCategoryRepository;
import com.ouzacocktailbarkitchen.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemCategoryRepository menuItemCategoryRepository;

    public List<MenuItemDto> getAllMenuItems() {
        return menuItemRepository.findAll().stream()
                .map(this::mapToMenuItemDto)
                .collect(Collectors.toList());
    }

    public List<MenuItemDto> getAvailableMenuItems() {
        return menuItemRepository.findAllByIsAvailableTrue().stream()
                .map(this::mapToMenuItemDto)
                .collect(Collectors.toList());
    }

    public List<MenuItemCategoryDto> getAllCategories() {
        return menuItemCategoryRepository.findAll().stream()
                .map(this::mapToMenuItemCategoryDto)
                .collect(Collectors.toList());
    }

    public MenuItemDto createMenuItem(CreateMenuItemRequest request) {
        MenuItemCategory category = menuItemCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        MenuItem newMenuItem = MenuItem.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .imageUrl(request.imageUrl())
                .isAvailable(request.isAvailable())
                .category(category)
                .build();

        MenuItem savedMenuItem = menuItemRepository.save(newMenuItem);
        return mapToMenuItemDto(savedMenuItem);
    }

    public MenuItemDto updateMenuItem(UUID id, UpdateMenuItemRequest request) {
        MenuItem existingMenuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id: " + id));

        existingMenuItem.setName(request.name());
        existingMenuItem.setDescription(request.description());
        existingMenuItem.setPrice(request.price());
        existingMenuItem.setImageUrl(request.imageUrl());
        existingMenuItem.setAvailable(request.isAvailable());

        // Check if category needs to be updated
        if (!existingMenuItem.getCategory().getId().equals(request.categoryId())) {
            MenuItemCategory newCategory = menuItemCategoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));
            existingMenuItem.setCategory(newCategory);
        }

        MenuItem updatedMenuItem = menuItemRepository.save(existingMenuItem);
        return mapToMenuItemDto(updatedMenuItem);
    }

    public void deleteMenuItem(UUID id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("MenuItem not found with id: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    private MenuItemDto mapToMenuItemDto(MenuItem menuItem) {
        return new MenuItemDto(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getImageUrl(),
                menuItem.getCategory().getName(),
                menuItem.isAvailable()
        );
    }

    private MenuItemCategoryDto mapToMenuItemCategoryDto(MenuItemCategory category) {
        return new MenuItemCategoryDto(
                category.getId(),
                category.getName()
        );
    }
}