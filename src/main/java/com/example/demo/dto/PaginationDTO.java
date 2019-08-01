package com.example.demo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaginationDTO<T> {
    private List<T> data;
    private boolean showPrevious;
    private boolean showFirstPage;
    private boolean showNext;
    private boolean showEndPage;
    private Integer page;//当前页码
    private List<Integer> pages = new ArrayList<>();//当前页数清单
    private Integer totalPage;

    public void setPagination(Integer totalPage, Integer page) {
        //页总数
        this.totalPage = totalPage;
        this.page = page;
        /*
         * 页数显示逻辑
         * 当前页-i>0加入
         * 当前页+i<=totalpage加入
         * 当前页左3右3一共7个页数
         * */
        pages.add(page);
        for (int i = 1; i <= 3; i++) {
            if (page - i > 0) {
                pages.add(0, page - i);
            }

            if (page + i <= totalPage) {
                pages.add(page + i);
            }
        }

        // 是否展示上一页
        showPrevious = page != 1;

        // 是否展示下一页
        showNext = page != totalPage;

        // 是否展示第一页
        //contains包含
        showFirstPage = !pages.contains(1);

        // 是否展示最后一页
        showEndPage = !pages.contains(totalPage);
    }
}

