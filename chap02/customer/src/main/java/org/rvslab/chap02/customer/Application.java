package org.rvslab.chap02.customer;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Optional;

/**
 * 종합적인 마이크로서비스
 * JPA, H2 사용
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * 몇 가지 초기데이터 저장
	 *
	 * @param repo the repo
	 * @return the command line runner
	 */
	@Bean
	CommandLineRunner init(CustomerRepository repo) {
		return (evt) -> {
			repo.save(new Customer("Adam1", "adam1@boot.com"));
			repo.save(new Customer("Adam2", "adam2@boot.com"));
			repo.save(new Customer("Adam3", "adam3@boot.com"));
			repo.save(new Customer("Adam4", "adam4@boot.com"));
			repo.save(new Customer("Adam5", "adam5@boot.com"));
			repo.save(new Customer("Adam6", "adam6@boot.com"));
			repo.save(new Customer("Adam7", "adam7@boot.com"));
		};
	}
}


/**
 * 서비스 종단점 역할
 */
@RestController
class CustomerController {

	@Autowired
	CustomerRegistrar customerRegistrar;

	@Autowired
	CustomerController(CustomerRegistrar customerRegistrar) {
		this.customerRegistrar = customerRegistrar;
	}
	/**
	 * 회원가입.
	 * 회원가입에 성공 시 Customer 객체가 응답으로 반환됨.
	 *
	 * @param customer the customer
	 * @return the customer
	 */
	@RequestMapping(path = "/register", method = RequestMethod.POST)
	Customer register(@RequestBody Customer customer) {
		return customerRegistrar.register(customer);
	}
}

/**
 * 비즈니스 로직을 담고 있음.
 * 등록할 회원이 이미 있는지 확인해서 이미 있으면 에러 메세지, 없으면 새로 등록.
 *
 * 회원등록이 성공하면 비동기방식으로 고객알림서비스 호출해서 고객에게 이메일 발송.
 * 고객알림서비스의 호출은 메시징을 통해 구현됨.
 *
 */
@Component
@Lazy
class CustomerRegistrar {
	CustomerRepository customerRepository;
	Sender sender;

	@Autowired
	CustomerRegistrar(CustomerRepository customerRepository, Sender sender) {
		this.customerRepository = customerRepository;
		this.sender = sender;
	}

	Customer register(Customer customer) {
		Optional<Customer> existingCustomer = customerRepository.findByName(customer.getName());

		if (existingCustomer.isPresent()) {
			throw new RuntimeException("is already exits.");
		} else {
			customerRepository.save(customer);
			sender.send(customer.getEmail());
		}
		return customer;
	}
}

/**
 * 발신자.
 * RabbitMQ와 AMQP를 기반으로 만듦.
 */
@Component
@Lazy
class Sender {

	@Autowired
	RabbitMessagingTemplate template;

	@Bean
	Queue queue() {
		return new Queue("CustomerQ", false);
	}

	public void send(String message) {
		template.convertAndSend("CustomerQ", message);
	}
}

/**
 * Customer 엔티티의 영속성 처리
 * 표준 JPA 리파지토리를 상속하기 때문에 엔티티의 속성별 기본적인 조회 메소드들은 스프링 데이터 JPA 리파지토리에 의해 자동으로 구현됨.
 */
@RepositoryRestResource		// RESTful 서비스를 토해 리파지토리로의 접근을 가능하게 해줌.
@Lazy
interface CustomerRepository extends JpaRepository<Customer, Long> {
	// 검색 결과가 존재하면 해당 고객의 Customer 객체 반환
	Optional<Customer> findByName(@Param("name") String name);
}

/**
 * JPA Entity
 */
@Entity
class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	private String email;

	public Customer() {
	}

	public Customer(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", name=" + name + ", email=" + email + "]";
	}
}