package il.co.topq.report.plugins.mail;

import il.co.topq.report.business.execution.ExecutionMetadata;

public interface MailEnhancer {
	
	String getEnhancerName();
	
	String render(ExecutionMetadata metadata);
}
