package com.ouzacocktailbarkitchen.config;

import com.ouzacocktailbarkitchen.model.MenuItem;
import com.ouzacocktailbarkitchen.model.MenuItemCategory;
import com.ouzacocktailbarkitchen.repository.MenuItemCategoryRepository;
import com.ouzacocktailbarkitchen.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order; // Correct import for @Order annotation
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID; // Needed for passing null to UUID id fields in constructors

@Component
@RequiredArgsConstructor
@Order(2)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MenuItemRepository menuItemRepository;
    private final MenuItemCategoryRepository menuItemCategoryRepository;

    @Override
    public void run(String... args) {
        if (menuItemCategoryRepository.count() > 0) {
            log.info("Menu data already exists. Skipping seeding.");
            return;
        }

        // Create and save MenuItemCategory instances
        // Replaced builder() calls with AllArgsConstructor calls due to "cannot find symbol method builder()" errors.
        // The 'id' field is @GeneratedValue, so passing null for new entities is appropriate.
        MenuItemCategory mezzeCategory = new MenuItemCategory(null, "Mezze & Appetizers");
        menuItemCategoryRepository.save(mezzeCategory);

        MenuItemCategory grillCategory = new MenuItemCategory(null, "From the Grill");
        menuItemCategoryRepository.save(grillCategory);

        MenuItemCategory mainCoursesCategory = new MenuItemCategory(null, "Main Courses");
        menuItemCategoryRepository.save(mainCoursesCategory);

        MenuItemCategory dessertsCategory = new MenuItemCategory(null, "Desserts");
        menuItemCategoryRepository.save(dessertsCategory);

        MenuItemCategory signatureCocktailsCategory = new MenuItemCategory(null, "Signature Cocktails");
        menuItemCategoryRepository.save(signatureCocktailsCategory);

        MenuItemCategory classicCocktailsCategory = new MenuItemCategory(null, "Classic Cocktails");
        menuItemCategoryRepository.save(classicCocktailsCategory);

        // Create and save MenuItem instances
        // Replaced builder() calls with AllArgsConstructor calls due to "cannot find symbol method builder()" errors.
        // The 'id' field is @GeneratedValue, so passing null for new entities is appropriate.
        menuItemRepository.save(new MenuItem(
                null, // id
                "Hummus",
                "Creamy chickpea dip with tahini, lemon, and garlic.",
                new BigDecimal("8.00"),
                null, // imageUrl
                true, // isAvailable
                mezzeCategory
        ));
        menuItemRepository.save(new MenuItem(
                null, // id
                "Falafel",
                "Crispy fried chickpea patties with herbs and spices.",
                new BigDecimal("9.50"),
                null, // imageUrl
                true, // isAvailable
                mezzeCategory
        ));

        menuItemRepository.save(new MenuItem(
                null, // id
                "Chicken Souvlaki",
                "Marinated chicken skewers grilled to perfection.",
                new BigDecimal("18.00"),
                null, // imageUrl
                true, // isAvailable
                grillCategory
        ));
        menuItemRepository.save(new MenuItem(
                null, // id
                "Lamb Kebab",
                "Spiced ground lamb grilled on a skewer.",
                new BigDecimal("22.00"),
                null, // imageUrl
                true, // isAvailable
                grillCategory
        ));

        menuItemRepository.save(new MenuItem(
                null, // id
                "Moussaka",
                "Layered eggplant, spiced meat, and béchamel sauce.",
                new BigDecimal("24.00"),
                null, // imageUrl
                true, // isAvailable
                mainCoursesCategory
        ));
        menuItemRepository.save(new MenuItem(
                null, // id
                "Pan-Seared Sea Bass",
                "With lemon-caper sauce and roasted vegetables.",
                new BigDecimal("28.00"),
                null, // imageUrl
                true, // isAvailable
                mainCoursesCategory
        ));

        menuItemRepository.save(new MenuItem(
                null, // id
                "Baklava",
                "Rich, sweet pastry with layers of filo, nuts, and honey.",
                new BigDecimal("7.00"),
                null, // imageUrl
                true, // isAvailable
                dessertsCategory
        ));

        menuItemRepository.save(new MenuItem(
                null, // id
                "Aegean Sunset",
                "Ouzo, aperol, passion fruit, and lime.",
                new BigDecimal("15.00"),
                null, // imageUrl
                true, // isAvailable
                signatureCocktailsCategory
        ));
        menuItemRepository.save(new MenuItem(
                null, // id
                "Spartan's Spirit",
                "Metaxa brandy, honey syrup, and bitters.",
                new BigDecimal("16.00"),
                null, // imageUrl
                true, // isAvailable
                signatureCocktailsCategory
        ));

        menuItemRepository.save(new MenuItem(
                null, // id
                "Negroni",
                "Gin, Campari, sweet vermouth.",
                new BigDecimal("14.00"),
                null, // imageUrl
                true, // isAvailable
                classicCocktailsCategory
        ));

        log.info("Database seeded with initial menu data.");
    }
}