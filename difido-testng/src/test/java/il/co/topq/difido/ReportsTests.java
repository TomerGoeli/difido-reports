package il.co.topq.difido;

import org.testng.annotations.Test;

public class ReportsTests extends AbstractDifidoTestCase{
	
	@Test(description = "Test full report")
	public void testFullReport(){
		report.step("Step 01 - Perfrom login");
		report.startLevel("About to login");
		report.log("Send keys to login text box");
		report.log("Send keys to password text box");
		report.log("Clicking on 'login' button");
		report.endLevel();
	}
	
}
