package com.example.community.schedule;

import com.example.community.cache.HotTagCache;
import com.example.community.mapper.QuestionMapper;
import com.example.community.model.Question;
import com.example.community.model.QuestionExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 热门话题  定时器 定时刷新
 */
@Component
@Slf4j
public class HotTagTasks {
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private HotTagCache hotTagCache;

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    //@Scheduled(cron="0 0 1 * * *")
    public void HotTagSchedule() {
        int offset = 0;
        int limit = 20;
        List<Question> list = new ArrayList<>();
        Map<String, Integer> tagMap = new HashMap<>();
        while (offset == 0 || list.size() == limit) {
            list = questionMapper.selectByExampleWithRowbounds(new QuestionExample(), new RowBounds(offset, limit));
            for (Question question : list) {
                String[] tags = StringUtils.split(question.getTag(), ",");
                for (String tag : tags) {
                    Integer priority = tagMap.get(tag);
                    if (priority != null) {
                        tagMap.put(tag, priority + 5 + question.getCommentCount());
                    } else {
                        tagMap.put(tag, 5 + question.getCommentCount());
                    }
                }
            }
            offset += limit;
        }
        hotTagCache.updateTags(tagMap);
        log.info("The time is now {}", new Date());
    }
}
