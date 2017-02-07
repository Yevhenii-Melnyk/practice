package sample.spring.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication(scanBasePackages = "sample.spring.async")
public class AsyncBootApplication extends WebMvcConfigurerAdapter {

	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(1);
		taskExecutor.initialize();
		configurer.setTaskExecutor(taskExecutor);
	}

	public static void main(String[] args) {
		SpringApplication.run(AsyncBootApplication.class, args);
	}
}

@RestController
class AsyncController {

	final static Logger LOGGER = LoggerFactory.getLogger(AsyncController.class);

	private AsyncService asyncService;

	public AsyncController(AsyncService asyncService) {
		this.asyncService = asyncService;
	}

	@RequestMapping("/callable")
	public Callable<String> callable() {
		return () -> {
			Thread.sleep(5000);
			return asyncService.result();
		};
	}

	@RequestMapping("/deferredAsync")
	public DeferredResult<String> deferredAsync() {
		DeferredResult<String> result = new DeferredResult<>();
		result.setResultHandler(inner -> {
			LOGGER.info("~~~~~~~~COMPLETED DEFERRED WITH VALUE : " + inner);
		});
		CompletableFuture<String> future = CompletableFuture.supplyAsync(asyncService::result);
		future.whenCompleteAsync((res, e) -> {
			LOGGER.info("~~~~~~~~COMPLETED DEFERRED~~~~~~~~");
			result.setResult(res);
		});
		return result;
	}

	@RequestMapping("/deferred")
	public DeferredResult<String> deferred() {
		DeferredResult<String> result = new DeferredResult<>();
		result.setResultHandler(inner -> {
			LOGGER.info("~~~~~~~~COMPLETED DEFERRED WITH VALUE : " + inner);
		});
		CompletableFuture<String> future = CompletableFuture.supplyAsync(asyncService::result);
		future.whenComplete((res, e) -> {
			LOGGER.info("~~~~~~~~COMPLETED DEFERRED~~~~~~~~");
			result.setResult(res);
		});
		return result;
	}

	@RequestMapping("/sse")
	public SseEmitter sse() {
		SseEmitter sseEmitter = new SseEmitter(10000000L);
		CompletableFuture.supplyAsync(() -> {
			while (true) {
				try {
//					sseEmitter.send(SseEmitter.event().reconnectTime(500).data(asyncService.result()));
					sseEmitter.send(asyncService.result());
					Thread.sleep(1000);
				} catch (IOException | InterruptedException e) {
					LOGGER.error(e.getMessage());
				}
			}
		});
		return sseEmitter;
	}



}

@Component
class AsyncService {

	final static Logger LOGGER = LoggerFactory.getLogger(AsyncService.class);

	public String result() {
		LOGGER.info("~~~~~~~~GETTING RESULT~~~~~~~~");
		return "hello";
	}

}
