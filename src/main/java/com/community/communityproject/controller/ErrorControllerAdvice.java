package com.community.communityproject.controller;

import com.community.communityproject.config.exception.*;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.AccessDeniedException;

/**
 * errorNumber
 * 1 : BoardNotFound
 * 2 : BoardNotFound
 * 3 : UserNotFound
 * 4 : TokenExpired
 * 5 : InvalidEndpointRequest
 * 6 : FavoriteNotFound
 * 7 : UserNotFound
 * 8 : LikeNotFound
 * 9 : CommentNotFound
 * 10 : CommentUserNotEqual
 * 11 : AccessDenied
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ErrorControllerAdvice {

    @ExceptionHandler(BoardNotFoundException.class)
    public ModelAndView handleBoardNotFound(BoardNotFoundException ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "존재하지 않는 게시글입니다.");
        mav.addObject("errorNumber", "1");
        mav.setViewName("errorPage");
        return mav;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        String uri = request.getRequestURI();
        String message = "";
        ModelAndView mav = new ModelAndView();
        if (uri.contains("community") || uri.contains("notice") || uri.contains("knowledge") || uri.contains("questions")) {
            message = "존재하지 않는 게시글입니다.";
            mav.addObject("errorNumber", "2");
        }
        else if (uri.contains("profile")) {
            message = "존재하지 않는 사용자입니다.";
            mav.addObject("errorNumber", "3");
        }
        else {
            message = "알 수 없는 이유로 에러가 발생했습니다.";
            mav.addObject("errorNumber", "0");
        }
        mav.addObject("errorMessage", message);
        mav.setViewName("errorPage");
        return mav;
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ModelAndView handleExpiredToken(ExpiredJwtException ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "토큰이 만료되었습니다. 다시 로그인을 해 주세요.");
        mav.addObject("errorNumber", "4");
        mav.setViewName("errorPage");
        return mav;
    }

    @ExceptionHandler(InvalidEndpointRequestException.class)
    public ModelAndView handleInvalidEndpointRequest(InvalidEndpointRequestException ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "잘못된 접근입니다.");
        mav.addObject("errorNumber", "5");
        mav.setViewName("errorPage");
        return mav;
    }

    @ExceptionHandler(FavoriteNotFoundException.class)
    public ModelAndView handleFavoriteNotFound(FavoriteNotFoundException ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "즐겨찾기를 누른 기록이 없습니다.");
        mav.addObject("errorNumber", "6");
        mav.setViewName("errorPage");
        return mav;
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "사용자를 찾을 수 없습니다.");
        mav.addObject("errorNumber", "7");
        mav.setViewName("errorPage");
        return mav;
    }


    @ExceptionHandler(LikeNotFoundException.class)
    public ModelAndView handleLikeNotFound(LikeNotFoundException ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "좋아요를 누른 기록이 없습니다.");
        mav.addObject("errorNumber", "8");
        mav.setViewName("errorPage");
        return mav;
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ModelAndView handleCommentNotFound(CommentNotFoundException ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "해당 댓글을 찾을 수 없습니다.");
        mav.addObject("errorNumber", "9");
        mav.setViewName("errorPage");
        return mav;
    }

    @ExceptionHandler(CommentUserNotEqual.class)
    public ModelAndView handleCommentNotFound(CommentUserNotEqual ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "댓글을 올린 사용자만 댓글을 지을 수 있습니다.");
        mav.addObject("errorNumber", "10");
        mav.setViewName("errorPage");
        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.error("=====================================");
        log.error("!!!! Error occurred at URL : " + request.getRequestURL() + " !!!!");
        log.error("!!!!" + ex.getMessage() + "!!!!");
        log.error("=====================================");
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "권한이 없습니다!");
        mav.addObject("errorNumber", "11");
        mav.setViewName("errorPage");
        return mav;
    }
}
