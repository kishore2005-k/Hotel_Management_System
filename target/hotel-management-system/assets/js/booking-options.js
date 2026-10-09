"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const dialog = document.querySelector("#find-room-food-dialog");
    const foodInputs = [...document.querySelectorAll("[data-food-item]")];
    const closeButtons = document.querySelectorAll(
        ".find-room-food-dialog-close, .find-room-food-close-button"
    );
    const confirmButton = document.querySelector(".find-room-food-confirm-button");
    const selectionStatus = document.querySelector(".find-room-food-dialog-selection");
    const itemIllustrations = {
        "Idli with sambar": "🥥", "Masala dosa": "🥞", "Medu vada": "🍩", "Poha": "🍋",
        "Upma": "🥣", "Aloo paratha": "🫓", "Puri bhaji": "🥘", "Chole bhature": "🍛",
        "Vegetable uttapam": "🥬", "Pav bhaji": "🍲", "Samosa": "🥟", "Paneer tikka": "🧀",
        "Vegetable pakora": "🧅", "Hara bhara kebab": "🌿", "Paneer butter masala": "🍛",
        "Palak paneer": "🥬", "Dal tadka": "🫘", "Chana masala": "🫘",
        "Vegetable biryani": "🍚", "Vegetarian thali": "🍽️", "Chicken tikka": "🍢",
        "Tandoori chicken": "🍗", "Chicken 65": "🍗", "Fish fry": "🐟",
        "Butter chicken": "🍛", "Chicken biryani": "🍚", "Mutton rogan josh": "🍲",
        "Fish curry": "🐟", "Egg curry": "🥚", "Butter naan": "🫓",
        "Tandoori roti": "🫓", "Jeera rice": "🍚", "Steamed basmati rice": "🍚",
        "Raita": "🥣", "Fresh salad": "🥗", "Gulab jamun": "🍮", "Rasmalai": "🍨",
        "Kheer": "🍚", "Kulfi": "🍦", "Masala chai": "☕", "Filter coffee": "☕",
        "Sweet lassi": "🥛", "Nimbu pani": "🍋"
    };
    let activeForm = null;
    const selectionsByForm = new WeakMap();

    if (!dialog) {
        return;
    }

    const selectedItems = () => foodInputs.filter((input) => input.checked);

    const selectionFor = (form) => {
        if (!selectionsByForm.has(form)) {
            const container = form.querySelector(".find-room-selected-food-inputs");
            const values = [...(container?.querySelectorAll('input[name="foodItems"]') || [])]
                .map((input) => input.value);
            selectionsByForm.set(form, new Set(values));
        }
        return selectionsByForm.get(form);
    };

    const updateMenuSelection = () => {
        const selected = selectedItems();
        foodInputs.forEach((input) => {
            const card = input.closest(".find-room-food-item");
            const illustration = card?.querySelector(".find-room-food-item-icon");
            if (illustration && itemIllustrations[input.value]) {
                illustration.textContent = itemIllustrations[input.value];
            }
            card?.classList.toggle("is-selected", input.checked);
        });
        if (selectionStatus) {
            selectionStatus.textContent = selected.length
                ? `${selected.length} sample ${selected.length === 1 ? "item" : "items"} selected`
                : "No sample items selected";
        }
    };

    const closeMenu = () => {
        if (dialog.open) {
            dialog.close();
        }
        document.body.classList.remove("has-food-menu-open");
    };

    const showFoodError = (form, message) => {
        const error = form.querySelector(".find-room-food-error");
        if (error) {
            error.textContent = message;
            error.hidden = false;
        }
    };

    const clearFoodError = (form) => {
        const error = form.querySelector(".find-room-food-error");
        if (error) {
            error.textContent = "";
            error.hidden = true;
        }
    };

    const attachSelectedItems = (form) => {
        const container = form.querySelector(".find-room-selected-food-inputs");
        if (!container) {
            return;
        }
        container.replaceChildren();
        selectedItems().forEach((item) => {
            const hiddenInput = document.createElement("input");
            hiddenInput.type = "hidden";
            hiddenInput.name = "foodItems";
            hiddenInput.value = item.value;
            container.append(hiddenInput);
        });
    };

    const openMenu = (form) => {
        if (!form) {
            return;
        }
        activeForm = form;
        clearFoodError(form);
        const selection = selectionFor(form);
        foodInputs.forEach((input) => {
            input.checked = selection.has(input.value);
        });
        updateMenuSelection();
        document.body.classList.add("has-food-menu-open");
        if (!dialog.open) {
            dialog.showModal();
        }
    };

    document.querySelectorAll(".find-room-booking-form").forEach((form) => {
        const radios = [...form.querySelectorAll('input[type="radio"][name="foodRequested"]')];
        const options = [...form.querySelectorAll(".find-room-food-option")];
        const editButton = form.querySelector("[data-open-food-menu]");
        const summary = form.querySelector(".find-room-food-summary");

        const updateFoodChoice = () => {
            const selectedChoice = radios.find((radio) => radio.checked);
            options.forEach((option) => {
                const radio = option.querySelector('input[name="foodRequested"]');
                option.classList.toggle("is-selected", Boolean(radio?.checked));
            });
            if (!selectedChoice) {
                return;
            }
            const wantsFood = selectedChoice.value === "yes";
            if (editButton) {
                editButton.hidden = !wantsFood;
            }
            if (!wantsFood) {
                if (activeForm === form) {
                    closeMenu();
                }
                selectionsByForm.set(form, new Set());
                foodInputs.forEach((input) => {
                    input.checked = false;
                });
                attachSelectedItems(form);
                if (summary) {
                    summary.textContent = "";
                }
                clearFoodError(form);
                updateMenuSelection();
            } else {
                openMenu(form);
            }
        };

        radios.forEach((radio) => radio.addEventListener("change", updateFoodChoice));
        editButton?.addEventListener("click", () => openMenu(form));
        form.addEventListener("submit", (event) => {
            const wantsFood = radios.find((radio) => radio.checked)?.value === "yes";
            if (wantsFood && !form.querySelector(".find-room-selected-food-inputs input[name='foodItems']")) {
                event.preventDefault();
                showFoodError(form, "Choose at least one menu suggestion, then confirm your food request.");
                openMenu(form);
            }
        });

        updateFoodChoiceInitial(form, radios, options, editButton);
    });

    foodInputs.forEach((input) => input.addEventListener("change", () => {
        if (dialog.open && activeForm) {
            selectionsByForm.set(activeForm, new Set(selectedItems().map((item) => item.value)));
        }
        updateMenuSelection();
    }));

    closeButtons.forEach((button) => button.addEventListener("click", closeMenu));
    dialog.addEventListener("close", () => {
        document.body.classList.remove("has-food-menu-open");
    });
    dialog.addEventListener("cancel", () => {
        document.body.classList.remove("has-food-menu-open");
    });
    dialog.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            event.preventDefault();
            closeMenu();
        }
    });
    dialog.addEventListener("click", (event) => {
        if (event.target === dialog) {
            closeMenu();
        }
    });

    confirmButton?.addEventListener("click", () => {
        const selected = selectedItems();
        if (!activeForm) {
            closeMenu();
            return;
        }
        if (!selected.length) {
            showFoodError(activeForm, "Select at least one sample menu item to add a food request.");
            selectionStatus?.focus();
            return;
        }
        selectionsByForm.set(activeForm, new Set(selected.map((item) => item.value)));
        attachSelectedItems(activeForm);
        const summary = activeForm.querySelector(".find-room-food-summary");
        if (summary) {
            summary.textContent = `Food request added — ${selected.length} sample ${selected.length === 1 ? "item" : "items"} selected. Availability and pricing will be confirmed by reception.`;
        }
        const editButton = activeForm.querySelector("[data-open-food-menu]");
        if (editButton) {
            editButton.textContent = "Edit menu selection";
        }
        clearFoodError(activeForm);
        closeMenu();
        activeForm.querySelector('input[name="foodRequested"][value="yes"]')
            ?.focus({ preventScroll: true });
    });

    updateMenuSelection();
});

function updateFoodChoiceInitial(form, radios, options, editButton) {
    const selectedChoice = radios.find((radio) => radio.checked);
    options.forEach((option) => {
        const radio = option.querySelector('input[name="foodRequested"]');
        option.classList.toggle("is-selected", Boolean(radio?.checked));
    });
    if (editButton) {
        editButton.hidden = selectedChoice?.value !== "yes";
    }
}
