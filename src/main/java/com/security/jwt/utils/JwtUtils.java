package com.security.jwt.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Author shitie
 * @Date 2020/11/4 0004 下午 1:02
 */
public class JwtUtils {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    //密钥，用于signature（签名）部分解密
    private static final String PRIMARY_KEY = "jewelers";
    //签发者
    private static final String ISS = "Rich.st";
    // 添加角色的key
    private static final String ROLE_CLAIMS = "role";

    // 过期时间是3600秒，既是1个小时
    private static final long EXPIRATION = 3600L;

    // 选择了记住我之后的过期时间为7天
    private static final long EXPIRATION_REMEMBER = 604800L;

    /**
     *
     * 创建Token
     *
     * @param username
     * @param isRememberMe
     * @return java.lang.String
     */
    public static String createToken(String username, List<String> roles, boolean isRememberMe) {
        long expiration = isRememberMe ? EXPIRATION_REMEMBER : EXPIRATION;
        HashMap<String, Object> map = new HashMap<>();
        map.put(ROLE_CLAIMS, roles);
        return Jwts.builder()
                //采用HS512算法对JWT进行的签名,PRIMARY_KEY是我们的密钥
                .signWith(SignatureAlgorithm.HS512, PRIMARY_KEY)
                //设置角色名
                .setClaims(map)
                //设置发证人
                .setIssuer(ISS)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .compact();
    }

    /**
     * description: 从token中获取用户名
     *
     * @param token
     * @return java.lang.String
     */
    public static String getUsername(String token){
        return getTokenBody(token).getSubject();
    }

    // 获取用户角色
    public static List<String> getUserRole(String token){
        return (List<String>) getTokenBody(token).get(ROLE_CLAIMS);
    }

    /**
     * description: 判断Token是否过期
     *
     * @param token
     * @return boolean
     */
    public static boolean isExpiration(String token){
        return getTokenBody(token).getExpiration().before(new Date());
    }

    /**
     * description:　获取
     *
     * @param token
     * @return io.jsonwebtoken.Claims
     */
    private static Claims getTokenBody(String token){
        return Jwts.parser()
                .setSigningKey(PRIMARY_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}
