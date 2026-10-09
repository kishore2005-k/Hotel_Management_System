"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const searchForm = document.querySelector("#find-room-search-form");
    const checkInInput = document.querySelector("#find-room-check-in");
    const checkOutInput = document.querySelector("#find-room-check-out");
    const searchMessage = document.querySelector("#find-room-search-message");
    const priceInput = document.querySelector("#find-room-price-max");
    const priceOutput = document.querySelector("#find-room-price-value");
    const resultsGrid = document.querySelector("#find-room-results-grid");
    const detailsDialog = document.querySelector("#find-room-details-dialog");
    const results = [...document.querySelectorAll(".find-room-result-card")];
    const amenityLabels = {
        "air-conditioning": "Air conditioning",
        "free-wifi": "Free Wi-Fi",
        "smart-tv": "Smart TV",
        "private-bathroom": "Private bathroom",
        "work-desk": "Work desk",
        "seating-area": "Seating area",
        "living-area": "Separate living area",
        "scenic-view": "Garden / scenic view"
    };
    const query = new URLSearchParams(window.location.search);
    const selectedAmenities = query.getAll("amenity");
    document.querySelectorAll(".find-room-search-form input[name='amenity']").forEach((input) => {
        input.checked = selectedAmenities.includes(input.value);
    });

    const parseDate = (value) => {
        if (!value) {
            return null;
        }
        const date = new Date(`${value}T00:00:00`);
        return Number.isNaN(date.getTime()) ? null : date;
    };

    const showSearchMessage = (message) => {
        if (searchMessage) {
            searchMessage.textContent = message;
        }
    };

    const updateCheckOutMinimum = () => {
        if (checkInInput && checkOutInput) {
            checkOutInput.min = checkInInput.value || checkInInput.min;
        }
    };

    checkInInput?.addEventListener("change", updateCheckOutMinimum);
    updateCheckOutMinimum();

    searchForm?.addEventListener("submit", (event) => {
        const checkIn = parseDate(checkInInput.value);
        const checkOut = parseDate(checkOutInput.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (!checkIn || !checkOut || checkIn < today || checkOut <= checkIn) {
            event.preventDefault();
            showSearchMessage("Choose a check-in date today or later and a check-out date after check-in.");
            (checkIn && checkIn >= today ? checkOutInput : checkInInput).focus();
            return;
        }

        const button = searchForm.querySelector("#find-room-search-button");
        if (button) {
            button.disabled = true;
            button.setAttribute("aria-busy", "true");
            button.textContent = "Checking availability…";
        }
        showSearchMessage("Checking live availability for your stay…");
    });

    priceInput?.addEventListener("input", () => {
        if (priceOutput) {
            priceOutput.value = Number(priceInput.value).toLocaleString("en-IN");
        }
        filterResults();
    });
    if (priceInput && priceOutput) {
        priceOutput.value = Number(priceInput.value).toLocaleString("en-IN");
    }

    const getSelectedAmenities = () => [
        ...document.querySelectorAll('.find-room-search-form input[name="amenity"]:checked')
    ].map((input) => input.value);

    const getFilterValues = () => ({
        category: document.querySelector("#find-room-category-filter")?.value || "any",
        bed: document.querySelector("#find-room-bed-filter")?.value || "any",
        ac: document.querySelector("#find-room-ac-filter")?.value || "any",
        price: Number(priceInput?.value || Number.MAX_SAFE_INTEGER),
        guests: Number(document.querySelector("#find-room-adults")?.value || 1)
            + Number(document.querySelector("#find-room-children")?.value || 0),
        requestedRooms: Number(document.querySelector("#find-room-count")?.value || 1),
        amenities: getSelectedAmenities(),
        sort: document.querySelector("#find-room-sort")?.value || "recommended"
    });

    function filterResults() {
        if (!resultsGrid) {
            return;
        }

        const filters = getFilterValues();
        const matches = results.filter((card) => {
            const cardAmenities = card.dataset.amenities.split(/\s+/);
            return (filters.category === "any" || card.dataset.category === filters.category)
                && (filters.bed === "any" || card.dataset.bed === filters.bed)
                && (filters.ac === "any" || card.dataset.ac === String(filters.ac === "ac"))
                && Number(card.dataset.rate) <= filters.price
                && Number(card.dataset.capacity) >= filters.guests
                && filters.amenities.every((amenity) => cardAmenities.includes(amenity));
        });

        matches.sort((first, second) => {
            const firstRate = Number(first.dataset.rate);
            const secondRate = Number(second.dataset.rate);
            if (filters.sort === "price-low") {
                return firstRate - secondRate;
            }
            if (filters.sort === "price-high") {
                return secondRate - firstRate;
            }
            const firstRecommended = (first.dataset.ac === "true" ? 0 : 1) * 100000 + firstRate;
            const secondRecommended = (second.dataset.ac === "true" ? 0 : 1) * 100000 + secondRate;
            return firstRecommended - secondRecommended;
        });

        results.forEach((card) => {
            card.hidden = true;
        });

        const enoughRooms = matches.length >= filters.requestedRooms;
        matches.forEach((card) => {
            card.hidden = !enoughRooms;
            resultsGrid.append(card);
        });

        const emptyState = document.querySelector("#find-room-empty-state");
        const count = document.querySelector("#find-room-results-count");
        const message = document.querySelector("#find-room-filter-message");
        if (emptyState) {
            emptyState.hidden = enoughRooms && matches.length > 0;
        }
        if (count) {
            count.textContent = enoughRooms
                ? `${matches.length} ${matches.length === 1 ? "room" : "rooms"} available`
                    + (filters.requestedRooms > 1 ? ` · ${filters.requestedRooms} requested` : "")
                : `${matches.length} matching ${matches.length === 1 ? "room" : "rooms"}`;
        }
        if (message) {
            message.textContent = matches.length > 0 && !enoughRooms
                ? `Only ${matches.length} room${matches.length === 1 ? "" : "s"} match these filters for your dates. Reduce the number of rooms or broaden your search. Each booking request is for one room.`
                : enoughRooms && filters.requestedRooms > 1
                    ? `You requested ${filters.requestedRooms} rooms. Select a room and submit a separate reservation request for each; each request is for one room.`
                    : "";
        }
    }

    document.querySelectorAll(
        "#find-room-category-filter, #find-room-bed-filter, #find-room-ac-filter, #find-room-adults, #find-room-children, #find-room-count, #find-room-sort, .find-room-search-form input[name='amenity']"
    ).forEach((control) => {
        control.addEventListener("change", filterResults);
    });

    const categoryDetails = {
        single: {
            category: "single",
            title: "Single Room",
            bed: "Single bed",
            capacity: "1 guest",
            description: "A calm, well-appointed retreat with a single bed, work desk, private bathroom, Smart TV, and free Wi-Fi. Air conditioning is available in AC-configured rooms; garden or forest views depend on the room.",
            photo: `${document.querySelector(".find-room-category-card img")?.src || ""}`,
            amenities: ["free-wifi", "smart-tv", "private-bathroom", "work-desk", "scenic-view"]
        },
        double: {
            category: "double",
            title: "Double Room",
            bed: "Queen-size bed",
            capacity: "Up to 2 guests",
            description: "A restful double room with a queen-size bed, seating area, private bathroom, Smart TV, and free Wi-Fi. Air conditioning is available in AC-configured rooms; garden or mountain views depend on the room.",
            photo: document.querySelectorAll(".find-room-category-card img")[1]?.src || "",
            amenities: ["free-wifi", "smart-tv", "private-bathroom", "seating-area", "scenic-view"]
        },
        suite: {
            category: "suite",
            title: "Luxury Suite",
            bed: "King-size bed",
            capacity: "Up to 4 guests",
            description: "A generous suite with a king-size bed, separate living area, premium bathroom, Smart TV, and free Wi-Fi. Air conditioning is available in AC-configured rooms; garden or balcony views are subject to availability.",
            photo: document.querySelectorAll(".find-room-category-card img")[2]?.src || "",
            amenities: ["free-wifi", "smart-tv", "private-bathroom", "living-area", "scenic-view"]
        }
    };

    const openDetails = (details, roomNumber = "Category overview", rate = "", roomId = "") => {
        if (!detailsDialog || !details) {
            return;
        }
        detailsDialog.querySelector("#find-room-dialog-title").textContent = details.title;
        detailsDialog.querySelector(".find-room-dialog-description").textContent = details.description;
        detailsDialog.querySelector(".find-room-dialog-bed").textContent = details.bed;
        detailsDialog.querySelector(".find-room-dialog-capacity").textContent = details.capacity;
        detailsDialog.querySelector(".find-room-dialog-number").textContent = roomNumber;
        const photo = detailsDialog.querySelector(".find-room-dialog-photo");
        photo.src = details.photo;
        photo.alt = `${details.title} at Whispering Pines Lodge`;
        const amenityList = detailsDialog.querySelector(".find-room-dialog-amenities");
        amenityList.replaceChildren();
        details.amenities.forEach((amenity) => {
            const item = document.createElement("span");
            item.textContent = amenityLabels[amenity] || amenity;
            amenityList.append(item);
        });
        const dialogAction = detailsDialog.querySelector(".find-room-dialog-action");
        dialogAction.textContent = rate ? `Choose this room · ₹${rate} / night` : "Choose this room";
        dialogAction.dataset.category = details.category || "";
        dialogAction.dataset.roomNumber = roomNumber;
        dialogAction.dataset.roomRate = rate;
        dialogAction.dataset.roomId = roomId;
        detailsDialog.showModal();
    };

    document.querySelectorAll("[data-room-details]").forEach((button) => {
        button.addEventListener("click", () => {
            const category = categoryDetails[button.dataset.roomDetails];
            openDetails(category);
        });
    });

    document.querySelectorAll("[data-view-room]").forEach((button) => {
        button.addEventListener("click", () => {
            const card = button.closest(".find-room-result-card");
            const details = {
                title: card.dataset.roomTitle,
                bed: card.dataset.bedLabel,
                capacity: `Up to ${card.dataset.capacity} guests`,
                description: card.dataset.roomDescription,
                photo: card.dataset.roomPhoto,
                category: card.dataset.category,
                amenities: card.dataset.amenities.split(/\s+/)
            };
            openDetails(details, card.dataset.roomNumber,
                Number(card.dataset.rate).toLocaleString("en-IN"), card.dataset.roomId);
        });
    });

    document.querySelector(".find-room-dialog-close")?.addEventListener("click", () => {
        detailsDialog?.close();
    });

    const selectCategory = (category) => {
        const categoryFilter = document.querySelector("#find-room-category-filter");
        if (categoryFilter) {
            categoryFilter.value = category;
        }
        const checkIn = parseDate(checkInInput?.value);
        const checkOut = parseDate(checkOutInput?.value);
        if (checkIn && checkOut && checkOut > checkIn) {
            searchForm.requestSubmit();
            return;
        }
        showSearchMessage("Choose your check-in and check-out dates to see live availability for this room.");
        document.querySelector("#availability-panel")?.scrollIntoView({ behavior: "smooth", block: "start" });
        checkInInput?.focus({ preventScroll: true });
    };

    document.querySelectorAll("[data-book-category]").forEach((button) => {
        button.addEventListener("click", () => selectCategory(button.dataset.bookCategory));
    });

    detailsDialog?.querySelector(".find-room-dialog-action")?.addEventListener("click", (event) => {
        const category = event.currentTarget.dataset.category;
        const roomId = event.currentTarget.dataset.roomId;
        const roomNumber = event.currentTarget.dataset.roomNumber;
        detailsDialog.close();
        if (roomId) {
            const card = results.find((item) => item.dataset.roomId === roomId
                && item.dataset.roomNumber === roomNumber);
            card?.querySelector("[data-book-room]")?.click();
            return;
        }
        if (category) {
            selectCategory(category);
            return;
        }
    });

    document.querySelectorAll("[data-book-room]").forEach((button) => {
        button.addEventListener("click", () => {
            const card = button.closest(".find-room-result-card");
            const form = card.querySelector(".find-room-booking-form");
            if (!form) {
                return;
            }
            form.hidden = false;
            form.querySelector("[name='customerName']")?.focus({ preventScroll: true });
            form.scrollIntoView({ behavior: "smooth", block: "nearest" });
        });
    });

    document.querySelectorAll(".find-room-booking-form").forEach((form) => {
        form.addEventListener("submit", (event) => {
            const checkInValue = form.querySelector("[name='checkIn']").value;
            const checkOutValue = form.querySelector("[name='checkOut']").value;
            const checkIn = parseDate(checkInValue);
            const checkOut = parseDate(checkOutValue);
            const nightlyRate = Number(form.closest(".find-room-result-card")?.dataset.rate);
            const nights = checkIn && checkOut
                ? Math.round((checkOut.getTime() - checkIn.getTime()) / 86400000)
                : 0;

            if (nights < 1 || !Number.isFinite(nightlyRate) || nightlyRate <= 0) {
                event.preventDefault();
                const summary = form.querySelector(".find-room-booking-summary");
                summary.textContent = "We couldn't calculate this stay. Please search for availability again.";
                summary.setAttribute("role", "alert");
                return;
            }

            const total = nightlyRate * nights;
            const summary = form.querySelector(".find-room-booking-summary");
            const totalText = `₹${total.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
            summary.textContent = `${nights} ${nights === 1 ? "night" : "nights"} × ₹${nightlyRate.toLocaleString("en-IN")} = ${totalText} for one room. Food or other requests are not included; the hotel confirms your reservation request.`;
            summary.removeAttribute("role");

            const room = form.closest(".find-room-result-card");
            const accepted = window.confirm(
                `Booking summary\n${room.dataset.roomTitle} · Room ${room.dataset.roomNumber}\n`
                +                 `${checkInValue} to ${checkOutValue} · ${nights} ${nights === 1 ? "night" : "nights"}\n`
                + `${nights} × ₹${nightlyRate.toLocaleString("en-IN")} = ${totalText} for one room\n\n`
                + "This sends a reservation request, not a confirmed booking. The hotel will contact you to confirm."
            );
            if (!accepted) {
                event.preventDefault();
                return;
            }

            const submitButton = form.querySelector("button[type='submit']");
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.setAttribute("aria-busy", "true");
                submitButton.textContent = "Sending request…";
            }
        });
    });

    filterResults();
});
