package il.co.topq.report.business.elastic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.elasticsearch.ElasticsearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

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
		// List<ElasticsearchTest> storedTests = null;
		// try {
		// storedTests = getStoredTests(machineCreatedEvent);
		// } catch (Exception e) {
		// log.error("Failed to retrieve tests from Elastic due to " +
		// e.getMessage());
		// }
		final List<TestNode> executionTests = getExecutionTests(machineCreatedEvent.getMachineNode());
		List<ElasticsearchTest> esTests = convertToElasticTests(machineCreatedEvent, executionTests);
		storeInElastic(esTests);

		// final List<TestNode> unstoredTests =
		// findTestsThatAreNotStored(storedTests, executionTests);
		// final List<ElasticsearchTest> testsToStore =
		// convertToElasticTests(unstoredTests);

	}

	private void storeInElastic(List<ElasticsearchTest> esTests) {
		for (ElasticsearchTest esTest : esTests) {
			try {
				ESUtils.add(Common.ELASTIC_INDEX, TEST_TYPE, esTest.getUid(), esTest);
			} catch (ElasticsearchException | JsonProcessingException e) {
				log.error("Failed to update test node with id " + esTest.getUid() + " due to " + e.getMessage());
			}
		}

	}

	private List<ElasticsearchTest> convertToElasticTests(MachineCreatedEvent machineCreatedEvent,
			List<TestNode> executionTests) {
		final List<ElasticsearchTest> elasticTests = new ArrayList<ElasticsearchTest>();
		for (TestNode testNode : executionTests) {
			elasticTests.add(testNodeToElasticTest(machineCreatedEvent, testNode));
		}
		return elasticTests;
	}

	private ElasticsearchTest testNodeToElasticTest(MachineCreatedEvent machineCreatedEvent, TestNode testNode) {
		// String timestamp = null;
		// if (testNode.getTimestamp() != null) {
		// timestamp = testNode.getTimestamp().replaceFirst(" at ", " ");
		// } else {
		// timestamp =
		// Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());
		// }
		ElasticsearchTest esTest = new ElasticsearchTest(testNode.getUid(),
				machineCreatedEvent.getMetadata().getTimestamp(), convertToUtc(testNode.getTimestamp()));
		esTest.setStatus(testNode.getStatus().name());
		esTest.setDuration(testNode.getDuration());
		esTest.setMachine(machineCreatedEvent.getMachineNode().getName());
		final ScenarioNode rootScenario = machineCreatedEvent.getMachineNode().getChildren()
				.get(machineCreatedEvent.getMachineNode().getChildren().size() - 1);
		if (machineCreatedEvent.getMachineNode().getChildren() != null
				&& !machineCreatedEvent.getMachineNode().getChildren().isEmpty()) {
			esTest.setExecution(rootScenario.getName());
		}
		if (rootScenario.getScenarioProperties() != null) {
			esTest.setScenarioProperties(new HashMap<String, String>(rootScenario.getScenarioProperties()));
		}
		if (testNode.getParent() != null) {
			esTest.setParent(testNode.getParent().getName());
		}
		if (testNode.getDescription() != null) {
			esTest.setDescription(testNode.getDescription());
		}
		if (testNode.getParameters() != null) {
			esTest.setProperties(testNode.getParameters());
		}
		if (testNode.getProperties() != null) {
			esTest.setProperties(testNode.getProperties());
		}
		esTest.setDuration(testNode.getDuration());
		esTest.setStatus(testNode.getStatus().name());
		esTest.setExecutionId(machineCreatedEvent.getExecutionId());
		esTest.setUrl(findTestUrl(machineCreatedEvent.getMetadata(), testNode.getUid()));
		return esTest;
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
		if (null == machineNode.getChildren()) {
			return executionTests;
		}
		for (Node node : machineNode.getChildren(true)) {
			if (node instanceof TestNode) {
				executionTests.add((TestNode) node);
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

	private String findTestUrl(ExecutionMetadata executionMetadata, String uid) {
		if (executionMetadata == null) {
			return "";
		}
		// @formatter:off
		// http://localhost:8080/reports/execution_2015_04_15__21_14_29_767/tests/test_8691429121669-2/test.html
		return "http://" + System.getProperty("server.address") + ":" + System.getProperty("server.port") + "/"
				+ Common.REPORTS_FOLDER_NAME + "/" + executionMetadata.getFolderName() + "/" + "tests" + "/" + "test_"
				+ uid + "/" + "test.html";
		// @formatter:on
	}

}
