package il.co.topq.report.front.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.report.business.plugins.PluginController;

@RestController
@Path("api/plugins")
public class PluginResource {

	private static final Logger log = LoggerFactory.getLogger(PluginResource.class);

	private PluginController pluginController;

	@Autowired
	public PluginResource(PluginController pluginController) {
		this.pluginController = pluginController;
	}

	/**
	 * Get list of all the reports
	 * 
	 * @param pluginName
	 *            The name of the plugin to execute
	 * @return
	 */
	@GET
	public void get(@PathParam("plugin") String pluginName, String params) {
		log.debug("GET - Execute plugin " + pluginName + "(" + params + ")");
		pluginController.executePlugin(pluginName, params);
	}

}
