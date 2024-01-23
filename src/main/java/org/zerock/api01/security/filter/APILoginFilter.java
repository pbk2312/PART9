package org.zerock.api01.security.filter;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;


//  AbstractAuthenticationProcessingFilter 는 로그인 처리를 담당하기 때문에 다른 필터들과 달리 로그인을 처리하는 경로에 대한 설정과
// 실제 인증 처리를 담당하는 AuthenticationManager 객체의 설정이 필수로 필요합니다.
// APILoginFilter 는 POST 방식으로 요청이 들어올 때 JSON 문자열을 처리하는 parseRequestJSON() 메소드를 구성하고 mid 와 mpw 를 확인할 수 있도록 합니다
@Log4j2
public class APILoginFilter extends AbstractAuthenticationProcessingFilter {

    public APILoginFilter(String defaultFilterProccessUrl) {
        super(defaultFilterProccessUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException
            , IOException, ServletException {

        log.info("APILoginFilter--------");

        if (request.getMethod().equalsIgnoreCase("GET")) {
            log.info("GET METHOD NOT SUPPORT");
            return null;
        }
        Map<String, String> jsonData = paraRequestJSON(request);

        log.info(jsonData);

        return null;


    }

    private Map<String, String> paraRequestJSON(HttpServletRequest request) {

        // JSON 데이터를 분석해서 mid, mpw 전달 값을 Map 으로 처리
        try (Reader reader = new InputStreamReader(request.getInputStream())) {

            Gson gson = new Gson();

            return gson.fromJson(reader, Map.class);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;

    }


}
