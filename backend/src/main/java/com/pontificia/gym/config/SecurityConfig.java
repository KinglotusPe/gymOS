package com.pontificia.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/asistencias/escanear-dni", "/asistencias/escanear"))
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos públicos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Ruta de login
                .requestMatchers("/login").permitAll()
                // API REST y Reportes JasperReports públicos
                .requestMatchers("/api/**", "/reportes/**").permitAll()
                // Gestión de Usuarios y Roles (Solo Admin General)
                .requestMatchers("/usuarios/**").hasAuthority("ROLE_ADMIN")
                // Portal del Socio / Cliente
                .requestMatchers("/portal/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")
                // Módulos de Entrenadores, Evaluaciones Físicas y Rutinas
                .requestMatchers("/entrenadores/**", "/seguimientos/**", "/rutinas/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA", "ROLE_ENTRENADOR")
                // Reportes oficiales JasperReports
                .requestMatchers("/reportes/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")
                // Módulos Administrativos y Operativos
                .requestMatchers("/clientes/**", "/membresias/**", "/pagos/**", "/asistencias/**", "/caja/**", "/tienda/**", "/casilleros/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")
                // Panel Principal / Dashboard
                .requestMatchers("/", "/inicio").hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
