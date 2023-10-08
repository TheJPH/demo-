package com.example.community.dto;

import com.example.community.model.User;
import lombok.Data;


@Data
public class QuestionDTO {
    private Long id;
    private String title;
    private String description;
    private Long gmtCreate;
    private Long gmtModified;
    private Long creator;
    private String tag;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private User user;
}
