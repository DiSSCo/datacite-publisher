package eu.dissco.core.datacitepublisher.security;

import eu.dissco.core.datacitepublisher.Profiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
@Profile({Profiles.PUBLISH, Profiles.WEB})
public class MethodSecurityConfig {

  @Bean
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    return new DefaultMethodSecurityExpressionHandler();
  }

}

