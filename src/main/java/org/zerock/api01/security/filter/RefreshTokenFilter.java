package org.zerock.api01.security.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zerock.api01.util.JWTUtil;

import java.io.IOException;

// 토큰 갱신에 사용할 경로('/refreshToken') 와 JWTUtil 을 주입받도록 설계,해당 경로가 아닌 경우에는 다음 순서의 필터가 실행되도록 구성합니다.

@Log4j2
@RequiredArgsConstructor
public class RefreshTokenFilter extends OncePerRequestFilter {

    private final String refreshPath;

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.equals(refreshPath)) {
            log.info("skip refresh token filter......");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Refresh Token Filter...run......1");


    }


}
