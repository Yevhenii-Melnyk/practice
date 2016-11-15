package spring.reactive;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import spring.reactive.entity.CustomArgument;

@Component
public class CustomHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public Mono<Object> resolveArgument(MethodParameter param,
										BindingContext bindingContext, ServerWebExchange exchange) {
		return Mono.just(new CustomArgument(
				exchange.getRequest().getQueryParams().getFirst("content")));
	}

	@Override
	public boolean supportsParameter(MethodParameter param) {
		return CustomArgument.class.isAssignableFrom(param.getParameterType());
	}
}