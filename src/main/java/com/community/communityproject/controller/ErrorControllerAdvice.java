package com.community.communityproject.controller;

import com.community.communityproject.config.exception.BoardNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ErrorControllerAdvice {

    @ExceptionHandler(BoardNotFoundException.class)
    public void handleBoardNotFound(BoardNotFoundException ex, Model model) {
        log.error("=====================================");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        model.addAttribute("errorMessage", "Board Not Found.");
    }
}
