package il.co.topq.report.plugins;

public interface Plugin {

	/**
	 * The name of the plugin. Mostly used for manually execution
	 * 
	 * @return The name of the plugin
	 */
	public String getName();

	/**
	 * For manual triggering
	 * 
	 * @param params
	 */
	void execute(String params);

}
