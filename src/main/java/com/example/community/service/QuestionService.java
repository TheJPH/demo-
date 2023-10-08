package com.example.community.service;

import com.example.community.dto.PaginationDTO;
import com.example.community.dto.QuestionDTO;
import com.example.community.dto.QuestionQueryDTO;
import com.example.community.enums.CommentTypeEnum;
import com.example.community.exception.CustomizeErrorCode;
import com.example.community.exception.CustomizeException;
import com.example.community.mapper.*;
import com.example.community.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QuestionExtMapper questionExtMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 查找问题列表分页
     *
     * @param search
     * @param page
     * @param size
     * @return
     */
    public PaginationDTO list(String search, String tag, Integer page, Integer size) {
        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays.stream(tags).collect(Collectors.joining("|"));

        }


        PaginationDTO paginationDTO = new PaginationDTO();
        Integer totalPage;
        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        if (StringUtils.isNotBlank(search)) {
            questionQueryDTO.setSearch(search);
        }else{
            questionQueryDTO.setSearch(null);
        }
        questionQueryDTO.setTag(tag);

        Integer totalCount = questionExtMapper.countBySearch(questionQueryDTO);
        //确认页数
        if (totalCount % size == 0) {
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
        }
        //分页逻辑
        paginationDTO.setPagination(totalPage, page);
        //偏移量
        Integer offset = page < 1 ? 0 : size * (page - 1);
        //赋值 显示个数以及偏移量
        questionQueryDTO.setSize(size);
        questionQueryDTO.setPage(offset);
        /*
         * questionQueryDTO中seach size page 赋值
         * 构建查询语句
         * 通过向model中question进行查询 得出结果
         * */
        List<Question> questions = questionExtMapper.selectBySearch(questionQueryDTO);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        /*
         * foreach循环 得出元素
         * 缺少user元素 通过userMapper进行查询
         * 同时创建questionDTO 并完整赋值
         * 然后向questionDTOList赋值获得查询问题清单
         * 最后赋值给paginationDTO 向html 输出 并做好分页逻辑
         * */
        for (Question question : questions) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }
        paginationDTO.setData(questionDTOList);
        return paginationDTO;
    }

    /**
     * 通过用户id区分问题 分页
     * 我的问题
     *
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public PaginationDTO list(Long userId, Integer page, Integer size) {
        PaginationDTO paginationDTO = new PaginationDTO();
        Integer totalPage;
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria()
                .andCreatorEqualTo(userId);
        Integer totalCount = (int) questionMapper.countByExample(new QuestionExample());
        if (totalCount % size == 0) {
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
        }
        paginationDTO.setPagination(totalPage, page);
        //size*(page-1)
        Integer offset = size * (page - 1);
        QuestionExample Example = new QuestionExample();
        Example.createCriteria()
                .andCreatorEqualTo(userId);
        List<Question> questions = questionMapper.selectByExampleWithRowbounds(Example, new RowBounds(offset, size));
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Question question : questions) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }
        paginationDTO.setData(questionDTOList);
        return paginationDTO;
    }

    /**
     * 通过id查找问题
     *
     * @param id
     * @return
     */
    public QuestionDTO getById(Long id) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (question == null) {
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDTO);
        User user = userMapper.selectByPrimaryKey(question.getCreator());
        questionDTO.setUser(user);
        return questionDTO;
    }

    public void deleteById(Long id) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (question == null) {
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        CommentExample example = new CommentExample();
        example.createCriteria().andParentIdEqualTo(id)
                .andTypeEqualTo(CommentTypeEnum.QUESTION.getType());
        //找到问题回复
        List<Comment> comments = commentMapper.selectByExample(example);
        if (comments != null) {
            for (Comment comment : comments) {
                //找到二级评论
                CommentExample example1 = new CommentExample();
                example1.createCriteria().andParentIdEqualTo(id)
                        .andTypeEqualTo(CommentTypeEnum.COMMENT.getType());
                List<Comment> commentsons = commentMapper.selectByExample(example1);
                if (commentsons != null) {
                    //删除二级评论
                    for (Comment commentson : commentsons) {
                        //删除二级回复
                        commentMapper.deleteByPrimaryKey(commentson.getParentId());
                    }
                } else {
                    throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
                }
                //删除问题回复
                commentMapper.deleteByPrimaryKey(comment.getId());
            }
        } else {
            throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
        }
        //删除有关通知
        notificationMapper.deleteByPrimaryKey(id);
        //删除问题
        questionMapper.deleteByPrimaryKey(id);
    }

    /**
     * 创建 更新 问题
     *
     * @param question
     */
    public void createOrUpdate(Question question) {
        if (question.getId() == null) {
            //等于空 就创建 提交问题
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            question.setViewCount(0);
            question.setLikeCount(0);
            question.setCommentCount(0);
            questionMapper.insert(question);
        } else {
            //修改问题  更新
            Question updateQuestion = new Question();
            updateQuestion.setGmtModified(System.currentTimeMillis());
            updateQuestion.setTitle(question.getTitle());
            updateQuestion.setDescription(question.getDescription());
            updateQuestion.setTag(question.getTag());
            QuestionExample example = new QuestionExample();
            example.createCriteria()
                    .andIdEqualTo(question.getId());
            int update = questionMapper.updateByExampleSelective(updateQuestion, example);
            if (update != 1) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
        }
    }

    /**
     * 浏览次数
     *
     * @param id
     */
    public void incView(Long id) {

        Question question = new Question();
        question.setId(id);
        question.setViewCount(1);
        questionExtMapper.incView(question);
    }


    /**
     * 相关推荐
     * 通过查找id tag 找出相关问题进行推荐
     *
     * @param queryDTO
     * @return
     */
    public List<QuestionDTO> selectRelated(QuestionDTO queryDTO) {
        if (StringUtils.isBlank(queryDTO.getTag())) {
            return new ArrayList<>();
        }
        String[] tags = StringUtils.split(queryDTO.getTag(), ",");
        String regexpTag = Arrays.stream(tags).collect(Collectors.joining("|"));
        Question question = new Question();
        question.setId(queryDTO.getId());
        question.setTag(regexpTag);
        /*
         * 通过对问题的标签和编号进行查询
         * 并得出拥有相同标签的问题
         *
         * */
        List<Question> questions = questionExtMapper.selectRelated(question);
        List<QuestionDTO> questionDTOS = questions.stream().map(q -> {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(q, questionDTO);
            return questionDTO;
        }).collect(Collectors.toList());
        return questionDTOS;
    }

}
