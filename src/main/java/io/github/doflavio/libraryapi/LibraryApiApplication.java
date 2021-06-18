package io.github.doflavio.libraryapi;

import io.github.doflavio.libraryapi.service.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {

	@Autowired
	private EmailService emailService;

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public CommandLineRunner runner(){
		return args -> {
			List<String> emails = Arrays.asList("libray-api-47bada@inbox.mailtrap.io");
			emailService.sendMails("Testando serviço de emails", emails);
			System.out.println("EMAILS ENVIADOS");
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}


	/* Método usado para teste
	@Scheduled(cron = "0 36 12 1/1 * ?") // http://www.cronmaker.com/;jsessionid=node0dez4znzqnvlpuozoq901dj7l767989.node0?0
	public void testeAgendamentoTarefas(){
		System.out.println("AGENDAMENTO DE TAREFAS FUNCIONANDO COM SUCESSO");
	}
	 */

}
