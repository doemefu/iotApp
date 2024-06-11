package ch.furchert.iotapp.security;

import ch.furchert.iotapp.security.jwt.AuthEntryPointJwt;
import ch.furchert.iotapp.security.jwt.AuthTokenFilter;
import ch.furchert.iotapp.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;


@Configuration
@EnableMethodSecurity
// (securedEnabled = true,
// jsr250Enabled = true,
// prePostEnabled = true) // by default
public class WebSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    //version 2y is the most secure
    //strength 10 is standard and means 2^10 rounds
    //SecureRandom is used to generate a salt for the hash
    //Custom RNG: By providing your own SecureRandom instance, you have control over the random number generator (RNG)
    // used for salt generation. This can be useful if you have specific requirements for the RNG, such as using a
    // hardware-based RNG or a specific algorithm.
    //BCrypt hash string will look like: $2<a/b/x/y>$[strength]$[22 character salt][31 character hash]
    //peppers won't be implemented as it is not recommended for todays algorithms
    //source: https://stackoverflow.com/questions/16891729/best-practices-salting-peppering-passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 10, new SecureRandom());
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        logger.debug("WebSecurityConfig.authenticationProvider start");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        logger.debug("WebSecurityConfig.authenticationProvider end");

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.debug("WebSecurityConfig.filterChain start");

        http
                .csrf(csrf -> csrf.
                        csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(request -> {
                            // Disable CSRF for API paths for debugging
                            return !request.getServletPath().startsWith("/api/auth/login") &&
                                    !request.getServletPath().startsWith("/api/auth/register");
                        }))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/get/**").permitAll()
                                .requestMatchers("/api/user-management/forgotPassword").permitAll()
                                .requestMatchers("/api/user-management/resetPassword").permitAll()
                                .requestMatchers("/api/ws/**").permitAll()
                                .anyRequest().authenticated()
                )
                /*
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .deleteCookies("XSRF-TOKEN")
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl("/api/auth/logout/success"))
                */
        ;

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        logger.debug("WebSecurityConfig.filterChain end");

        return http.build();
    }

    //not quite clear what's needed here
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://furchert.ch", "http://localhost:80", "https://localhost:443", "https://localhost:33"));
        configuration.setAllowedMethods(Arrays.asList("PATCH", "GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "Requestor-Type", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(Arrays.asList("ResponseMessage", "X-Get-Header", "X-XSRF-TOKEN"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    //is this needed?
    @Bean
    public UserDetailsService userDetailsService() {

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}