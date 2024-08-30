package com.example.jwt.filter;

import com.example.jwt.dto.CustomUserDetails;
import com.example.jwt.dto.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;

    @Autowired
    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
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

    @Override
    //성공적으로 인증이 된다면
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, Authentication authentication){
        //UserDetailsS
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        //인증된 사용자의 주체를 반환한다.

        String username = customUserDetails.getUsername();
        //사용자의 이름을 저장

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        //authentication 객체에서 사용자가 가진 권한 목록을 가져온다.
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        //authorities 컬렉션을 순회하기 위해 이터레이터 생성
        GrantedAuthority auth = iterator.next();
        //이터레이터에서 첫 번째 권한을 가져옵니다.

        String role = auth.getAuthority();
        //auth 객체로 권한의 이름을 가져온다.

        String token = jwtUtil.createJwt(username, role, 60*60*10L);
        //JWT Util 객체를 사용해 JWT를 생성한다. (유효기간, 역할, 시간)등을 받아 생성.
        response.addHeader("Authorization", "Bearer " + token);
        //응답으로 넘긴다.
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }


}
