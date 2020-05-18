package lifecycle;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import settings.SettingsManager;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Bean that manages the lifecycle with deployment of this application
 */
@Singleton
@Startup
@SuppressWarnings("unused")
public class LifeCycleBean
{
    private static final Logger LOGGER = Logger.getLogger(LifeCycleBean.class.getName());

    /**
     * Code executed after deployment
     */
    @PostConstruct
    @SuppressWarnings("unused")
    public void postConstruct()
    {
        File kafkaHashFile = new File(
                System.getProperty("jboss.server.config.dir").concat("/greenlake/validation/kafka.hash"));
        File hadoopHashFile = new File(
                System.getProperty("jboss.server.config.dir").concat("/greenlake/validation/hadoop.hash"));
        File resourceDownloadFile = new File(
                System.getProperty("jboss.server.config.dir").concat("/greenlake/resource-downloads.json"));

        JSONObject resourceDownloads = new JSONObject()
                .put("kafka", "http://mirror.23media.de/apache/kafka/2.4.1/kafka_2.11-2.4.1.tgz")
                .put("hadoop",
                     "http://mirror.softaculous.com/apache/hadoop/common/hadoop-2.10.0/hadoop-2.10.0.tar.gz");

        fileOrDefault("kafkaHash", kafkaHashFile, "97543-13547921263321486-579938047",
                      false);
        fileOrDefault("hadoopHash", hadoopHashFile,
                      "975433076010100756194257424810714116584309433274073523508109400031",
                      false);
        fileOrDefault("resourceDownloads", resourceDownloadFile, resourceDownloads.toString(),
                      false);

        try
        {
            if (resourceDownloads.has("kafka") && resourceDownloads.has("hadoop"))
            {
                resourceDownloads = new JSONObject(SettingsManager.getInstance().getSetting("resourceDownloads"));
            }
            else
            {
                LOGGER.warning(
                        "The given resourceDownloads configuration file did not contain all needed data, default values are used");
            }
        }
        catch (JSONException e)
        {
            LOGGER.warning(
                    "The given resourceDownloads configuration file did not contain valid json, default values are used");
        }
        SettingsManager.getInstance().removeSetting("resourceDownloads");
        SettingsManager.getInstance().setSetting("kafkaDownload", resourceDownloads.getString("kafka"), false);
        SettingsManager.getInstance().setSetting("hadoopDownload", resourceDownloads.getString("hadoop"), false);
    }

    /**
     * helper method that tries to load a value from a file and sets a default if not successful
     *
     * @param setting name of the setting to load
     * @param file file to load the setting from
     * @param defaultValue default value to set when file is not loaded
     * @param persistable whether the setting can be written to greenlake-properties.json
     */
    private void fileOrDefault(String setting, File file, String defaultValue, boolean persistable)
    {
        if (file.exists())
        {
            try
            {
                SettingsManager.getInstance()
                        .setSetting(setting, FileUtils.readFileToString(file, "UTF-8"), persistable);
                LOGGER.info(
                        String.format("Configuration file found. Setting '%s' overwritten with data from %s", setting,
                                      file.getAbsolutePath()));
                return;
            }
            catch (IOException e)
            {
                LOGGER.warning("Could not open kafkaHash even though it is supposed to exist.");
            }
        }

        SettingsManager.getInstance().setSetting(setting, defaultValue, persistable);
    }
}
