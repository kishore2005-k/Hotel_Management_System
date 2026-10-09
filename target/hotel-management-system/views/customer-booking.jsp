<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Find a room | Whispering Pines Lodge</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=find-room-food-modal-v2">
    <script src="${pageContext.request.contextPath}/assets/js/booking-options.js?v=find-room-food-modal-v2" defer></script>
    <script src="${pageContext.request.contextPath}/assets/js/find-room.js" defer></script>
</head>
<body class="find-room-page">
<jsp:include page="/views/common/header.jsp"/>
<main class="find-room">
    <section class="find-room-hero" aria-labelledby="find-room-title">
        <p class="find-room-kicker">A FOREST RETREAT, MADE YOURS</p>
        <h1 id="find-room-title">Stay a little closer to nature.</h1>
        <p>Thoughtfully appointed rooms, warm wood, and the quiet luxury of the pines.</p>
        <a class="find-room-hero-link" href="#availability-panel">Discover your stay <span aria-hidden="true">↓</span></a>
        <div class="find-room-hero-rule" aria-hidden="true"></div>
        <span class="find-room-hero-caption">WHISPERING PINES LODGE <span aria-hidden="true">·</span> REST, RECONNECT, FEEL AT HOME</span>
    </section>

    <div class="find-room-content">
        <section class="find-room-categories" aria-labelledby="categories-title">
            <div class="find-room-section-heading">
                <div>
                    <p class="find-room-kicker">YOUR PRIVATE SANCTUARY</p>
                    <h2 id="categories-title">Rooms with room to breathe</h2>
                </div>
                <p>Choose your favourite, then search your dates to see live rates and availability.</p>
            </div>

            <div class="find-room-category-grid">
                <article class="find-room-category-card">
                    <img src="${pageContext.request.contextPath}/assets/images/single-room-photo.jpg"
                         alt="Warm wood single room with a comfortable bed and garden view">
                    <div class="find-room-category-copy">
                        <p class="find-room-kicker">QUIETLY YOURS</p>
                        <h3>Single Room</h3>
                        <p>A peaceful, considered hideaway for one.</p>
                        <p class="find-room-category-rate">
                            <c:choose>
                                <c:when test="${not empty startingRates['single']}">
                                    From <strong>₹<fmt:formatNumber value="${startingRates['single']}" minFractionDigits="0" maxFractionDigits="2"/></strong> / night
                                </c:when>
                                <c:otherwise>Search for current rates</c:otherwise>
                            </c:choose>
                        </p>
                        <div class="find-room-category-actions">
                            <button class="find-room-outline-button" type="button" data-room-details="single">View Details</button>
                            <button class="find-room-gold-button" type="button" data-book-category="single">Book Now <span aria-hidden="true">→</span></button>
                        </div>
                    </div>
                </article>

                <article class="find-room-category-card">
                    <img src="${pageContext.request.contextPath}/assets/images/double-room-photo.jpg"
                         alt="Elegant double room with a queen bed, timber details, and a mountain view">
                    <div class="find-room-category-copy">
                        <p class="find-room-kicker">MADE FOR TWO</p>
                        <h3>Double Room</h3>
                        <p>A little more space for slow mornings together.</p>
                        <p class="find-room-category-rate">
                            <c:choose>
                                <c:when test="${not empty startingRates['double']}">
                                    From <strong>₹<fmt:formatNumber value="${startingRates['double']}" minFractionDigits="0" maxFractionDigits="2"/></strong> / night
                                </c:when>
                                <c:otherwise>Search for current rates</c:otherwise>
                            </c:choose>
                        </p>
                        <div class="find-room-category-actions">
                            <button class="find-room-outline-button" type="button" data-room-details="double">View Details</button>
                            <button class="find-room-gold-button" type="button" data-book-category="double">Book Now <span aria-hidden="true">→</span></button>
                        </div>
                    </div>
                </article>

                <article class="find-room-category-card">
                    <img src="${pageContext.request.contextPath}/assets/images/suite-room-photo.jpg"
                         alt="Spacious luxury suite with a king bed and separate living area">
                    <div class="find-room-category-copy">
                        <p class="find-room-kicker">THE ART OF UNWINDING</p>
                        <h3>Luxury Suite</h3>
                        <p>Generous space for a stay worth lingering over.</p>
                        <p class="find-room-category-rate">
                            <c:choose>
                                <c:when test="${not empty startingRates['suite']}">
                                    From <strong>₹<fmt:formatNumber value="${startingRates['suite']}" minFractionDigits="0" maxFractionDigits="2"/></strong> / night
                                </c:when>
                                <c:otherwise>Search for current rates</c:otherwise>
                            </c:choose>
                        </p>
                        <div class="find-room-category-actions">
                            <button class="find-room-outline-button" type="button" data-room-details="suite">View Details</button>
                            <button class="find-room-gold-button" type="button" data-book-category="suite">Book Now <span aria-hidden="true">→</span></button>
                        </div>
                    </div>
                </article>
            </div>
        </section>

        <c:if test="${requested}">
            <p class="find-room-notice find-room-success" role="status">
                Your reservation request was sent. The hotel will contact you to confirm it.
            </p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="find-room-notice find-room-error" role="alert"><c:out value="${formError}"/></p>
        </c:if>
        <c:if test="${not empty databaseError}">
            <p class="find-room-notice find-room-error" role="alert"><c:out value="${databaseError}"/></p>
        </c:if>

        <section class="find-room-search-panel" id="availability-panel" aria-labelledby="search-title">
            <div class="find-room-section-heading">
                <div>
                    <p class="find-room-kicker">FIND YOUR DATES</p>
                    <h2 id="search-title">Make this stay yours</h2>
                </div>
                <p>Live availability is checked against the lodge reservation calendar.</p>
            </div>
            <form method="get" action="${pageContext.request.contextPath}/booking" class="find-room-search-form" id="find-room-search-form">
                <label>
                    Check-in
                    <input type="date" name="checkIn" id="find-room-check-in" required
                           min="<c:out value='${minimumDate}'/>" value="<c:out value='${checkInValue}'/>">
                </label>
                <label>
                    Check-out
                    <input type="date" name="checkOut" id="find-room-check-out" required
                           min="<c:out value='${minimumDate}'/>" value="<c:out value='${checkOutValue}'/>">
                </label>
                <label>
                    Adults
                    <select name="adults" id="find-room-adults">
                        <c:forEach var="guestCount" begin="1" end="4">
                            <option value="<c:out value='${guestCount}'/>" ${param.adults eq guestCount ? 'selected' : ''}>
                                <c:out value="${guestCount}"/> <c:out value="${guestCount eq 1 ? 'adult' : 'adults'}"/>
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <label>
                    Children
                    <select name="children" id="find-room-children">
                        <c:forEach var="childCount" begin="0" end="4">
                            <option value="<c:out value='${childCount}'/>" ${param.children eq childCount ? 'selected' : ''}>
                                <c:out value="${childCount}"/> <c:out value="${childCount eq 1 ? 'child' : 'children'}"/>
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <label>
                    Rooms
                    <select name="nRooms" id="find-room-count">
                        <c:forEach var="roomCount" begin="1" end="5">
                            <option value="<c:out value='${roomCount}'/>" ${param.nRooms eq roomCount ? 'selected' : ''}>
                                <c:out value="${roomCount}"/> <c:out value="${roomCount eq 1 ? 'room' : 'rooms'}"/>
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <label>
                    Room category
                    <select name="category" id="find-room-category-filter">
                        <option value="any" ${empty param.category or param.category eq 'any' ? 'selected' : ''}>Any room</option>
                        <option value="single" ${param.category eq 'single' ? 'selected' : ''}>Single Room</option>
                        <option value="double" ${param.category eq 'double' ? 'selected' : ''}>Double Room</option>
                        <option value="suite" ${param.category eq 'suite' ? 'selected' : ''}>Luxury Suite</option>
                    </select>
                </label>
                <label>
                    Bed type
                    <select name="bedType" id="find-room-bed-filter">
                        <option value="any" ${empty param.bedType or param.bedType eq 'any' ? 'selected' : ''}>Any bed</option>
                        <option value="single" ${param.bedType eq 'single' ? 'selected' : ''}>Single bed</option>
                        <option value="queen" ${param.bedType eq 'queen' ? 'selected' : ''}>Queen bed</option>
                        <option value="king" ${param.bedType eq 'king' ? 'selected' : ''}>King bed</option>
                    </select>
                </label>
                <label>
                    Air conditioning
                    <select name="acType" id="find-room-ac-filter">
                        <option value="any" ${empty param.acType or param.acType eq 'any' ? 'selected' : ''}>Any</option>
                        <option value="ac" ${param.acType eq 'ac' ? 'selected' : ''}>Air-conditioned</option>
                        <option value="non-ac" ${param.acType eq 'non-ac' ? 'selected' : ''}>Non-AC</option>
                    </select>
                </label>
                <label class="find-room-price-filter">
                    Maximum price per night
                    <span class="find-room-price-output">₹<output id="find-room-price-value"><c:out value="${not empty param.priceMax ? param.priceMax : (maximumRate gt 0 ? maximumRate : 100000)}"/></output></span>
                    <input type="range" name="priceMax" id="find-room-price-max" min="0" max="<c:out value='${maximumRate gt 0 ? maximumRate : 100000}'/>"
                           step="500" value="<c:out value='${not empty param.priceMax ? param.priceMax : (maximumRate gt 0 ? maximumRate : 100000)}'/>">
                </label>
                <fieldset class="find-room-amenity-filter">
                    <legend>Include amenities</legend>
                    <label><input type="checkbox" name="amenity" value="free-wifi"> Free Wi-Fi</label>
                    <label><input type="checkbox" name="amenity" value="smart-tv"> Smart TV</label>
                    <label><input type="checkbox" name="amenity" value="private-bathroom"> Private bathroom</label>
                    <label><input type="checkbox" name="amenity" value="work-desk"> Work desk</label>
                    <label><input type="checkbox" name="amenity" value="seating-area"> Seating area</label>
                    <label><input type="checkbox" name="amenity" value="living-area"> Separate living area</label>
                    <label><input type="checkbox" name="amenity" value="scenic-view"> Garden / mountain view</label>
                </fieldset>
                <div class="find-room-search-actions">
                    <button class="find-room-gold-button" type="submit" id="find-room-search-button">Search Availability <span aria-hidden="true">→</span></button>
                    <a class="find-room-reset-button" href="${pageContext.request.contextPath}/booking">Reset filters</a>
                    <span class="find-room-search-message" id="find-room-search-message" role="status" aria-live="polite"></span>
                </div>
            </form>
        </section>

        <c:if test="${searched}">
            <section class="find-room-results" id="find-room-results" aria-labelledby="results-title">
                <div class="find-room-results-heading">
                    <div>
                        <p class="find-room-kicker">PERSONALLY CONSIDERED</p>
                        <h2 id="results-title">Rooms for your stay</h2>
                        <p class="find-room-results-dates">
                            <c:out value="${checkInValue}"/> <span aria-hidden="true">→</span> <c:out value="${checkOutValue}"/>
                        </p>
                    </div>
                    <div class="find-room-results-tools">
                        <span class="find-room-results-count" id="find-room-results-count"><c:out value="${rooms.size()}"/> available</span>
                        <label>
                            Sort by
                            <select id="find-room-sort">
                                <option value="recommended">Recommended</option>
                                <option value="price-low">Price: low to high</option>
                                <option value="price-high">Price: high to low</option>
                            </select>
                        </label>
                    </div>
                </div>
                <p class="find-room-filter-message" id="find-room-filter-message" role="status" aria-live="polite"></p>
                <p class="find-room-empty-state" id="find-room-empty-state" ${not empty rooms ? 'hidden' : ''}>
                    <span aria-hidden="true">✧</span>
                    <strong>No rooms match those filters just yet.</strong>
                    Try a different room type, date, or amenity combination, or reset the filters to explore all available rooms.
                </p>
                <div class="find-room-results-grid" id="find-room-results-grid">
                    <c:forEach var="room" items="${rooms}">
                        <c:set var="roomTypeLower" value="${fn:toLowerCase(room.roomType)}"/>
                        <c:choose>
                            <c:when test="${fn:contains(roomTypeLower, 'suite')}">
                                <c:set var="roomCategory" value="suite"/>
                                <c:set var="roomTitle" value="Luxury Suite"/>
                                <c:set var="roomBed" value="king"/>
                                <c:set var="roomBedLabel" value="King-size bed"/>
                                <c:set var="roomCapacity" value="4"/>
                                <c:set var="roomPhoto" value="suite-room-photo.jpg"/>
                                <c:set var="roomAmenities" value="free-wifi smart-tv private-bathroom living-area scenic-view"/>
                                <c:set var="roomDescription" value="An inviting suite with a king-size bed and a separate living area."/>
                            </c:when>
                            <c:when test="${fn:contains(roomTypeLower, 'double')}">
                                <c:set var="roomCategory" value="double"/>
                                <c:set var="roomTitle" value="Double Room"/>
                                <c:set var="roomBed" value="queen"/>
                                <c:set var="roomBedLabel" value="Queen-size bed"/>
                                <c:set var="roomCapacity" value="2"/>
                                <c:set var="roomPhoto" value="double-room-photo.jpg"/>
                                <c:set var="roomAmenities" value="free-wifi smart-tv private-bathroom seating-area scenic-view"/>
                                <c:set var="roomDescription" value="A welcoming double room with a queen-size bed and a comfortable seating area."/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="roomCategory" value="single"/>
                                <c:set var="roomTitle" value="Single Room"/>
                                <c:set var="roomBed" value="single"/>
                                <c:set var="roomBedLabel" value="Single bed"/>
                                <c:set var="roomCapacity" value="1"/>
                                <c:set var="roomPhoto" value="single-room-photo.jpg"/>
                                <c:set var="roomAmenities" value="free-wifi smart-tv private-bathroom work-desk scenic-view"/>
                                <c:set var="roomDescription" value="A calm, well-appointed room with a single bed and a dedicated work desk."/>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${room.airConditioned}">
                            <c:set var="roomAmenities" value="${roomAmenities} air-conditioning"/>
                        </c:if>
                        <article class="find-room-result-card"
                                 data-category="<c:out value='${roomCategory}'/>"
                                 data-bed="<c:out value='${roomBed}'/>"
                                 data-capacity="<c:out value='${roomCapacity}'/>"
                                 data-rate="<c:out value='${room.nightlyRate}'/>"
                                 data-ac="<c:out value='${room.airConditioned}'/>"
                                 data-amenities="<c:out value='${roomAmenities}'/>"
                                 data-room-title="<c:out value='${roomTitle}'/>"
                                 data-room-number="<c:out value='${room.roomNumber}'/>"
                                 data-room-id="<c:out value='${room.id}'/>"
                                 data-bed-label="<c:out value='${roomBedLabel}'/>"
                                 data-room-description="<c:out value='${roomDescription}'/>"
                                 data-room-photo="${pageContext.request.contextPath}/assets/images/<c:out value='${roomPhoto}'/>">
                            <div class="find-room-result-photo">
                                <img src="${pageContext.request.contextPath}/assets/images/<c:out value='${roomPhoto}'/>"
                                     alt="<c:out value='${roomTitle}'/> at Whispering Pines Lodge">
                                <span class="find-room-room-number">ROOM <c:out value="${room.roomNumber}"/></span>
                                <span class="find-room-available">Available for your dates</span>
                            </div>
                            <div class="find-room-result-copy">
                                <div class="find-room-result-title">
                                    <div>
                                        <p class="find-room-kicker"><c:out value="${room.airConditioned ? 'AIR-CONDITIONED COMFORT' : 'A NATURALLY COOL RETREAT'}"/></p>
                                        <h3><c:out value="${roomTitle}"/></h3>
                                    </div>
                                    <p class="find-room-result-price">
                                        <strong>₹<fmt:formatNumber value="${room.nightlyRate}" minFractionDigits="0" maxFractionDigits="2"/></strong>
                                        <span>per night</span>
                                    </p>
                                </div>
                                <p class="find-room-result-description"><c:out value="${roomDescription}"/></p>
                                <div class="find-room-facts">
                                    <span><span aria-hidden="true">♙</span> Up to <c:out value="${roomCapacity}"/> guests</span>
                                    <span><span aria-hidden="true">▱</span> <c:out value="${roomBedLabel}"/></span>
                                </div>
                                <div class="find-room-amenities">
                                    <span><span aria-hidden="true">⌁</span> Free Wi-Fi</span>
                                    <span><span aria-hidden="true">▣</span> Smart TV</span>
                                    <span><span aria-hidden="true">♧</span> Private bathroom</span>
                                    <c:if test="${room.airConditioned}">
                                        <span><span aria-hidden="true">❄</span> Air conditioning</span>
                                    </c:if>
                                    <c:choose>
                                        <c:when test="${roomCategory eq 'single'}">
                                            <span><span aria-hidden="true">⌘</span> Work desk</span>
                                            <span><span aria-hidden="true">⌂</span> Garden / forest view</span>
                                        </c:when>
                                        <c:when test="${roomCategory eq 'double'}">
                                            <span><span aria-hidden="true">◌</span> Seating area</span>
                                            <span><span aria-hidden="true">⌂</span> Garden / mountain view</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span><span aria-hidden="true">◌</span> Separate living area</span>
                                            <span><span aria-hidden="true">⌂</span> Garden / balcony view, where available</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="find-room-result-actions">
                                    <button class="find-room-outline-button" type="button" data-view-room>View Details</button>
                                    <button class="find-room-gold-button" type="button" data-book-room>Book Now <span aria-hidden="true">→</span></button>
                                </div>

                                <form method="post" action="${pageContext.request.contextPath}/booking"
                                      class="booking-form find-room-booking-form" hidden>
                                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                                    <input type="hidden" name="roomId" value="<c:out value='${room.id}'/>">
                                    <input type="hidden" name="checkIn" value="<c:out value='${checkInValue}'/>">
                                    <input type="hidden" name="checkOut" value="<c:out value='${checkOutValue}'/>">
                                    <input type="hidden" name="acType" value="<c:out value='${acType}'/>">
                                    <label>
                                        Your name
                                        <input name="customerName" required maxlength="100" autocomplete="name"
                                               value="<c:out value='${bookingForm.customerName}'/>">
                                    </label>
                                    <label>
                                        Email
                                        <input name="customerEmail" type="email" required maxlength="254" autocomplete="email"
                                               value="<c:out value='${bookingForm.customerEmail}'/>">
                                    </label>
                                    <label>
                                        Contact number
                                        <input name="customerPhone" type="tel" required maxlength="30" autocomplete="tel"
                                               pattern="[+0-9 .\(\)\-]{7,30}"
                                               value="<c:out value='${bookingForm.customerPhone}'/>">
                                    </label>
                                    <section class="find-room-food-choice full-width" aria-labelledby="food-choice-title">
                                        <div class="find-room-food-choice-heading">
                                            <h3 id="food-choice-title">Would you like to request food?</h3>
                                            <p>Let us know if you would like the lodge to arrange meals during your stay.</p>
                                        </div>
                                        <div class="find-room-food-options">
                                            <label class="find-room-food-option">
                                                <input type="radio" name="foodRequested" value="no"
                                                       ${not foodRequested ? 'checked' : ''}>
                                                <span class="find-room-food-option-icon" aria-hidden="true">−</span>
                                                <span class="find-room-food-option-copy"><strong>No, thank you</strong><small>No meal request</small></span>
                                            </label>
                                            <div class="find-room-food-option-group">
                                                <label class="find-room-food-option">
                                                    <input type="radio" name="foodRequested" value="yes"
                                                           ${foodRequested ? 'checked' : ''}>
                                                    <span class="find-room-food-option-icon" aria-hidden="true">✧</span>
                                                    <span class="find-room-food-option-copy"><strong>Yes, please</strong><small>Explore our Indian menu</small></span>
                                                </label>
                                                <button class="find-room-food-edit" type="button" data-open-food-menu>
                                                    <c:choose>
                                                        <c:when test="${not empty selectedFoodItems}">Edit menu selection</c:when>
                                                        <c:otherwise>Choose menu items</c:otherwise>
                                                    </c:choose>
                                                </button>
                                            </div>
                                        </div>
                                        <p class="find-room-food-summary" aria-live="polite">
                                            <c:if test="${foodRequested and not empty selectedFoodItems}">
                                                Food request added — <c:out value="${selectedFoodItems.size()}"/> sample items selected. Availability and pricing will be confirmed by reception.
                                            </c:if>
                                        </p>
                                        <p class="find-room-food-error" role="alert" hidden></p>
                                    </section>
                                    <div class="find-room-selected-food-inputs" hidden>
                                        <c:forEach var="selectedFoodItem" items="${selectedFoodItems}">
                                            <input type="hidden" name="foodItems" value="<c:out value='${selectedFoodItem}'/>">
                                        </c:forEach>
                                    </div>
                                    <label class="full-width">
                                        Special requests or accessibility needs <span class="optional">(optional)</span>
                                        <textarea name="specialRequests" maxlength="1000" rows="3"><c:out value="${bookingForm.specialRequests}"/></textarea>
                                    </label>
                                    <div class="find-room-booking-summary full-width" aria-live="polite"></div>
                                    <div class="booking-submit full-width">
                                        <p>Submitting sends a request; your stay is not confirmed until the hotel approves it.</p>
                                        <button type="submit">Review &amp; request this room</button>
                                    </div>
                                </form>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </section>
        </c:if>

        <dialog class="find-room-food-dialog" id="find-room-food-dialog" aria-labelledby="find-room-food-dialog-title">
            <div class="find-room-food-dialog-shell">
                <header class="find-room-food-dialog-header">
                    <div>
                        <p class="find-room-food-dialog-kicker">A TASTE OF THE LODGE</p>
                        <h2 id="find-room-food-dialog-title">Our Indian Menu</h2>
                        <p>A few flavours to make your stay feel like home.</p>
                    </div>
                    <button class="find-room-food-dialog-close" type="button" aria-label="Close menu">×</button>
                </header>
                <p class="find-room-food-placeholder-note">
                    Sample menu placeholders — dishes and prices have not been configured by the lodge.
                    Reception will confirm availability and pricing.
                </p>
                <div class="find-room-food-dialog-body">
                    <c:forEach var="category" items="${foodMenuCategories}">
                        <section class="find-room-food-category">
                            <div class="find-room-food-category-heading">
                                <c:choose>
                                    <c:when test="${category.key eq 'Breakfast'}">
                                        <img src="${pageContext.request.contextPath}/assets/images/food-breakfast.svg" alt="">
                                        <c:set var="foodCategoryNote" value="A gentle start to the morning"/>
                                    </c:when>
                                    <c:when test="${category.key eq 'South Indian Specials'}">
                                        <img src="${pageContext.request.contextPath}/assets/images/food-breakfast.svg" alt="">
                                        <c:set var="foodCategoryNote" value="Comforting southern favourites"/>
                                    </c:when>
                                    <c:when test="${category.key eq 'North Indian Meals'}">
                                        <img src="${pageContext.request.contextPath}/assets/images/food-breads.svg" alt="">
                                        <c:set var="foodCategoryNote" value="A hearty taste of the north"/>
                                    </c:when>
                                    <c:when test="${category.key eq 'Vegetarian Dishes'}">
                                        <img src="${pageContext.request.contextPath}/assets/images/food-vegetarian.svg" alt="">
                                        <c:set var="foodCategoryNote" value="Garden-fresh Indian favourites"/>
                                    </c:when>
                                    <c:when test="${category.key eq 'Non-Vegetarian Dishes'}">
                                        <img src="${pageContext.request.contextPath}/assets/images/food-nonvegetarian.svg" alt="">
                                        <c:set var="foodCategoryNote" value="Slow-cooked lodge classics"/>
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}/assets/images/food-desserts.svg" alt="">
                                        <c:set var="foodCategoryNote" value="Refreshing sips and something sweet"/>
                                    </c:otherwise>
                                </c:choose>
                                <div>
                                    <h3><c:out value="${category.key}"/></h3>
                                    <p><c:out value="${foodCategoryNote}"/></p>
                                </div>
                            </div>
                            <div class="find-room-food-items">
                                <c:forEach var="foodItem" items="${category.value}">
                                    <c:set var="vegetarianItem" value="${category.key ne 'Non-Vegetarian Dishes'}"/>
                                    <label class="find-room-food-item">
                                        <input type="checkbox" data-food-item value="<c:out value='${foodItem}'/>"
                                               ${not empty selectedFoodItems and selectedFoodItems.contains(foodItem) ? 'checked' : ''}>
                                        <span class="find-room-food-item-icon" aria-hidden="true"></span>
                                        <span class="find-room-food-item-copy">
                                            <strong><c:out value="${foodItem}"/></strong>
                                            <small><c:out value="${vegetarianItem ? 'Vegetarian' : 'Non-vegetarian'}"/> · Sample suggestion</small>
                                        </span>
                                        <span class="find-room-food-item-check" aria-hidden="true">✓</span>
                                    </label>
                                </c:forEach>
                            </div>
                        </section>
                    </c:forEach>
                </div>
                <footer class="find-room-food-dialog-footer">
                    <span class="find-room-food-dialog-selection" aria-live="polite" tabindex="-1">No sample items selected</span>
                    <div>
                        <button class="find-room-food-close-button" type="button">Close Menu</button>
                        <button class="find-room-gold-button find-room-food-confirm-button" type="button">Confirm Food Request</button>
                    </div>
                </footer>
            </div>
        </dialog>

        <section class="find-room-contact" aria-label="Hotel contact information">
            <span class="find-room-contact-mark" aria-hidden="true">✧</span>
            <div>
                <h2>We'd love to help plan your stay</h2>
                <c:choose>
                    <c:when test="${not empty hotelPhone}">
                        <p>Call our reception at <a href="tel:<c:out value='${hotelPhone}'/>"><c:out value="${hotelPhone}"/></a></p>
                    </c:when>
                    <c:otherwise>
                        <p>Hotel phone number has not been configured yet. Please contact the hotel directly.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>
</main>

<dialog class="find-room-dialog" id="find-room-details-dialog" aria-labelledby="find-room-dialog-title">
    <button class="find-room-dialog-close" type="button" aria-label="Close room details">×</button>
    <img class="find-room-dialog-photo" alt="" src="">
    <div class="find-room-dialog-content">
        <p class="find-room-kicker">A LITTLE MORE TO LOVE</p>
        <h2 id="find-room-dialog-title"></h2>
        <p class="find-room-dialog-description"></p>
        <dl>
            <div><dt>Bed</dt><dd class="find-room-dialog-bed"></dd></div>
            <div><dt>Guest capacity</dt><dd class="find-room-dialog-capacity"></dd></div>
            <div><dt>Room</dt><dd class="find-room-dialog-number"></dd></div>
        </dl>
        <div class="find-room-dialog-amenities"></div>
        <button class="find-room-gold-button find-room-dialog-action" type="button">Choose this room</button>
    </div>
</dialog>

<jsp:include page="/views/common/footer.jsp"/>
</body>
</html>
