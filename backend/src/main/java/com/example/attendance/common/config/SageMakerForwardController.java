package com.example.attendance.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class SageMakerForwardController {

    private static final String PREFIX = "/absports/8080";

    @RequestMapping("/absports/8080/**")
    public String forward(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI();
        String stripped = path.substring(PREFIX.length());
        if (stripped.isEmpty()) {
            stripped = "/";
        }

        if (stripped.contains("..") || stripped.contains("//")) {
            response.setStatus(400);
            return null;
        }

        if (!stripped.startsWith("/api/") && !stripped.startsWith("/actuator/")) {
            response.setStatus(404);
            return null;
        }

        return "forward:" + stripped;
    }
}
