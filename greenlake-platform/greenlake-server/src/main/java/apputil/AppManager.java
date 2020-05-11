package apputil;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the apps registered to the platform
 */
public class AppManager
{
    private HashMap<String, AppDefinition> appDefinitions = new HashMap<>();
    private static AppManager instance = null;
    private static final Logger LOGGER = Logger.getLogger(AppManager.class.getName());

    /**
     * Private constructor for singleton pattern, creates an app called home, that resembles the homepage
     */
    private AppManager()
    {
        loadFromConfig();
        registerApp("home", 0, "Home", "/homepage.html", "/images/icons/home.png");
    }

    /**
     * @return Instance of AppManager (singleton instance)
     */
    public static AppManager getInstance()
    {
        if (instance == null)
        {
            instance = new AppManager();
        }
        return instance;
    }

    /**
     * register an app to the platform
     *
     * @param appId id of the app
     * @param orderNumber position where to display the app inside the plattform
     * @param name display name of the app
     * @param url url to the app
     * @param iconPath path to the app icon
     * @return whether the app could be registered or whether an app with this name already exists (=false)
     */
    public boolean registerApp(String appId, int orderNumber, String name, String url, String iconPath)
    {
        if (appDefinitions.containsKey(appId))
        {
            return false;
        }

        appDefinitions.put(appId, new AppDefinition(name, orderNumber, iconPath, url));
        return true;
    }

    /**
     * unregister an app to the platform
     *
     * @param appId id of the app to unregister
     * @return boolean whether the app could be removed, based on whether it existed
     */
    public boolean unregisterApp(String appId)
    {
        if (!appDefinitions.containsKey(appId))
        {
            return false;
        }

        appDefinitions.remove(appId);
        return true;
    }

    /**
     * @return JSONArray of AppDefinitions
     */
    public JSONArray getAppsAsJSON()
    {
        List<JSONObject> appList = new ArrayList<>();

        for (String appId : appDefinitions.keySet())
        {
            AppDefinition definition = appDefinitions.get(appId);
            JSONObject appDefJSON = new JSONObject();
            appDefJSON.put("appId", appId)
                    .put("orderNumber", definition.getOrderNumber())
                    .put("name", definition.getName())
                    .put("url", definition.getUrl())
                    .put("iconPath", definition.getIconPath());

            appList.add(appDefJSON);
        }

        appList.sort(Comparator.comparingInt(def -> def.getInt("orderNumber")));
        JSONArray appsAsJSON = new JSONArray();
        appList.forEach(appsAsJSON::put);

        return appsAsJSON;
    }

    private void loadFromConfig()
    {
        File configFile = new File(System.getProperty("jboss.server.config.dir")
                                           .concat("/greenlake/apps/external-apps.json"));

        if (configFile.exists())
        {
            try
            {
                String configContent = FileUtils.readFileToString(configFile, "UTF-8");
                JSONArray jsonConfig = new JSONArray(configContent);

                jsonConfig.iterator().forEachRemaining(element -> {

                    try
                    {
                        JSONObject elementAsJSON = new JSONObject(element.toString());
                        registerApp(elementAsJSON.getString("appId"), elementAsJSON.getInt("orderNumber"),
                                    elementAsJSON.getString("name"), elementAsJSON.getString("url"),
                                    elementAsJSON.getString("iconPath"));
                    }
                    catch (JSONException e)
                    {
                        LOGGER.warning("One or more apps could not be loaded because of invalid configuration.");
                    }
                });
            }
            catch (IOException | JSONException e)
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));

                LOGGER.warning(
                        String.format(
                                "Could not open config file %s for external apps. Using defaults. The error was:\n%s",
                                configFile.getAbsolutePath(), sw.toString()));
            }
        }
    }
}
