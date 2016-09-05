package il.co.topq.report.plugins;

import il.co.topq.report.business.execution.ExecutionMetadata;

public interface ExecutionPlugin extends Plugin {

	/**
	 * For calling at the end of the execution
	 * 
	 * @param metadata
	 */
	void onExecutionEnded(ExecutionMetadata metadata);

	/**
	 * For manual execution
	 * 
	 * @param params
	 */
	void execute(String params);

}
