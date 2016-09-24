package il.co.topq.report.business.elastic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.Common;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionCreatedEvent;
import il.co.topq.report.events.ExecutionDeletedEvent;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.MachineCreatedEvent;
import il.co.topq.report.events.TestDetailsCreatedEvent;

/**
 * 
 * @author Itai.Agmon
 *
 */
@Component
public class ESController {

	private static final String TEST_TYPE = "test";

	private final Logger log = LoggerFactory.getLogger(ESController.class);

	// TODO: For testing. Of course that this should be handled differently.
	// Probably using the application context.
	public static boolean enabled = true;

	@EventListener
	public void onExecutionCreatedEvent(ExecutionCreatedEvent executionCreatedEvent) {
	}

	@EventListener
	public void onExecutionDeletedEvent(ExecutionDeletedEvent executionDeletedEvent) {
	}

	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		if (!enabled) {
			return;
		}
	}

	private String convertToUtc(final String dateInLocalTime) {
		try {
			final Date originalDate = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.parse(dateInLocalTime);
			final SimpleDateFormat sdf = (SimpleDateFormat) Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.clone();
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf.format(originalDate);
		} catch (ParseException e) {
			log.warn("Failed to convert date " + dateInLocalTime + " to UTC time zone");
			return dateInLocalTime;
		}
	}

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		if (!enabled) {
			return;
		}
		// Get all the current tests that are linked to the execution.
		List<ElasticsearchTest> storedTests = null;
		try {
			storedTests = getStoredTests(machineCreatedEvent);
		} catch (Exception e) {
			log.error("Failed to retrieve tests from Elastic due to " + e.getMessage());
		}
		final List<TestNode> executionTests = getExecutionTests(machineCreatedEvent.getMachineNode());
		final List<TestNode> unstoredTests = findTestsThatAreNotStored(storedTests, executionTests);
		final List<ElasticsearchTest> testsToStore = convertToElasticTests(unstoredTests);

	}

	private List<ElasticsearchTest> convertToElasticTests(List<TestNode> unstoredTests) {
		final List<ElasticsearchTest> testsToStore = new ArrayList<ElasticsearchTest>();
		for (TestNode testNode : unstoredTests) {
			testsToStore.add(testNodeToElasticTest(testNode));
		}
		return testsToStore;
	}

	private ElasticsearchTest testNodeToElasticTest(TestNode testNode) {
//		ElasticsearchTest esTest = new ElasticsearchTest(testNode.getUid(), testNode.gete, timeStamp)
		return null;
	}

	private List<TestNode> findTestsThatAreNotStored(List<ElasticsearchTest> storedTests,
			List<TestNode> executionTests) {
		List<TestNode> unstoredTests = new ArrayList<TestNode>();
		for (TestNode testNode : executionTests) {
			boolean found = false;
			for (ElasticsearchTest executionTest : storedTests) {
				if (executionTest.getUid() == testNode.getUid()) {
					found = true;
					continue;
				}
			}
			if (!found) {
				unstoredTests.add(testNode);
			}

		}
		return unstoredTests;
	}

	private List<TestNode> getExecutionTests(MachineNode machineNode) {
		List<TestNode> executionTests = new ArrayList<TestNode>();
		for (ScenarioNode rootScenario : machineNode.getAllScenarios()) {
			for (Node child : rootScenario.getChildren(true)) {
				if (child instanceof TestNode) {
					executionTests.add((TestNode) child);
				}
			}

		}
		return executionTests;
	}

	private List<ElasticsearchTest> getStoredTests(MachineCreatedEvent machineCreatedEvent) throws Exception {
		// TODO: We should accept tests that are in status 'in progress;
		return ESUtils.getAllByQuery(Common.ELASTIC_INDEX, TEST_TYPE, ElasticsearchTest.class,
				"executionId:" + machineCreatedEvent.getExecutionId());
	}

	@EventListener
	public void OnTestDetailsCreatedEvent(TestDetailsCreatedEvent testDetailsCreatedEvent) {

	}

	private String findTestUrl(ExecutionMetadata executionMetadata, TestDetails details) {
		if (executionMetadata == null) {
			return "";
		}
		// @formatter:off
		// http://localhost:8080/reports/execution_2015_04_15__21_14_29_767/tests/test_8691429121669-2/test.html
		return "http://" + System.getProperty("server.address") + ":" + System.getProperty("server.port") + "/"
				+ Common.REPORTS_FOLDER_NAME + "/" + executionMetadata.getFolderName() + "/" + "tests" + "/" + "test_"
				+ details.getUid() + "/" + "test.html";
		// @formatter:on
	}

}
