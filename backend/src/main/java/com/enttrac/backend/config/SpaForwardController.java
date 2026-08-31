package com.enttrac.backend.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        String path = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);

        boolean isApiRequest = path != null && path.startsWith("/api/");
        boolean looksLikeStaticFile = path != null && path.contains(".");

        if (!isApiRequest && !looksLikeStaticFile) {
            return "forward:/index.html";
        }

        return "error";
    }
}