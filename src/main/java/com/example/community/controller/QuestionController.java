package com.example.community.controller;

import com.example.community.dto.CommentDTO;
import com.example.community.dto.QuestionDTO;
import com.example.community.enums.CommentTypeEnum;
import com.example.community.exception.CustomizeErrorCode;
import com.example.community.exception.CustomizeException;
import com.example.community.service.CommentService;
import com.example.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class QuestionController {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private CommentService commentService;

    @GetMapping("/question/{id}")
    public String question(@PathVariable(name = "id") String id, Model model) {
        Long questionId;
        try {
            questionId = Long.parseLong(id);
        } catch (NumberFormatException E) {
            throw new CustomizeException(CustomizeErrorCode.INVALID_INPUT);
        }
        /*获取问题信息*/
        QuestionDTO questionDTO = questionService.getById(questionId);

        /*获取相关问题*/
        List<QuestionDTO> relatedQuestions = questionService.selectRelated(questionDTO);

        /*获取问题评论信息*/
        List<CommentDTO> comments = commentService.listByTargetId(questionId, CommentTypeEnum.QUESTION);
        //累加阅读数
        questionService.incView(questionId);
        /*问题*/
        model.addAttribute("question", questionDTO);
        /*评论*/
        model.addAttribute("comments", comments);
        /*相关问题*/
        model.addAttribute("relatedQuestions", relatedQuestions);
        return "question";
    }
}
