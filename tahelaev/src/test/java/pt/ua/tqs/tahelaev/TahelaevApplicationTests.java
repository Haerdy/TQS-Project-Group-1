package pt.ua.tqs.tahelaev;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;

@SpringBootTest
class TahelaevApplicationTests {

	@Test
	void contextLoads() {
		
	}

	@Test
	void mainRunsTheSpringApplication() {
		String[] args = new String[] { "--spring.main.web-application-type=none" };

		try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
			TahelaevApplication.main(args);

			springApplication.verify(
					() -> SpringApplication.run(TahelaevApplication.class, args));
		}
	}

}
