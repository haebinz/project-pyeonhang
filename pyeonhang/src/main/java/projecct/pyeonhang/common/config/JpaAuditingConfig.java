package projecct.pyeonhang.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.Optional;


@Configuration
@EnableJpaAuditing(dateTimeProviderRef="auditorDateTimeProvider")
public class JpaAuditingConfig {
    @Bean(name="auditorDateTimeProvider")
    public DateTimeProvider auditorDateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now());
    }
}
