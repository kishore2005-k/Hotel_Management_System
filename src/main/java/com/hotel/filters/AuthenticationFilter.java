package com.hotel.filters;

import java.io.IOException;
import com.hotel.security.CsrfTokens;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("/app/*")
public class AuthenticationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            session = httpRequest.getSession(true);
            session.setAttribute("loginReturnTo", httpRequest.getServletPath());
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth");
            return;
        }
        String csrfToken = CsrfTokens.getOrCreate(session);
        if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
            if (!CsrfTokens.isValid(session, httpRequest.getParameter("csrfToken"))) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid security token.");
                return;
            }
        }
        httpResponse.setHeader("Cache-Control", "no-store");
        httpRequest.setAttribute("csrfToken", csrfToken);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
