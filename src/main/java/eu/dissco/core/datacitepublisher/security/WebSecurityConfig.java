package eu.dissco.core.datacitepublisher.security;


import eu.dissco.core.datacitepublisher.Profiles;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@Profile(Profiles.PUBLISH)
public class WebSecurityConfig  {

  private final JwtAuthConverter jwtAuthConverter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
        .requestMatchers(EndpointRequest.to(HealthEndpoint.class))
        .permitAll()
        .anyRequest()
        .hasRole("orchestration-admin"));

    http.oauth2ResourceServer(jwtoauth2ResourceServer -> jwtoauth2ResourceServer.jwt((
        jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)
    )));

    http.sessionManagement(sessionManagement -> sessionManagement
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

}
