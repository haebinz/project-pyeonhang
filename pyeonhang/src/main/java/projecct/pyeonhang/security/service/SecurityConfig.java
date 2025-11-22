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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import projecct.pyeonhang.attendance.service.AttendanceService;
import projecct.pyeonhang.common.filter.CustomLogoutFilter;
import projecct.pyeonhang.common.filter.JWTFilter;
import projecct.pyeonhang.common.filter.LoginFilter;
import projecct.pyeonhang.common.utils.JWTUtils;
import projecct.pyeonhang.users.dto.UserSecureDTO;
import projecct.pyeonhang.users.service.UserServiceDetails;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserServiceDetails serviceDetails;
    private final JWTUtils jwtUtils;
    private final AttendanceService attendanceService;


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
        AuthenticationConfiguration configuration = http.getSharedObject(AuthenticationConfiguration.class);

        // loginFilter에서 인증처리 하기 위한 매니저 생성
        AuthenticationManager manager = this.authenticationManager(configuration);

        LoginFilter loginFilter = new LoginFilter(manager, jwtUtils,attendanceService);
        loginFilter.setFilterProcessesUrl("/api/v1/user/login");

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(this.configurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                //인증/비인증 경로 처리
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/**").permitAll()
                                        .requestMatchers("/user/login").permitAll()
                                        .requestMatchers("/user/login/**").permitAll()// 인증처리 안함 패스
                                        .requestMatchers("/api/v1/user/login").permitAll() // 인증처리 안함 패스
                                        .requestMatchers("/user/login/error").permitAll()
                                        .requestMatchers("/user/logout/**").permitAll()
                                        .requestMatchers("/user/add").permitAll()
                                        .requestMatchers("/api/v1/refresh").permitAll()
                                        .requestMatchers("/api/v1/email/**").permitAll()
                                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                        .requestMatchers("/.well-known/**").permitAll()
                                        .requestMatchers("/favicon.ico").permitAll()
                                        .requestMatchers("/img/**", "/css/**", "/js/**", "/static/**", "/files/**").permitAll()
                                        .requestMatchers("/api/v1/board", "/api/v1/board/*").permitAll()
                                        .requestMatchers("/api/v1/crawl/**").permitAll()
                                        .requestMatchers("/api/v1/board/**").hasRole("USER")
                                        .requestMatchers("/api/v1/crawl/**/comment/**").hasRole("USER")
                                        .requestMatchers("/api/v1/crawl/*/comment/**").hasRole("USER")
                                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                        .anyRequest().authenticated()
                )
                // LoginFilter 전에 JWTFilter를 실행
                .addFilterBefore(new JWTFilter(jwtUtils), LoginFilter.class)
                // UsernamePasswordAuthenticationFilter 이거 대신 LoginFilter를 실행해라
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new CustomLogoutFilter(jwtUtils), LogoutFilter.class)
                // 세션 유지 안함
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(withDefaults());

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
        config.setAllowedOrigins(List.of("http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:4000", "https://pyeonhang.world",
                "http://localhost:4001"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
