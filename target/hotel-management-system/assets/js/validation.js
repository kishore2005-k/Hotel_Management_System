"use strict";

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("form[data-validate]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            if (!form.reportValidity()) {
                event.preventDefault();
            }
        });
    });
});
