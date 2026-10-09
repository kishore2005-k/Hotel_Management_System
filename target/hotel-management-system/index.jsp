<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Whispering Pines Lodge</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<jsp:include page="/views/common/header.jsp"/>
<main class="container home">
    <p class="eyebrow">YOUR FOREST-SIDE RETREAT</p>
    <h1>Whispering Pines Lodge</h1>
    <p>Find your room, check availability, and request your stay among the pines.</p>
    <c:choose>
        <c:when test="${not empty sessionScope.user}">
            <a class="button" href="${pageContext.request.contextPath}/app/rooms">Manage rooms</a>
            <a class="button secondary" href="${pageContext.request.contextPath}/booking">Customer booking page</a>
        </c:when>
        <c:otherwise>
            <a class="button" href="${pageContext.request.contextPath}/booking">Check availability / book a room</a>
            <a href="${pageContext.request.contextPath}/setup">Hotel administrator sign in / setup</a>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/views/common/footer.jsp"/>
</body>
</html>
