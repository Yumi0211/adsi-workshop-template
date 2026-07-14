package com.example.attendance.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SageMakerForwardController {

    @RequestMapping("/absports/8080/**")
    public String forward(HttpServletRequest request) {
        String path = request.getRequestURI();
        String stripped = path.substring("/absports/8080".length());
        if (stripped.isEmpty()) {
            stripped = "/";
        }
        return "forward:" + stripped;
    }
}
