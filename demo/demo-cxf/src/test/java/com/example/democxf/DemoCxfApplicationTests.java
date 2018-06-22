package com.example.democxf;

import com.example.democxf.web.Hello;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoCxfApplicationTests {

	@Autowired
	TestRestTemplate testRestTemplate;

	@Before
	public void setUp() throws Exception {
		testRestTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
	}

	@Test
	public void hello() {
	    Hello request = new Hello();
	    request.setMsg("test");
		Hello[] response =  testRestTemplate.postForObject("/api/hello/name?count=5", request, Hello[].class);
		assertEquals(5, response.length);
		assertEquals("test name", response[0].getMsg());
	}

}
