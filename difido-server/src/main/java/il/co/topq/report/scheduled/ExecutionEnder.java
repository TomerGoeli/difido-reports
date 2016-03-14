package il.co.topq.report.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.listener.ListenersManager;
import il.co.topq.report.listener.execution.ExecutionManager;
import il.co.topq.report.listener.execution.ExecutionManager.ExecutionMetaData;

@Component
public class ExecutionEnder {

	private final Logger log = LoggerFactory.getLogger(ExecutionEnder.class);

	private static int maxExecutionIdleTimeout = 0;

	private static boolean enabled;

	static {
		maxExecutionIdleTimeout = Configuration.INSTANCE.readInt(ConfigProps.MAX_EXECUTION_IDLE_TIME_IN_SEC);
		if (maxExecutionIdleTimeout > 0) {
			enabled = true;
		}
	}

	@Scheduled(fixedRate = 30000)
	public void setExecutionsToNotActive() {
		if (!enabled) {
			log.debug("The executionEnder is disabled");
			return;
		}
		log.trace("Waking up in order to search for executions that need to end");
		final ExecutionMetaData[] metaDataArr = ExecutionManager.INSTANCE.getAllMetaData();
		for (ExecutionMetaData meta : metaDataArr) {
			if (!meta.isActive()) {
				continue;
			}
			final int idleTime = (int) (System.currentTimeMillis() - meta.getLastAccessedTime()) / 1000;
			if (null == meta.getExecution()) {
				log.warn("Active meta data of execution with id " + meta.getId() + " has no execution included");
			}
			if (idleTime > maxExecutionIdleTimeout) {
				log.debug("Execution with id " + meta.getId() + " idle time is " + idleTime
						+ " which exceeded the max idle time of " + maxExecutionIdleTimeout + ". Disabling execution");
				ListenersManager.INSTANCE.notifyExecutionEnded(meta.getId(), meta.getExecution());
			}
		}

	}

}
