package com.expensetracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "jwt.secret=VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JKV1RUZXN0aW5nMTIzNDU2Nzg5MA==",
    "app.mail.from=sender@example.com",
    "brevo.api-key=test-api-key",
    "app.frontend-url=http://localhost:5173"
})
class ExpensetrackerapiApplicationTests {

	@Test
	void contextLoads() {
	}

}
