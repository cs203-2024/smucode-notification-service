package com.cs203.smucode;

import com.cs203.smucode.config.TestSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class NotificationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
