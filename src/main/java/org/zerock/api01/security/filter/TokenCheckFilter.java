package org.zerock.api01.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zerock.api01.security.exception.AccessTokenException;
import org.zerock.api01.util.JWTUtil;

import java.io.IOException;
import java.util.Map;

// 현재 사용자가 로그인한 사용자인지 체크하는 로그인 체크필터와 유사하게 JWT 토큰을 검사하는 역할을 위해서 사용
// JWTUtil 의 validateToken() 가능 추가

@Log4j2
@RequiredArgsConstructor
public class TokenCheckFilter extends OncePerRequestFilter { //OncePerRequestFilter:하나의 요청에 대해서 한번씩 동작하는 필터로 서블릿 API의 필터와 유사

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response
            , FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);

            return;
        }

        log.info("Token Check Filter ...........");
        log.info("JWTUtil: " + jwtUtil);

        // 자동으로 브라우저에 에러메시지를 상테 코드와 함께 전송
        try {
            validateAccessToken(request);
            filterChain.doFilter(request, response);

        } catch (AccessTokenException accessTokenException) {
            accessTokenException.sendResponseError(response);
        }

    }

    // Access Token  검증 validateAccessToken()
    private Map<String, Object> validateAccessToken(HttpServletRequest request) throws AccessTokenException {

        String headerStr = request.getHeader("Authorization");

        if (headerStr == null || headerStr.length() < 0) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
        }

        // Bearer 생략
        String tokenType = headerStr.substring(0, 6);
        String tokenStr = headerStr.substring(7);

        if (tokenType.equalsIgnoreCase("Bearer") == false) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADTYPE);
        }

        try {
            Map<String, Object> values = jwtUtil.validateToken(tokenStr);
            return values;
        } catch (MalformedJwtException malformedJwtException) {
            log.error("MalFormedJwtException..........");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.MALFORM);
        } catch (SignatureException signatureException) {
            log.error("SignatureException-------");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADSIGN);
        } catch (ExpiredJwtException expiredJwtException) {
            log.error("ExpiredJwtException-----");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EXPIRED);
        }


    }


}
