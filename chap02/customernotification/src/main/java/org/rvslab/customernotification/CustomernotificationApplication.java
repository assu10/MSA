package org.rvslab.customernotification;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class CustomernotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomernotificationApplication.class, args);
	}
}

/**
 * 회원등록 시 발송되는 메시지를 기다림.
 * 고객 프로파일 서비스에서 발송되는 메시지를 받아서 고객에게 이메일 발송
 */
@Component
class Receiver {
	@Autowired
	Mailer mailer;

	@Bean
	Queue queue() {
		return new Queue("CustomerQ", false);
	}

	@RabbitListener(queues = "CustomerQ")
	public void processMessage(String email) {
		System.out.println(email);
		mailer.sendMail(email);
	}
}

/**
 * 이메일을 실제로 발송
 * JavaMailSender 이용
 */
@Component
class Mailer {
	@Autowired
	private JavaMailSender javaMailSender;

	public void sendMail(String email) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(email);
		mailMessage.setSubject("Registration");
		mailMessage.setText("Successfully Registered.");

		javaMailSender.send(mailMessage);
	}
}