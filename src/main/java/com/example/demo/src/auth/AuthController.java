package com.example.demo.src.auth;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.config.BaseResponseStatus;
import com.example.demo.src.auth.model.PostLoginReq;
import com.example.demo.src.auth.model.PostLoginRes;
import com.example.demo.utils.JwtService;
import com.example.demo.utils.ValidationRegex;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private final AuthProvider authProvider;
    @Autowired
    private final AuthService authService;
    @Autowired
    private final JwtService jwtService;

    public AuthController(AuthProvider authProvider, AuthService authService, JwtService jwtService){
        this.authProvider = authProvider;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @ResponseBody
    @PostMapping("/login")
    public BaseResponse<PostLoginRes> logIn(@RequestBody PostLoginReq postLoginReq) {
        try{
            HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
            Cookie[] cookies = request.getCookies();
            boolean is_remember_member = false;
            int userIdx = 0;
            String jwt = "";
            if(cookies != null) {
                for (Cookie c : cookies) {
                    if (c.getName() == "jwt") {
                        is_remember_member = true;
                        jwt = c.getValue();
                        userIdx = jwtService.getUserIdxByJwt(jwt);
                    }
                }
                if (is_remember_member)
                    return new BaseResponse<>(new PostLoginRes(userIdx, jwt));
            }
            if(postLoginReq.getEmail() == null)
                return new BaseResponse<>(BaseResponseStatus.POST_USERS_EMPTY_EMAIL);
            if(postLoginReq.getPwd() == null)
                return new BaseResponse<>(BaseResponseStatus.POST_USERS_EMPTY_PASSWORD);
            if(!ValidationRegex.isRegexEmail(postLoginReq.getEmail()))
                return new BaseResponse<>(BaseResponseStatus.POST_USERS_INVALID_EMAIL);

            PostLoginRes postLoginRes = authService.logIn(postLoginReq);
            return new BaseResponse<>(postLoginRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
