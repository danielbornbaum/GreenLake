package settings;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class SettingsManager
{
    private static final Logger LOGGER = Logger.getLogger(SettingsManager.class.getName());
    private static SettingsManager instance;
    private HashMap<String, String> settings = new HashMap<>();
    private List<String> persistables = new ArrayList<>();

    private SettingsManager()
    {
    }

    public static SettingsManager getInstance()
    {
        if (instance == null)
        {
            instance = new SettingsManager();
        }

        return instance;
    }

    public SettingsManager setSetting(String key, String value, boolean persistable)
    {
        settings.put(key, value);

        if (persistable)
        {
            persistables.add(key);
        }

        return this;
    }

    public String getSetting(String key)
    {
        return settings.get(key);
    }

    public SettingsManager removeSetting(String key)
    {
        persistables.remove(key);
        settings.remove(key);
        return this;
    }

    public boolean hasSetting(String key)
    {
        return settings.containsKey(key);
    }

    public void persistSettings() throws IOException
    {
        File configFile = new File(System.getProperty("jboss.server.config.dir")
                                           .concat("/greenlake/greenlake-properties.json"));

        JSONObject configJSON = new JSONObject();

        for (String key : persistables)
        {
            configJSON.put(key, settings.get(key));
        }

        FileUtils.writeStringToFile(configFile, configJSON.toString(), "UTF-8");
    }
}
