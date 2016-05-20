package il.co.topq.report.addons;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * 
 * @author Itai Agmon
 *
 */
@Component
public class AddonManager {

	private List<Class<? extends Addon>> addonClassList;

	public AddonManager() {
		addonClassList = new ArrayList<Class<? extends Addon>>();
		addonClassList.add(DelmeMailEnhancer.class);

		// Class classToLoad =
		// Class.forName(requiredAddonClass.getClass().getName(), true,
		// this.getClass().getClassLoader());

		// Read the list off addons from the configuration.
		// Create list of classes. Provide message in case it can't find addon
	}

	@SuppressWarnings("unchecked")
	public <T extends Addon> List<T> getAddons(Class<T> requiredAddonClass) {
		final List<T> result = new ArrayList<T>();
		try {
			for (Class<? extends Addon> addonClass : addonClassList) {
				// TODO: Handle exceptions
				if (requiredAddonClass.isAssignableFrom(addonClass)) {
					final T addOnInstance = (T)addonClass.newInstance();
					if (null == addOnInstance.getName() || addOnInstance.getName().isEmpty()){
						//TOOD: report error
					}
					// TODO: report instance name
					result.add(addOnInstance);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		AddonManager a = new AddonManager();
		List<MailEnhancerAddon> addons = a.getAddons(MailEnhancerAddon.class);
		for (MailEnhancerAddon addon : addons) {
			System.out.println(addon.render(null));
		}
	}

}
