package il.co.topq.report.addons;

import il.co.topq.report.business.execution.ExecutionMetadata;

public class DelmeMailEnhancer implements MailEnhancerAddon {

	@Override
	public String getName() {
		return "My mail enhancer";
	}

	@Override
	public String render(ExecutionMetadata metadata) {
		return "success";
	}

}
