package sample.spring.serialization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "sample.spring.serialization")
public class SerializationApp {

	@Bean
	public Jackson2ObjectMapperBuilder mapperBuilder() {
		Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder = new Jackson2ObjectMapperBuilder();
		jackson2ObjectMapperBuilder.serializers(new UserSerializer());
		return jackson2ObjectMapperBuilder;
	}

	public static void main(String[] args) {
		SpringApplication.run(SerializationApp.class, args);
	}
}

@RestController
class SerializationController {
	@GetMapping("/user")
	public User user() {
		return new User("sample");
	}
}

