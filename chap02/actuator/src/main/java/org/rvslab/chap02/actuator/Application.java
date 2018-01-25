package org.rvslab.chap02.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.concurrent.atomic.LongAdder;

/**
 * 서비스의 운영에 필요한 스프링 부트의 기능
 *
 * 스트링 부트 Actuator는 스프링 부트 애플리케이션을 운영하고 관리하는데 필요한 기능들을 사용하기 쉬운 형태로 제공함.
 */
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

/**
 * 1분에 2개 이상의 트랜잭션 요청을 받으면 서버 상태가 out of service로 설정되도록 함.
 *
 * 1분동안의 트랜잭션 수를 카운트하는 간단한 POJO 클래스.
 */
class TPSCounter {
	LongAdder count;
	int threshold = 2;
	Calendar expiry = null;

	TPSCounter() {
		count = new LongAdder();
		expiry = Calendar.getInstance();
		expiry.add(Calendar.MINUTE, 1);
	}

	/**
	 * 1분이 경과했는지 검사
	 * @return the boolean
	 */
	boolean isExpired() {
		return Calendar.getInstance().after(expiry);
	}

	/**
	 * 1분동안의 트랜잭션 수가 허용치(threshold) 이내에 있는지 검사
	 * @return the boolean
	 */
	boolean isWeak() {
		return (count.intValue() > threshold);
	}

	/**
	 * 트랜잭션 요청에 따라 단순히 카운트 증가
	 */
	void increment() {
		count.increment();
	}
}

/**
 * HealthIndicator를 구현해서 실제 서버 검진 기능 구성
 */
@Component
class TPSHealth implements HealthIndicator {
	TPSCounter counter;

	// 카운터가 weak인지 검사
	// 카운터가 weak 하다는 것은 서비스가 처리할 수 있는 것보다 더 많은 트랜잭션을 처리하고 있음을 의미함.
	// weak하다면 인스턴스를 Out of Service로 표시함.
	@Override
	public Health health() {
		boolean health = howGoodIsHealth();		// perform some specific health check
		if (health) {
			return Health.outOfService().withDetail("Too many requests", "OutofService").build();
		}
		return Health.up().build();
	}

	void updateTx() {
		if (counter == null || counter.isExpired()) {
			counter = new TPSCounter();
		}
		counter.increment();
	}

	boolean howGoodIsHealth() {
		return counter.isWeak();
	}
}


@RestController
class GreetingController {
	private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

	// TPSHealth 클래스를 GreetingController에 주입하고 greet 메소드가 호출될 때 health.updateTx() 호출하게 함.
	TPSHealth health;
	CounterService counterService;
	GaugeService gaugeService;

	@Autowired
	GreetingController(TPSHealth health, CounterService counterService, GaugeService gaugeService) {
		this.health = health;
		this.counterService = counterService;
		this.gaugeService = gaugeService;
	}

	@CrossOrigin
	@RequestMapping("/")
	Greet greet() {
		logger.info("Serving Request...!!!");
		health.updateTx();
		this.counterService.increment("greet.txnCount");
		gaugeService.submit("greet.customgauge", 1.0);
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

