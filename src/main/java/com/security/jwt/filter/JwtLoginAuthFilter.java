package com.security.jwt.filter;


import com.alibaba.fastjson.JSON;
import com.security.jwt.common.ResultEnum;
import com.security.jwt.common.ResultVO;
import com.security.jwt.model.LoginUser;
import com.security.jwt.pojo.JwtAuthUser;
import com.security.jwt.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author shitie
 * @Date 2020/11/4 0004 下午 1:20
 */
public class JwtLoginAuthFilter extends UsernamePasswordAuthenticationFilter {

    /*
     AuthenticationManager： 用户认证的管理类，所有的认证请求（比如login）都会通过提交一个token给AuthenticationManager的authenticate()方法来实现。
     当然事情肯定不是它来做，具体校验动作会由AuthenticationManager将请求转发给具体的实现类来做。根据实现反馈的结果再调用具体的Handler来给用户以反馈。
      */
    private AuthenticationManager authenticationManager;

    private ThreadLocal<Boolean> rememberMe = new ThreadLocal<>();

    public JwtLoginAuthFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        // 设置该过滤器地址
        super.setFilterProcessesUrl("/auth/login");
    }

    /**
     * description: 登录验证
     *
     * @param request
     * @param response
     * @return org.springframework.security.core.Authentication
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername(request.getParameter("username"));
        loginUser.setPassword(request.getParameter("password"));
        loginUser.setRememberUser(Boolean.parseBoolean(request.getParameter("rememberMe")));
        System.out.println(loginUser.toString());
        rememberMe.set(loginUser.getRememberUser());
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword(), new ArrayList<>())
        );

    }

    /**
     * description: 登录验证成功后调用，验证成功后将生成Token，并重定向到用户主页home
     * 与AuthenticationSuccessHandler作用相同
     *
     * @param request
     * @param response
     * @param chain
     * @param authResult
     * @return void
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        // 查看源代码会发现调用getPrincipal()方法会返回一个实现了`UserDetails`接口的对象，这里是JwtAuthUser
        JwtAuthUser jwtUser = (JwtAuthUser) authResult.getPrincipal();
        System.out.println("JwtAuthUser:" + jwtUser.toString());
        boolean isRemember = rememberMe.get();
        List<String> roles = new ArrayList<>();
        Collection<? extends GrantedAuthority> authorities = jwtUser.getAuthorities();
        for (GrantedAuthority authority : authorities){
            roles.add(authority.getAuthority());
        }
        System.out.println("roles:"+roles);
        String token = JwtUtils.createToken(jwtUser.getUsername(), roles, isRemember);
        System.out.println("token:"+token);
        // 重定向无法设置header,这里设置header只能设置到/auth/login界面的header
        //response.setHeader("token", JwtTokenUtils.TOKEN_PREFIX + token);

        // 登录成功重定向到home界面
        // 这里先采用参数传递
        response.sendRedirect("/home?token="+token);
    }

    /**
     * description: 登录验证失败后调用，这里直接Json返回，实际上可以重定向到错误界面等
     * 与AuthenticationFailureHandler作用相同
     *
     * @param request
     * @param response
     * @param failed
     * @return void
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(JSON.toJSONString(ResultVO.result(ResultEnum.USER_LOGIN_FAILED,false)));
    }

}
