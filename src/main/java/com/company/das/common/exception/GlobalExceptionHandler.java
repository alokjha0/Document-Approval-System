package com.company.das.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public RedirectView handleRuntimeException(
            RuntimeException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        // Add error message
        redirectAttributes.addFlashAttribute("error", ex.getMessage());

        // Get previous URL (where error occurred)
        String referer = request.getHeader("Referer");

        // Fallback if null
        if (referer == null || referer.isEmpty()) {
            referer = "/";
        }

        return new RedirectView(referer);
    }
}
