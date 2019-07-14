package com.example.demo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageinationDTO {
    //
    private List<QuestionDTO> questions;
    //显示上1页
    private boolean showPrevious;
    //显示第1页
    private boolean showFirstPage;
    //显示下1页
    private boolean showNext;
    //显示最后页
    private boolean showEndPage;
    //当前页数
    private Integer page;
    //当前页数数组
    private List<Integer> pages = new ArrayList<>();

    private Integer totalPage;

    //计算得出页数 数组 分为几组
    public void setPagination(Integer totalPage, Integer page) {
      /*  if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }
        //页数越界
        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }*/
        this.totalPage = totalPage;
        this.page = page;

        //页数数组展示
        pages.add(page);
        for (int i = 1; i <= 3; i++) {
            if (page - i > 0) {
                pages.add(0, page - i);
            }
            if (page + i <= totalPage) {
                pages.add(page + i);
            }
        }


        //是否展示上一页
        if (page == 1) {
            showPrevious = false;
        } else {
            showPrevious = true;
        }
        //是否展示下一页
        if (page == totalPage) {
            showNext = false;
        } else {
            showNext = true;
        }
        //是否展示第一页
        if (pages.contains(1)) {
            showFirstPage = false;
        } else {
            showFirstPage = true;
        }
        //是否展示最后一页
        if (pages.contains(totalPage)) {
            showEndPage = false;
        } else {
            showEndPage = true;
        }

    }
}
