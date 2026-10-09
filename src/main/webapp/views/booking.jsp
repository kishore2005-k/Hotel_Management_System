<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reservation requests | Whispering Pines Lodge</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <script src="${pageContext.request.contextPath}/assets/js/validation.js" defer></script>
</head>
<body>
<jsp:include page="/views/common/header.jsp"/>
<main class="container">
    <div class="page-heading">
        <p class="eyebrow">HOTEL OPERATIONS</p>
        <h1>Reservation requests</h1>
        <p>Review guest requests and approve or reject each stay.</p>
    </div>
    <c:if test="${param.updated eq 'approve'}">
        <p class="notice success" role="status">Reservation request approved.</p>
    </c:if>
    <c:if test="${param.updated eq 'approve' and param.email eq 'sent'}">
        <p class="notice success" role="status">Confirmation email sent to the customer's email address.</p>
    </c:if>
    <c:if test="${param.updated eq 'approve' and param.email eq 'not-configured'}">
        <p class="notice error" role="alert">Reservation approved, but confirmation email was not sent. Configure the SMTP environment variables and contact the customer directly.</p>
    </c:if>
    <c:if test="${param.updated eq 'approve' and param.email eq 'failed'}">
        <p class="notice error" role="alert">Reservation approved, but the confirmation email could not be sent. Check the application log and contact the customer directly.</p>
    </c:if>
    <c:if test="${param.updated eq 'reject'}">
        <p class="notice success" role="status">Reservation request rejected.</p>
    </c:if>
    <c:if test="${param.updated eq 'checkout'}">
        <p class="notice success" role="status">Guest checked out. The room is released for new reservations.</p>
    </c:if>
    <c:if test="${param.updated eq 'unavailable'}">
        <p class="notice error" role="alert">This request has already been handled or is no longer available.</p>
    </c:if>
    <c:if test="${not empty databaseError}">
        <p class="notice error" role="alert"><c:out value="${databaseError}"/></p>
    </c:if>
    <section class="panel">
        <c:choose>
            <c:when test="${empty bookings}">
                <p class="empty-state">No reservation requests yet.</p>
            </c:when>
            <c:otherwise>
                <div class="table-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th scope="col">Guest / contact</th>
                            <th scope="col">Room</th>
                            <th scope="col">Stay</th>
                            <th scope="col">Nightly rate</th>
                            <th scope="col">Food request</th>
                            <th scope="col">Special requests</th>
                            <th scope="col">Status / actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="booking" items="${bookings}">
                            <tr>
                                <td>
                                    <strong><c:out value="${booking.customerName}"/></strong><br>
                                    <a href="mailto:<c:out value='${booking.customerEmail}'/>"><c:out value="${booking.customerEmail}"/></a><br>
                                    <a href="tel:<c:out value='${booking.customerPhone}'/>"><c:out value="${booking.customerPhone}"/></a>
                                </td>
                                <td><c:out value="${booking.roomNumber}"/> · <c:out value="${booking.roomType}"/></td>
                                <td><c:out value="${booking.checkInDate}"/> – <c:out value="${booking.checkOutDate}"/></td>
                                <td><fmt:formatNumber value="${booking.nightlyRate}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${booking.foodRequested}">
                                            Yes: <c:forEach var="foodItem" items="${booking.foodItems}" varStatus="foodStatus">
                                                <c:if test="${foodStatus.index gt 0}">, </c:if><c:out value="${foodItem}"/>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>No</c:otherwise>
                                    </c:choose>
                                </td>
                                <td><c:out value="${empty booking.specialRequests ? '—' : booking.specialRequests}"/></td>
                                <td>
                                    <span class="status ${booking.status eq 'PENDING' ? 'pending' : booking.status eq 'CONFIRMED' ? 'available' : booking.status eq 'CHECKED_OUT' ? 'checked-out' : 'unavailable'}">
                                        <c:out value="${booking.status}"/>
                                    </span>
                                    <c:if test="${booking.status eq 'PENDING'}">
                                        <div class="booking-admin-actions">
                                            <form method="post" action="${pageContext.request.contextPath}/app/bookings">
                                                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                                <input type="hidden" name="bookingId" value="<c:out value='${booking.id}'/>">
                                                <input type="hidden" name="action" value="approve">
                                                <button type="submit">Approve</button>
                                            </form>
                                            <form method="post" action="${pageContext.request.contextPath}/app/bookings">
                                                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                                <input type="hidden" name="bookingId" value="<c:out value='${booking.id}'/>">
                                                <input type="hidden" name="action" value="reject">
                                                <button class="danger-button" type="submit">Reject</button>
                                            </form>
                                        </div>
                                    </c:if>
                                    <c:if test="${booking.status eq 'CONFIRMED'}">
                                        <div class="booking-admin-actions">
                                            <form method="post" action="${pageContext.request.contextPath}/app/bookings">
                                                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                                <input type="hidden" name="bookingId" value="<c:out value='${booking.id}'/>">
                                                <input type="hidden" name="action" value="checkout">
                                                <button class="secondary" type="submit">Check out &amp; release room</button>
                                            </form>
                                        </div>
                                        <small>Use after the guest has physically checked out.</small>
                                    </c:if>
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
