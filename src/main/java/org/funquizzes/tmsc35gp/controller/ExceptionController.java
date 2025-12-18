package org.funquizzes.tmsc35gp.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.BindException;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException e, Model model) {
        model.addAttribute("errors", e.getBindingResult().getAllErrors());
        model.addAttribute("message", "Ошибки валидации формы");
        return "error";
    }
}