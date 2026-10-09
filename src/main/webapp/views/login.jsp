<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in | Whispering Pines Lodge</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<jsp:include page="/views/common/header.jsp"/>
<main class="container login-container">
    <section class="panel login-panel">
        <p class="eyebrow">HOTEL OPERATIONS</p>
        <h1>Sign in</h1>
        <p>Sign in with your hotel administrator account to manage rooms.</p>
        <c:if test="${param.setup eq 'complete'}">
            <p class="notice success" role="status">Administrator created. Sign in to continue.</p>
        </c:if>
        <c:if test="${not empty loginError}">
            <p class="notice error" role="alert"><c:out value="${loginError}"/></p>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/auth" class="login-form">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <label>
                Username
                <input name="username" required maxlength="100" autocomplete="username" autofocus>
            </label>
            <label>
                Password
                <input name="password" type="password" required autocomplete="current-password">
            </label>
            <button type="submit">Sign in</button>
        </form>
    </section>
</main>
<jsp:include page="/views/common/footer.jsp"/>
</body>
</html>
