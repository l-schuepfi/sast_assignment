package com.djamware.mynotes.configs;

import com.djamware.mynotes.repositories.RoleRepository;
import com.djamware.mynotes.repositories.UserRepository;
import com.djamware.mynotes.services.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomizeAuthenticationSuccessHandler customizeAuthenticationSuccessHandler;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public WebSecurityConfig(CustomizeAuthenticationSuccessHandler customizeAuthenticationSuccessHandler, BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository, RoleRepository roleRepository){
        this.customizeAuthenticationSuccessHandler = customizeAuthenticationSuccessHandler;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Bean
    public UserDetailsService jpaUserDetails() {
        return new CustomUserDetailsService(userRepository, roleRepository, bCryptPasswordEncoder);
    }

    @Bean
    public HttpFirewall allowUrlEncodedDoubleSlashFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        // retrieve builder from httpSecurity
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(jpaUserDetails())
                .passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.build();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(
                auth-> auth.requestMatchers("/notes").hasAuthority("ADMIN")
                .requestMatchers("/login").permitAll()
                .requestMatchers("/signup").permitAll()
                .requestMatchers("/notes/**").hasAuthority("ADMIN")
                .requestMatchers("/h2/console").permitAll()
                .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .successHandler(customizeAuthenticationSuccessHandler).loginPage("/login")
                        .failureUrl("/login?error=true").usernameParameter("email").passwordParameter("password")
                        )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout"))
                .exceptionHandling((exh -> exh.authenticationEntryPoint(
                        (request, response, ex) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
                )));

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web-> {web.ignoring().requestMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
            web.httpFirewall(allowUrlEncodedDoubleSlashFirewall());
        };
    }

}
