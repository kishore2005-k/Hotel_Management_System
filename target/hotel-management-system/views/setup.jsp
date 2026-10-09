<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Initial administrator setup | Whispering Pines Lodge</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<jsp:include page="/views/common/header.jsp"/>
<main class="container login-container">
    <section class="panel login-panel">
        <p class="eyebrow">ONE-TIME SETUP</p>
        <h1>Create administrator</h1>
        <p>Create the first administrator account. Setup will be disabled after this account is created.</p>
        <c:if test="${not empty setupError}">
            <p class="notice error" role="alert"><c:out value="${setupError}"/></p>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/setup" class="login-form">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <label>
                Username
                <input name="username" required minlength="3" maxlength="100"
                       pattern="[A-Za-z0-9._-]{3,100}" autocomplete="username" autofocus>
            </label>
            <label>
                Password (12-128 characters)
                <input name="password" type="password" required minlength="12" maxlength="128"
                       autocomplete="new-password">
            </label>
            <label>
                Confirm password
                <input name="confirmPassword" type="password" required minlength="12" maxlength="128"
                       autocomplete="new-password">
            </label>
            <button type="submit">Create administrator</button>
        </form>
    </section>
</main>
<jsp:include page="/views/common/footer.jsp"/>
</body>
</html>
