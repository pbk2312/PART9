package org.zerock.api01.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.zerock.api01.security.APIUserDetailsService;
import org.zerock.api01.security.filter.APILoginFilter;
import org.zerock.api01.security.filter.RefreshTokenFilter;
import org.zerock.api01.security.filter.TokenCheckFilter;
import org.zerock.api01.security.handler.APILoginSuccessHandler;
import org.zerock.api01.util.JWTUtil;


// 시큐리티 설정
@Log4j2
@RequiredArgsConstructor
@Configuration
@EnableWebMvc
@EnableMethodSecurity(prePostEnabled = true)
public class CustomSecurityConfig {

    // 주입
    private final APIUserDetailsService apiUserDetailsService;
    private final JWTUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {

        // 정적으로 동작하는 파일들에는 굳이 시큐리티 적용 X
        log.info("---------web configure---------");

        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());


    }


    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {


        log.info("-----------------------configuration---------------------");

        // api 로 시작하는  모든 경로는 TokenCheckFilter 동작
        http.addFilterBefore(
                tokenCheckFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class

        );

        //refreshToken 호출 처리
        http.addFilterBefore(new RefreshTokenFilter("/refreshToken", jwtUtil),
                TokenCheckFilter.class);



        //AuthenticationManager설정
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(apiUserDetailsService).passwordEncoder(passwordEncoder());
        // Get AuthenticationManager
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        //반드시 필요
        http.authenticationManager(authenticationManager);

        //APILoginFilter
        APILoginFilter apiLoginFilter = new APILoginFilter("/generateToken");
        apiLoginFilter.setAuthenticationManager(authenticationManager);


        //APILoginSuccessHandler
        APILoginSuccessHandler successHandler = new APILoginSuccessHandler(jwtUtil);
        //SuccessHandler 세팅
        apiLoginFilter.setAuthenticationSuccessHandler(successHandler);

        //APILoginFilter의 위치 조정
        http.addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class);


        http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());

        http.sessionManagement(httpSecuritySessionManagementConfigurer -> {
            httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });


        return http.build();
    }

    private TokenCheckFilter tokenCheckFilter(JWTUtil jwtUtil) {
        return new TokenCheckFilter(jwtUtil);

    }
}



