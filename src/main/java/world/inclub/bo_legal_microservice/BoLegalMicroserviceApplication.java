package world.inclub.bo_legal_microservice;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;

@EnableR2dbcRepositories
@SpringBootApplication
public class BoLegalMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoLegalMicroserviceApplication.class, args);
	}

	@Bean
	ConnectionFactoryInitializer initializer(ConnectionFactory oCN) {
		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();		
		initializer.setConnectionFactory(oCN);
		/*		
		initializer.setDatabasePopulator(
			new ResourceDatabasePopulator(
				new ClassPathResource("schema.sql")
			)
		);		
		*/

		return initializer;
	}

}
