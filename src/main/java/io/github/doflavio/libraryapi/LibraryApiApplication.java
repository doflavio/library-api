package io.github.doflavio.libraryapi;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	public static void main(String[] args){
		SpringApplication.run(LibraryApiApplication.class, args);
	}


	/* Método usado para teste
	@Scheduled(cron = "0 36 12 1/1 * ?") // http://www.cronmaker.com/;jsessionid=node0dez4znzqnvlpuozoq901dj7l767989.node0?0
	public void testeAgendamentoTarefas(){
		System.out.println("AGENDAMENTO DE TAREFAS FUNCIONANDO COM SUCESSO");
	}
	 */

}
