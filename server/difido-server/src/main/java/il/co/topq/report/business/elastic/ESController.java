package il.co.topq.report.business.elastic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
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
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		if (!enabled) {
			return;
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
		long currentTime = System.currentTimeMillis();
		final List<TestNode> executionTests = getExecutionTests(machineCreatedEvent.getMachineNode());
		log.trace("Getting execution test toke " + (System.currentTimeMillis() - currentTime));
		currentTime = System.currentTimeMillis();
		List<ElasticsearchTest> esTests = convertToElasticTests(machineCreatedEvent, executionTests);
		log.trace("Converting tests to Elastic toke " + (System.currentTimeMillis() - currentTime));
		currentTime = System.currentTimeMillis();
		storeInElastic(esTests);
		log.trace("Storing tests in Elastic toke " + (System.currentTimeMillis() - currentTime));

		// final List<TestNode> unstoredTests =
		// findTestsThatAreNotStored(storedTests, executionTests);
		// final List<ElasticsearchTest> testsToStore =
		// convertToElasticTests(unstoredTests);

	}

	private void storeInElastic(List<ElasticsearchTest> esTests) {
		if (null == esTests || esTests.isEmpty()) {
			return;
		}
		String[] ids = new String[esTests.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = esTests.get(i).getUid();
		}
		try {
			BulkResponse response = ESUtils.addBulk(Common.ELASTIC_INDEX, TEST_TYPE, ids, esTests);
			if (response.hasFailures()) {
				log.error("Failed updating tests in Elastic");
			}
		} catch (Exception e) {
			log.error("Failed to add tests to Elastic due to " + e.getMessage());
		}
		// for (ElasticsearchTest esTest : esTests) {
		// try {
		// ESUtils.add(Common.ELASTIC_INDEX, TEST_TYPE, esTest.getUid(),
		// esTest);
		// } catch (ElasticsearchException | JsonProcessingException e) {
		// log.error("Failed to update test node with id " + esTest.getUid() + "
		// due to " + e.getMessage());
		// }
		// }

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
		String timestamp = null;
		if (testNode.getTimestamp() != null) {
			timestamp = testNode.getDate() + " " + testNode.getTimestamp();
		} else {
			timestamp = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());
		}
		String executionTimestamp = convertToUtc(machineCreatedEvent.getMetadata().getTimestamp());
		final ElasticsearchTest esTest = new ElasticsearchTest(testNode.getUid(), executionTimestamp,
				convertToUtc(timestamp));
		esTest.setName(testNode.getName());
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
			esTest.setParameters(testNode.getParameters());
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

}
