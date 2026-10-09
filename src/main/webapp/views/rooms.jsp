<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Room management | Whispering Pines Lodge</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<jsp:include page="/views/common/header.jsp"/>
<main class="container">
    <div class="page-heading">
        <div>
            <p class="eyebrow">HOTEL OPERATIONS</p>
            <h1>Room management</h1>
            <p>View room inventory and keep room details up to date.</p>
        </div>
    </div>

    <c:if test="${param.saved eq 'create'}">
        <p class="notice success" role="status">Room added successfully.</p>
    </c:if>
    <c:if test="${param.saved eq 'update'}">
        <p class="notice success" role="status">Room updated successfully.</p>
    </c:if>
    <c:if test="${not empty databaseError}">
        <p class="notice error" role="alert"><c:out value="${databaseError}"/></p>
    </c:if>
    <c:if test="${not empty formError}">
        <p class="notice error" role="alert"><c:out value="${formError}"/></p>
    </c:if>

    <c:choose>
        <c:when test="${not empty formRoom}">
            <c:set var="activeRoom" value="${formRoom}"/>
        </c:when>
        <c:when test="${not empty roomToEdit}">
            <c:set var="activeRoom" value="${roomToEdit}"/>
        </c:when>
        <c:otherwise>
            <c:set var="activeRoom" value="${null}"/>
        </c:otherwise>
    </c:choose>
    <c:set var="editing" value="${not empty activeRoom and activeRoom.id gt 0}"/>
    <c:choose>
        <c:when test="${formNightlyRate ne null}">
            <c:set var="displayRate" value="${formNightlyRate}"/>
        </c:when>
        <c:otherwise>
            <c:set var="displayRate" value="${activeRoom.nightlyRate}"/>
        </c:otherwise>
    </c:choose>

    <section class="panel" aria-labelledby="room-form-title">
        <h2 id="room-form-title">
            <c:choose>
                <c:when test="${editing}">Edit room</c:when>
                <c:otherwise>Add a room</c:otherwise>
            </c:choose>
        </h2>
        <form method="post" action="${pageContext.request.contextPath}/app/rooms" class="room-form">
            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
            <input type="hidden" name="action" value="${editing ? 'update' : 'create'}">
            <c:if test="${editing}">
                <input type="hidden" name="id" value="<c:out value='${activeRoom.id}'/>">
            </c:if>
            <label>
                Room number
                <input name="roomNumber" required maxlength="20" autocomplete="off"
                       value="<c:out value='${activeRoom.roomNumber}'/>">
            </label>
            <label>
                Room type
                <input name="roomType" required maxlength="50" placeholder="e.g. Single, Double, Suite"
                       value="<c:out value='${activeRoom.roomType}'/>">
            </label>
            <label>
                Nightly rate
                <input name="nightlyRate" type="number" required min="0.01" step="0.01"
                       value="<c:out value='${displayRate}'/>">
            </label>
            <label>
                Air conditioning
                <select name="airConditioned" required>
                    <option value="true" ${not empty activeRoom and activeRoom.airConditioned ? 'selected' : ''}>AC</option>
                    <option value="false" ${empty activeRoom or not activeRoom.airConditioned ? 'selected' : ''}>Non-AC</option>
                </select>
            </label>
            <label>
                Availability
                <select name="available" required>
                    <option value="true" ${empty activeRoom or activeRoom.available ? 'selected' : ''}>Available</option>
                    <option value="false" ${not empty activeRoom and not activeRoom.available ? 'selected' : ''}>Occupied / unavailable</option>
                </select>
            </label>
            <div class="form-actions">
                <button type="submit"><c:choose><c:when test="${editing}">Save changes</c:when><c:otherwise>Add room</c:otherwise></c:choose></button>
                <c:if test="${editing}">
                    <a class="button secondary" href="${pageContext.request.contextPath}/app/rooms">Cancel</a>
                </c:if>
            </div>
        </form>
    </section>

    <section class="panel" aria-labelledby="inventory-title">
        <div class="section-heading">
            <h2 id="inventory-title">Room inventory</h2>
            <span class="count"><c:out value="${rooms.size()}"/> rooms</span>
        </div>
        <c:choose>
            <c:when test="${empty rooms}">
                <p class="empty-state">No rooms have been added yet.</p>
            </c:when>
            <c:otherwise>
                <div class="table-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th scope="col">Room</th>
                            <th scope="col">Type</th>
                            <th scope="col">Air conditioning</th>
                            <th scope="col">Nightly rate</th>
                            <th scope="col">Availability</th>
                            <th scope="col"><span class="visually-hidden">Actions</span></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="room" items="${rooms}">
                            <tr>
                                <td><strong><c:out value="${room.roomNumber}"/></strong></td>
                                <td><c:out value="${room.roomType}"/></td>
                                <td><c:out value="${room.airConditioned ? 'AC' : 'Non-AC'}"/></td>
                                <td><fmt:formatNumber value="${room.nightlyRate}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                <td>
                                    <span class="status ${room.available ? 'available' : 'unavailable'}">
                                        <c:out value="${room.available ? 'Available' : 'Unavailable'}"/>
                                    </span>
                                </td>
                                <td class="actions">
                                    <a href="${pageContext.request.contextPath}/app/rooms?edit=<c:out value='${room.id}'/>">Edit</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<jsp:include page="/views/common/footer.jsp"/>
</body>
</html>
