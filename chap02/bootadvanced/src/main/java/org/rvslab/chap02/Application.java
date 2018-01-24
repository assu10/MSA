package org.rvslab.chap02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@EnableResourceServer       // oauth2 토큰을 검즈하는 보안 필터 활성화해서 접근 토큰 검증
@EnableAuthorizationServer  // 클라이언트 토큰을 저장하는 인메모리 저장소를 가진 서버 생성 (비번, id, secret 제공)
@EnableGlobalMethodSecurity // 메소드 수준에서 보안 적용 가능
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class GreetingController {
    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

    @Autowired
    Environment env;

    @CrossOrigin
    @RequestMapping("/")
    Greet greet() {
        logger.info("★bootadvanced.customproperty "+ env.getProperty("bootadvanced.customproperty"));
        return new Greet("Hello World!!");
    }
}

class Greet {
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
}
