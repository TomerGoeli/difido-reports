package il.co.topq.report.addons;

import il.co.topq.report.business.execution.ExecutionMetadata;

public interface MailEnhancerAddon extends Addon {

	String render(ExecutionMetadata metadata);
}
