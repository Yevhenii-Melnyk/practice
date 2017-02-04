package sample.spring.error.view;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@SpringBootApplication(scanBasePackages = "sample.spring.error.view")
public class ErrorViewApp {

	public static void main(String[] args) {
		SpringApplication.run(ErrorViewApp.class, args);
	}
}

@Controller
@RequestMapping("/")
class ErrorController {

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView index() {
		System.out.println("HOME");
		return new ModelAndView("home.html");
	}

	@RequestMapping(value = "/api/person/{person}", produces = "application/json", method = RequestMethod.GET)
	@ResponseBody
	public Person person(@PathVariable(value = "person") String person) {
		if (!"123".equals(person)) {
			return new Person(person);
		} else {
			throw new MyCustomException("Test");
		}
	}

	@RequestMapping(value = "/api/personView", method = RequestMethod.GET)
	public ModelAndView personView(@RequestParam(value = "person") String person) {
		System.out.println("PERSON VIEW");
		if (!"123".equals(person)) {
			return new ModelAndView("/home.html");
		} else {
			throw new MyCustomException("Test");
		}
	}

	@RequestMapping(value = "/api/personStr", method = RequestMethod.GET)
	public String personStr(@RequestParam(value = "person") String person) {
		System.out.println("PERSON STR");
		if (!"123".equals(person)) {
			return "home.html";
		} else {
			throw new MyCustomException("Test");
		}
	}

	@ExceptionHandler(MyCustomException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView errorCallingService() {
		System.out.println("ERROR CAUGHT  ");
		return new ModelAndView("/not_found.html");
	}
}

class MyCustomException extends RuntimeException {
	public MyCustomException(String message) {
		super(message);
	}
}

class Person {
	final String name;

	Person(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}