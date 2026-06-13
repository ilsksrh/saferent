package backend.saferent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "backend.saferent.repository")
@EnableElasticsearchRepositories(basePackages = "backend.saferent.search")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
