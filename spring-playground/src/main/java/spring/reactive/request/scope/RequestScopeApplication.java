package spring.reactive.request.scope;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@SpringBootApplication
@ComponentScan(basePackages = {"spring.reactive.request.scope"})
public class RequestScopeApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequestScopeApplication.class);
	}

	@Bean
	@Scope("prototype")
	public RandomString prototypeRandomString() {
		return new RandomString("base");
	}

	@Bean
	@Scope("request")
	public RandomString requestRandomString() {
		return new RandomString("base");
	}

}

@RestController
class Controller {

	@GetMapping("/scoped")
	public void get() {
		System.out.println("~~~~~~~~~~~~~~~~");
		System.out.println(getRandomPrototypeString());
		System.out.println(getRandomPrototypeString());
		System.out.println("################");
		System.out.println(getRandomRequestString());
		System.out.println(getRandomRequestString());
		System.out.println("~~~~~~~~~~~~~~~~");
	}

	@Lookup("prototypeRandomString")
	public RandomString getRandomPrototypeString() {
		throw new UnsupportedOperationException();
	}

	@Lookup("requestRandomString")
	public RandomString getRandomRequestString() {
		throw new UnsupportedOperationException();
	}

}

class RandomString {
	private String str;

	public RandomString(String str) {
		this.str = str + new Random().nextInt();
	}

	@Override
	public String toString() {
		return "RandomString{" +
				"str='" + str + '\'' +
				'}';
	}
}
