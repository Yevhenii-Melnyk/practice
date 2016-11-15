package spring.reactive;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import spring.reactive.entity.BootStarter;
import spring.reactive.entity.CustomArgument;

@RestController
public class HomeController {

	@RequestMapping(value = "/")
	public Mono<BootStarter> starter() {
		return Mono.just(new BootStarter("spring-boot-starter-web-reactive", "Spring Boot Web Reactive"));
	}

	@RequestMapping(value = "/custom-arg")
	public Mono<String> customArg(CustomArgument custom) {
		return Mono.just(custom.getCustom());
	}

}
