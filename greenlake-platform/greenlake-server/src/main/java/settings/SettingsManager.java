package settings;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class to manage the settings for this application
 */
public class SettingsManager
{
    private static final Logger LOGGER = Logger.getLogger(SettingsManager.class.getName());
    private static SettingsManager instance;
    private HashMap<String, String> settings = new HashMap<>();
    private List<String> persistables = new ArrayList<>();

    /**
     * Private constructor for singleton pattern
     */
    private SettingsManager()
    {
    }

    /**
     * @return SettingsManager instance for singleton pattern
     */
    public static SettingsManager getInstance()
    {
        if (instance == null)
        {
            instance = new SettingsManager();
        }

        return instance;
    }

    /**
     * set a setting by key
     *
     * @param key name of the setting
     * @param value value of the setting
     * @param persistable if the setting is persisted to greenlake-properties.json
     * @return this object to allow builder pattern where possible
     */
    public SettingsManager setSetting(String key, String value, boolean persistable)
    {
        settings.put(key, value);

        if (persistable)
        {
            persistables.add(key);
        }

        return this;
    }

    /**
     * returns a setting as string
     *
     * @param key name of the setting
     * @return value of the setting
     */
    public String getSetting(String key)
    {
        return settings.get(key);
    }

    /**
     * removes a setting from the settings
     *
     * @param key name of the setting
     * @return this object to allow builder pattern where possible
     */
    public SettingsManager removeSetting(String key)
    {
        persistables.remove(key);
        settings.remove(key);
        return this;
    }

    /**
     * @return whether a setting is present
     */
    public boolean hasSetting(String key)
    {
        return settings.containsKey(key);
    }

    /**
     * method to persist the settings to greenlake-properties.json, marks the end of the builder pattern and therefore
     * returns void
     *
     * @throws IOException when greenlake-properties.json can't be accessed
     */
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
