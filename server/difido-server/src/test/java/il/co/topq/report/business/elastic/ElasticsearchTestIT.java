package il.co.topq.report.business.elastic;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import il.co.topq.report.Common;

public class ElasticsearchTestIT {

	private int executionId;

	private String executionTimeStamp;

	@Before
	public void setUp() {
		executionId = 10;
		executionTimeStamp = Common.ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());
	}

	@Test
	public void testEquals() {
		ElasticsearchTest esTest0 = ElasticsearchTestGenerator.generateEsTest(executionId, executionTimeStamp, "aaa");
		ElasticsearchTest esTest1 = new ElasticsearchTest(esTest0.getUid(), esTest0.getExecutionTimeStamp(),
				esTest0.getTimeStamp());
		esTest1.setDescription(esTest0.getDescription());
		esTest1.setDuration(esTest0.getDuration());
		esTest1.setMachine(esTest0.getMachine());
		esTest1.setName(esTest0.getName());
		esTest1.setExecutionId(esTest0.getExecutionId());
		esTest1.setParent(esTest0.getParent());
		esTest1.setStatus(esTest0.getStatus());
		esTest1.setUrl(esTest0.getUrl());
		esTest1.setParameters(esTest0.getParameters());
		esTest1.setProperties(esTest0.getProperties());
		esTest1.setScenarioProperties(esTest0.getScenarioProperties());
		Assert.assertTrue(esTest0.equals(esTest1));
	}

	@Test
	public void testPositiveHashcode() {
		ElasticsearchTest esTest0 = ElasticsearchTestGenerator.generateEsTest(executionId, executionTimeStamp, "aaa");
		ElasticsearchTest esTest1 = new ElasticsearchTest(esTest0.getUid(), esTest0.getExecutionTimeStamp(),
				esTest0.getTimeStamp());
		esTest1.setDescription(esTest0.getDescription());
		esTest1.setDuration(esTest0.getDuration());
		esTest1.setMachine(esTest0.getMachine());
		esTest1.setName(esTest0.getName());
		esTest1.setExecutionId(esTest0.getExecutionId());
		esTest1.setParent(esTest0.getParent());
		esTest1.setStatus(esTest0.getStatus());
		esTest1.setUrl(esTest0.getUrl());
		esTest1.setParameters(esTest0.getParameters());
		esTest1.setProperties(esTest0.getProperties());
		esTest1.setScenarioProperties(esTest0.getScenarioProperties());
		Assert.assertTrue(esTest0.hashCode() == esTest1.hashCode());
	}
	
	@Test
	public void testNegativeHashcode() {
		ElasticsearchTest esTest0 = ElasticsearchTestGenerator.generateEsTest(executionId, executionTimeStamp, "aaa");
		ElasticsearchTest esTest1 = new ElasticsearchTest(esTest0.getUid(), esTest0.getExecutionTimeStamp(),
				esTest0.getTimeStamp());
		esTest1.setDescription(esTest0.getDescription());
		esTest1.setDuration(esTest0.getDuration());
		esTest1.setMachine(esTest0.getMachine());
		esTest1.setName(esTest0.getName() +"a");
		esTest1.setExecutionId(esTest0.getExecutionId());
		esTest1.setParent(esTest0.getParent());
		esTest1.setStatus(esTest0.getStatus());
		esTest1.setUrl(esTest0.getUrl());
		esTest1.setParameters(esTest0.getParameters());
		esTest1.setProperties(esTest0.getProperties());
		esTest1.setScenarioProperties(esTest0.getScenarioProperties());
		Assert.assertTrue(esTest0.hashCode() != esTest1.hashCode());
	}


}
