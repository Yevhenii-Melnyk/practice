package sample.spring.slash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@SpringBootApplication(scanBasePackages = "sample.spring.slash")
public class SlashSpringApp {
	public static void main(String[] args) {
		SpringApplication.run(SlashSpringApp.class, args);
	}
}

@RestController
class SampleController {
	@RequestMapping("/records/**")
	public String getId(Id id) {
		return id.id;
	}
}

@Configuration
@EnableWebMvc
class WebConfig extends WebMvcConfigurerAdapter {
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new IdResolver());
	}
}

class IdResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Id.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
								  ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest,
								  WebDataBinderFactory binderFactory) throws Exception {
		String basePath = ((String) webRequest.getAttribute(
				HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,
				RequestAttributes.SCOPE_REQUEST
		)).replace("**", "");
		String id = ((String) webRequest.getAttribute(
				HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
				RequestAttributes.SCOPE_REQUEST
		)).replace(basePath, "");
		return new Id(id);
	}
}

class Id {
	public final String id;

	Id(String id) {
		this.id = id;
	}
}