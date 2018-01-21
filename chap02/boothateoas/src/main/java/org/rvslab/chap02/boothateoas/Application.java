package org.rvslab.chap02.boothateoas;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Initializer를 활용한 스프링 부트 마이크로서비스 개발 : HATEOS 예제
 *
 * @since 2019.01.21
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

@RestController
class GreetingController {
	@RequestMapping("/greet")
	Greet greet() {
		return new Greet("Hello World!!");
	}

	/**
	 * GreetingController에 링크 추카
	 * 여기서 생성된 링크의 기본값은 /greeting?name=HATEOAS
	 *
	 * @param name the name
	 * @return the http entity
	 */
	@RequestMapping("/greeting")
	@ResponseBody
	public HttpEntity<Greet> greeting(@RequestParam(value="name", required = false, defaultValue = "HATEOAS") String name) {
		Greet greet = new Greet("Hello " + name);

		// 응답 JSON에 링크 추가 (여기선 동일한 API에 링크 추가)
		greet.add(linkTo(methodOn(GreetingController.class).greeting(name)).withSelfRel());
		return new ResponseEntity<Greet>(greet, HttpStatus.OK);
	}
}

/**
 * HATEOAS를 사용하기 위해선 DTO 객체에 ResourceSupport 클래스를 상속해줘야 함
 */
class Greet extends ResourceSupport {
	private String message;

	public Greet() {

	}

	public Greet(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String toString() { return message; }
}
