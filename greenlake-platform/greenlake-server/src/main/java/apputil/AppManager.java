package apputil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AppManager
{
    private HashMap<String, AppDefinition> appDefinitions = new HashMap<>();
    private static AppManager instance = null;

    private AppManager()
    {
        registerApp("home", 0, "Home", "/homepage.html", "/images/icons/home.png");
    }

    public static AppManager getInstance()
    {
        if (instance == null)
        {
            instance = new AppManager();
        }
        return instance;
    }

    public boolean registerApp(String appId, int orderNumber, String name, String url, String iconPath)
    {
        if (appDefinitions.containsKey(appId))
        {
            return false;
        }

        appDefinitions.put(appId, new AppDefinition(name, orderNumber, iconPath, url));
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
}
