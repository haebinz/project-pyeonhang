package projecct.pyeonhang.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import projecct.pyeonhang.common.utils.JWTUtils;
import projecct.pyeonhang.users.service.UserServiceDetails;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserServiceDetails serviceDetails;
    private final JWTUtils jwtUtils;


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web ->
                web.ignoring()
                        .requestMatchers("/static/imgs/**")
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                        .requestMatchers("/webjars/**");
        //마지막 명령어는 스프링 리소스 관련 처리
                /*
                  * 1. classpath:/META-INF/resources/   //라이브러리 리스소들 폴더
                    2. classpath:/resources/
                    3. classpath:/static/
                    4. classpath:/public/
                 */

    }

    //보안처리
    /**
     * scutiry 6 특증
     * 메서드 파라메터를 전부 함수형 인터페이스 처리
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                //인증/비인증 경로 처리
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/user/login").permitAll() // 인증처리 안함 패스
                                        .requestMatchers("/user/login/error").permitAll()
                                        .requestMatchers("/user/logout/**").permitAll()
                                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                        .requestMatchers("/.well-known/**").permitAll()
                                        .requestMatchers("/favicon.ico").permitAll()
                                        .requestMatchers("/img/**","/css/**","/js/**").permitAll()
                                        .requestMatchers("/api/v1/**").permitAll()
                                        .requestMatchers("/crawl/**").permitAll()
                                        .requestMatchers("/static/**").permitAll()
                                        .requestMatchers("/files/**").permitAll()
                                        .anyRequest().authenticated()
                )//로그인 처리
                .formLogin(
                        form ->
                                form.loginPage("/user/login")  //내가만든 로그인 페이지 경로
                                        .loginProcessingUrl("/login/proc")   // 로그인 처리 시작 경로

                ).logout(
                        out->
                                out.logoutUrl("/logout")  // AntPathRequestMatcher 대신 직접 URL 사용
                                        .invalidateHttpSession(true)  // spring session 제거
                                        .deleteCookies("JSESSIONID") //세션 Id 제거
                                        .clearAuthentication(true)  // 로그인 객체 삭제

                )
                .exceptionHandling(exp ->
                        //비로그인 상태에서 api  호출 시 오류
                        exp.defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")
                                //로그인이지만 권한 없은 api 호출 시 오류
                        ).defaultAccessDeniedHandlerFor(
                                new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN),
                                request -> request.getRequestURI().startsWith("/api/")
                        ));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(serviceDetails);
        provider.setPasswordEncoder(bcyPasswordEncoder());
        return provider;
    }



    //패스워드 암호화 객체 설정
    @Bean
    public PasswordEncoder bcyPasswordEncoder(){
        // 단방향 암호화 방식.  복호화 없음.  값 비교는 가능
        return  new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource configurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        //헤더 설정
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type",
                "X-Requested-With", "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        //메서드 설정
        config.setAllowedMethods(List.of("GET",  "POST", "DELETE", "PUT", "PATCH",  "OPTIONS"));
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001","http://localhost:4000","http://localhost:4001"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
