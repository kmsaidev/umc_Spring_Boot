package com.example.demo.src.auth;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.auth.model.PostLoginReq;
import com.example.demo.src.auth.model.PostLoginRes;
import com.example.demo.src.auth.model.User;
import com.example.demo.utils.JwtService;
import com.example.demo.utils.SHA256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static com.example.demo.config.BaseResponseStatus.*;

@Service
public class AuthService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthDao authDao;
    private final AuthProvider authProvider;
    private final JwtService jwtService;

    @Autowired
    public AuthService(AuthDao authDao, AuthProvider authProvider, JwtService jwtService) {
        this.authDao = authDao;
        this.authProvider = authProvider;
        this.jwtService = jwtService;
    }

    public PostLoginRes logIn(PostLoginReq postLoginReq) throws BaseException {
        User user = authDao.getPwd(postLoginReq);
        String encryptPwd;
        try{
            encryptPwd = new SHA256().encrypt(postLoginReq.getPwd());
        }
        catch (Exception exception) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        if(user.getPwd().equals(encryptPwd) && postLoginReq.isRemember()) {
            int userIdx = user.getUserIdx();
            String jwt = jwtService.createJwt(userIdx);

            HttpServletResponse response =  ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
            Cookie cookie = new Cookie("jwt", jwt);
            cookie.setMaxAge(60 * 60 * 30 * 60);
            response.addCookie(cookie);

            return new PostLoginRes(userIdx, jwt);
        }
        if(user.getPwd().equals(encryptPwd)){
            int userIdx = user.getUserIdx();
            String jwt = jwtService.createJwt(userIdx);
            return new PostLoginRes(userIdx, jwt);
        }
        else{
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }
}
