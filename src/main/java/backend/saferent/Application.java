package backend.saferent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "backend.saferent.repository")
@EnableElasticsearchRepositories(basePackages = "backend.saferent.search")
public class Application {

	@PostConstruct
	public void init() {
		// Все LocalDateTime.now() и временные метки — по времени Алматы
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Almaty"));
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
