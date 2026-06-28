package com.ouzacocktailbarkitchen.controller;

import com.ouzacocktailbarkitchen.dto.CreateMenuItemRequest;
import com.ouzacocktailbarkitchen.dto.MenuItemDto;
import com.ouzacocktailbarkitchen.dto.UpdateMenuItemRequest;
import com.ouzacocktailbarkitchen.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.ouzacocktailbarkitchen.controller.AdminMenuController;

@RestController
@RequestMapping("/api/v1/admin/menu")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuItemDto>> getAllMenuItemsAdmin() {
        List<MenuItemDto> menuItems = menuService.getAllMenuItems();
        return new ResponseEntity<>(menuItems, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MenuItemDto> createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {
        MenuItemDto createdMenuItem = menuService.createMenuItem(request);
        return new ResponseEntity<>(createdMenuItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemDto> updateMenuItem(@PathVariable UUID id, @Valid @RequestBody UpdateMenuItemRequest request) {
        MenuItemDto updatedMenuItem = menuService.updateMenuItem(id, request);
        return new ResponseEntity<>(updatedMenuItem, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable UUID id) {
        menuService.deleteMenuItem(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}