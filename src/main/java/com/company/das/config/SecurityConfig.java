package com.company.das.config;

import com.company.das.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

    	 http
         .authorizeHttpRequests(auth -> auth

             .requestMatchers(
                     "/",
                     "/css/**",
                     "/js/**",
                     "/images/**"
             ).permitAll()

             .anyRequest()
             .authenticated()
         )

                .formLogin(form -> form

                        .loginPage("/")

                        .loginProcessingUrl("/perform-login")

                        .defaultSuccessUrl("/dashboard", true)

                        .failureUrl(
                                "/?error=true"
                        )

                        .permitAll()
                )

                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl("/")

                        .permitAll()
                );

        return http.build();
    }
}