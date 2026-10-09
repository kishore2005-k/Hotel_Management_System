package com.hotel.services;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class IndianMenu {
    private static final Map<String, List<String>> CATEGORIES;
    private static final Set<String> ITEM_NAMES;

    static {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        categories.put("Breakfast", List.of("Poha", "Upma"));
        categories.put("South Indian Specials", List.of(
                "Idli with sambar", "Masala dosa", "Medu vada", "Vegetable uttapam"));
        categories.put("North Indian Meals", List.of(
                "Aloo paratha", "Puri bhaji", "Chole bhature", "Pav bhaji",
                "Butter naan", "Tandoori roti", "Jeera rice", "Steamed basmati rice",
                "Raita", "Fresh salad"));
        categories.put("Vegetarian Dishes", List.of(
                "Samosa", "Paneer tikka", "Vegetable pakora", "Hara bhara kebab",
                "Paneer butter masala", "Palak paneer", "Dal tadka", "Chana masala",
                "Vegetable biryani", "Vegetarian thali"));
        categories.put("Non-Vegetarian Dishes", List.of(
                "Chicken tikka", "Tandoori chicken", "Chicken 65", "Fish fry",
                "Butter chicken", "Chicken biryani", "Mutton rogan josh",
                "Fish curry", "Egg curry"));
        categories.put("Beverages", List.of(
                "Masala chai", "Filter coffee", "Sweet lassi", "Nimbu pani",
                "Gulab jamun", "Rasmalai", "Kheer", "Kulfi"));
        CATEGORIES = Collections.unmodifiableMap(categories);
        ITEM_NAMES = categories.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    private IndianMenu() {
    }

    public static Map<String, List<String>> getCategories() {
        return CATEGORIES;
    }

    public static Set<String> getItemNames() {
        return ITEM_NAMES;
    }
}
