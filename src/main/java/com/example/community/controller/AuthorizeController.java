package com.example.community.controller;

import com.example.community.dto.AccessTokenDTO;
import com.example.community.dto.GithubUser;
import com.example.community.model.User;
import com.example.community.provider.GithubProvider;
import com.example.community.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


@Controller
@Slf4j
public class AuthorizeController {
    @Autowired
    private GithubProvider githubProvider;
    @Value("${github.client.id}")
    private String clientId;
    @Value("${github.client_secret}")
    private String clientSecret;
    @Value("${github.redirect_uri}")
    private String redirectUri;

    @Autowired
    private UserService userService;

    @GetMapping("/callback")
    public <response> String callback(@RequestParam(name = "code") String code,
                                      @RequestParam(name = "state") String state,
                                      HttpServletResponse response) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setState(state);
        //获取accestoken值
        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        //json解析
        GithubUser githubUser = githubProvider.getUser(accessToken);

        if (githubUser != null && githubUser.getId() != null) {
            //登录成功 写session和cookie
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setAvatarUrl(githubUser.getAvatar_url());
            userService.createOrUpdate(user);
            Cookie cookie = new Cookie("token", token);
            //设定cookie有效时间
            cookie.setMaxAge(60 * 60 * 24 * 30 * 6);
            response.addCookie(cookie);
            // request.getSession().setAttribute("user", githubUser);
            return "redirect:/";

        } else {
            log.error("callback get  github error,{}", githubUser);
            //登录失败,重新登录
            return "redirect:/";
        }

    }

    @GetMapping("/logout")
    //退出登录
    public String logout(HttpServletRequest request,
                         //Response 设置cookie
                         HttpServletResponse response) {
        request.getSession().removeAttribute("user");
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}
