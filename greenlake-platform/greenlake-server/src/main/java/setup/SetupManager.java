package setup;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import settings.SettingsManager;
import util.LoggedClientCompatibleException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Class that manages the setup
 */
public class SetupManager
{
    private static boolean performed = false;
    private static Logger LOGGER = Logger.getLogger(SetupManager.class.getName());

    /**
     * detects if the setup is performed
     *
     * @return whether the setup has been performed
     * @throws IOException thrown by checking if the setup is performed due to file accesses
     * @throws LoggedClientCompatibleException thrown by checking if the setup is performed due to validating
     *         the folders where components are installed
     */
    public static boolean isPerformed() throws IOException, LoggedClientCompatibleException
    {
        if (!performed)
        {
            checkPerformed();
        }
        return performed;
    }

    /**
     * check if the setup is performed given that files meet a certain condition
     *
     * @throws IOException
     * @throws LoggedClientCompatibleException thrown by checking if the setup is performed due to validating
     *         the folders where components are installed
     */
    private static void checkPerformed() throws IOException, LoggedClientCompatibleException
    {
        File configFile = new File(System.getProperty("jboss.server.config.dir")
                                           .concat("/greenlake/greenlake-properties.json"));

        SettingsManager settingsManager = SettingsManager.getInstance();

        if (!configFile.exists())
        {
            performed = false;
            return;
        }

        JSONObject configJSON;

        try
        {
            configJSON = new JSONObject(FileUtils.readFileToString(configFile, "UTF-8"));
        }
        catch (JSONException exception)
        {
            LOGGER.severe("Could not read configFile: \n".concat(exception.getLocalizedMessage()));
            performed = false;
            return;
        }

        if (!(configJSON.has("pathToKafka") && configJSON.has("pathToHadoop")))
        {
            LOGGER.severe("ConfigFile is missing configurations");
            performed = false;
            return;
        }


        if (!KafkaSetupBean.getInstance().validateFolder(configJSON.getString("pathToKafka")))
        {
            LOGGER.severe("No valid Kafka installation found at configured location");
            performed = false;
            return;
        }

        if (!HadoopSetupBean.getInstance().validateFolder(configJSON.getString("pathToHadoop")))
        {
            LOGGER.severe("No valid Hadoop installation found at configured location");
            performed = false;
            return;
        }

        SettingsManager.getInstance().setSetting("pathToKafka", configJSON.getString("pathToKafka"),
                                                 true)
                .setSetting("pathToHadoop", configJSON.getString("pathToHadoop"), true);

        performed = true;
    }
}
