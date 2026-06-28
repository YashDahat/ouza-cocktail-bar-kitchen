package com.ouzacocktailbarkitchen.controller;

import com.ouzacocktailbarkitchen.dto.MenuItemCategoryDto;
import com.ouzacocktailbarkitchen.dto.MenuItemDto;
import com.ouzacocktailbarkitchen.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.ouzacocktailbarkitchen.controller.MenuController;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuItemDto>> getPublicMenu() {
        List<MenuItemDto> menuItems = menuService.getAvailableMenuItems();
        return new ResponseEntity<>(menuItems, HttpStatus.OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<MenuItemCategoryDto>> getMenuCategories() {
        List<MenuItemCategoryDto> categories = menuService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}