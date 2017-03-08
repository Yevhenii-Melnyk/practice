package spring.reactive;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

import java.io.File;

@Controller
public class FileController {

	@GetMapping("/file")
	@ResponseBody
	public Mono<Void> getFile(ServerHttpRequest request, ServerHttpResponse response) {
		try {
			ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
			Resource logo = new ClassPathResource("fish.mp4");
			File file = logo.getFile();
			zeroCopyResponse.getHeaders().setContentType(new MediaType("video", "mp4"));
			zeroCopyResponse.getHeaders().setContentLength(file.length());
			return zeroCopyResponse.writeWith(file, 0, file.length());
		} catch (Throwable ex) {
			return Mono.error(ex);
		}
	}

}
