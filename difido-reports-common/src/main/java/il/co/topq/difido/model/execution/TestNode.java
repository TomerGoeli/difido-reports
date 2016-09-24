package il.co.topq.difido.model.execution;

import il.co.topq.difido.model.Enums.Status;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "index", "uid","description", "duration", "timestamp", "className","parameters","properties" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class TestNode extends Node {

	@JsonProperty("index")
	private int index;

	@JsonProperty("uid")
	private String uid;

	@JsonProperty("description")
	private String description = "";

	@JsonProperty("duration")
	private long duration;

	@JsonProperty("timestamp")
	private String timestamp;

	@JsonProperty("className")
	private String className;
	
	@JsonProperty("parameters")
	private Map<String, String> parameters;

	@JsonProperty("properties")
	private Map<String, String> properties;


	public TestNode() {
		setStatus(Status.in_progress);
	}

	public TestNode(String name, String uid) {
		this(0, name, uid);

	}

	public TestNode(int index, String name, String uid) {
		super(name);
		if (index < 0) {
			throw new IllegalArgumentException("index can't be smaller then 0");
		}
		this.index = index;
		setStatus(Status.in_progress);
		this.uid = uid;
	}

	/**
	 * Copy constructor
	 * 
	 * @param aTestNode
	 * @return
	 */
	@JsonIgnore
	public static TestNode newInstance(TestNode aTestNode) {
		TestNode testNodeCopy = new TestNode(aTestNode.getIndex(), aTestNode.getName(), aTestNode.getUid());
		testNodeCopy.setDuration(aTestNode.getDuration());
		testNodeCopy.setParent(aTestNode.getParent());
		testNodeCopy.setStatus(aTestNode.getStatus());
		testNodeCopy.setTimestamp(aTestNode.getTimestamp());
		return testNodeCopy;
	}
	
	@JsonIgnore
	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new HashMap<String, String>();
		}
		properties.put(key, value);
	}

	@JsonIgnore
	public void addParameter(String key, String value) {
		if (parameters == null) {
			parameters = new HashMap<String, String>();
		}
		parameters.put(key, value);
	}


	@JsonIgnore
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + index;
		result = 31 * result + new Long(duration).intValue();
		if (timestamp != null) {
			result = 31 * result + timestamp.hashCode();
		}
		return result;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	

}
