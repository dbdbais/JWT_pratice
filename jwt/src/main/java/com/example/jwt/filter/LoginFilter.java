package com.example.jwt.filter;

import com.example.jwt.dto.CustomUserDetails;
import com.example.jwt.dto.JWTUtil;
import com.example.jwt.entity.RefreshEntity;
import com.example.jwt.entity.RefreshRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;

    private final RefreshRepository refreshRepository;

    @Autowired
    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshRepository refreshRepository){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        //클라이언트 요청에서 username, password 추출
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        System.out.println(username);

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        //token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }
    
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //24시간 활성화

        //cookie.setSecure(true); 
        //HTTPS 쓸 경우 활성화
        //cookie.setPath("/");
        // 쿠키가 적용될 범위 설정
        cookie.setHttpOnly(true);
        //JS단에서 접근 못하게 막는다

        return cookie;
    }

    //현재 RefreshEntity 추가하는 함수
    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }

    @Override
    //성공적으로 인증이 된다면
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, Authentication authentication){
//        //UserDetailsS
//        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
//        //인증된 사용자의 주체를 반환한다.
//
//        String username = customUserDetails.getUsername();
//        //사용자의 이름을 저장
//
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        //authentication 객체에서 사용자가 가진 권한 목록을 가져온다.
//        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
//        //authorities 컬렉션을 순회하기 위해 이터레이터 생성
//        GrantedAuthority auth = iterator.next();
//        //이터레이터에서 첫 번째 권한을 가져옵니다.
//
//        String role = auth.getAuthority();
//        //auth 객체로 권한의 이름을 가져온다.
//
//        String token = jwtUtil.createJwt(username, role, 60*60*10L);
//        //JWT Util 객체를 사용해 JWT를 생성한다. (유효기간, 역할, 시간)등을 받아 생성.
//        response.addHeader("Authorization", "Bearer " + token);
//        //응답으로 넘긴다.



        //유저 정보
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성
        String access = jwtUtil.createJwt("access", username, role, 600000L);
        String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        //Server에 Refresh 토큰 저장
        addRefreshEntity(username, refresh, 86400000L);

        //응답 설정
        response.setHeader("access", access);
        //헤더에 엑세스 토큰 넣어줌
        response.addCookie(createCookie("refresh", refresh));
        //쿠키에 리프레시 토큰 넣어줌
        response.setStatus(HttpStatus.OK.value());
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }


}
