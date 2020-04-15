package apputil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

public class AppManager implements Serializable
{
    private HashMap<String, AppDefinition> appDefinitions = new HashMap<>();
    private static AppManager instance = null;

    public static AppManager getInstance()
    {
        if (instance == null)
        {
            instance = new AppManager();
        }
        return instance;
    }

    public boolean registerApp(String appId, String translationKey, String url, String iconPath)
    {
        if (appDefinitions.containsKey(appId))
        {
            return false;
        }

        appDefinitions.put(appId, new AppDefinition(translationKey, iconPath, url));
        return true;
    }

    public boolean unregisterApp(String appId)
    {
        if (!appDefinitions.containsKey(appId))
        {
            return false;
        }

        appDefinitions.remove(appId);
        return true;
    }

    public JSONArray getAppsAsJSON()
    {
        JSONArray appsAsJSON = new JSONArray();

        for (AppDefinition definition : appDefinitions.values())
        {
            JSONObject appDefJSON = new JSONObject();
            appDefJSON.put("translationKey", definition.translationKey).put("url", definition.url)
                    .put("iconPath", definition.iconPath);
            appsAsJSON.put(appDefJSON);
        }

        return appsAsJSON;
    }
}
