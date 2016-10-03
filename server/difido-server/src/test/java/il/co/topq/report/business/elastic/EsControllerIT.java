package il.co.topq.report.business.elastic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import il.co.topq.report.Application;
import il.co.topq.report.Common;

public class EsControllerIT {

	private static ESController escontroller;

	private static String executionTimeStamp;

	private int executionId = 1;

	@BeforeClass
	public static void setup() throws IOException {
		FileUtils.deleteDirectory(new File("data"));
		Application.startElastic();
		escontroller = new ESController();
		executionTimeStamp = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());

	}

	@Test
	public void testAddOrUpdateToElastic() throws Exception {
		List<ElasticsearchTest> tests = generateTests(10);
		escontroller.addOrUpdateInElastic(tests);
		Thread.sleep(1000);
		List<ElasticsearchTest> storedTests = ESUtils.getAllByQuery(Common.ELASTIC_INDEX, "test",
				ElasticsearchTest.class, "executionId:" + executionId);
		Assert.assertEquals(tests.size(), storedTests.size());
		storedTests.removeAll(tests);
		Assert.assertTrue(storedTests.size() == 0);

	}

	private List<ElasticsearchTest> generateTests(final int numOfTests) {
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		for (int i = 0; i < numOfTests; i++) {
			tests.add(generateEsTest(i + ""));
		}
		return tests;
	}

	private ElasticsearchTest generateEsTest(String uid) {
		ElasticsearchTest test = new ElasticsearchTest(uid, executionTimeStamp,
				Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date()));
		test.setDescription("foo bar");
		test.setDuration(100);
		test.setExecutionId(executionId);
		test.setMachine("localhost");
		test.setName("Test Foo");
		return test;
	}

	@AfterClass
	public static void teardown() throws IOException {
		Application.stopElastic();
		FileUtils.deleteDirectory(new File("data"));
	}

}
