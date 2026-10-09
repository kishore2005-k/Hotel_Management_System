<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header class="container">
    <nav aria-label="Main navigation">
        <a class="brand" href="${pageContext.request.contextPath}/">Whispering Pines Lodge</a>
        <a href="${pageContext.request.contextPath}/booking">Find a room</a>
        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/app/rooms">Rooms</a>
                <a href="${pageContext.request.contextPath}/app/bookings">Requests</a>
                <span class="nav-user">Signed in as <c:out value="${sessionScope.user.username}"/></span>
                <form method="post" action="${pageContext.request.contextPath}/logout" class="logout-form">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                    <button type="submit" class="link-button">Sign out</button>
                </form>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/auth">Sign in</a>
            </c:otherwise>
        </c:choose>
    </nav>
</header>
