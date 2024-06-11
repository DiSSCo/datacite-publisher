package eu.dissco.core.datacitepublisher.security;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig  {

  private final JwtAuthConverter jwtAuthConverter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
        .anyRequest().authenticated());

    http.oauth2ResourceServer(jwtoauth2ResourceServer -> jwtoauth2ResourceServer.jwt((
        jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)
    )));

    http.sessionManagement(sessionManagement -> sessionManagement
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

}
