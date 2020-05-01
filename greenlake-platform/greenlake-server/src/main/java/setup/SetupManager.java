package setup;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import settings.SettingsManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class SetupManager
{
    private static boolean performed = false;
    private static Logger LOGGER = Logger.getLogger(SetupManager.class.getName());

    public static boolean isPerformed() throws IOException
    {
        if (!performed)
        {
            checkPerformed();
        }
        return performed;
    }

    private static void checkPerformed() throws IOException
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
            renameOldFile();
            return;
        }

        if (!(configJSON.has("pathToKafka") && configJSON.has("pathToHadoop")))
        {
            LOGGER.severe("ConfigFile is missing configurations");
            performed = false;
            renameOldFile();
            return;
        }


        if (!KafkaSetupBean.getInstance().validateFolder(configJSON.getString("pathToKafka")))
        {
            LOGGER.severe("No valid Kafka installation found at configured location");
            performed = false;
            renameOldFile();
            return;
        }

        if (!HadoopSetupBean.getInstance().validateFolder(configJSON.getString("pathToHadoop")))
        {
            LOGGER.severe("No valid Hadoop installation found at configured location");
            performed = false;
            renameOldFile();
            return;
        }

        SettingsManager.getInstance().setSetting("pathToKafka", configJSON.getString("pathToKafka"),
                                                 true)
                .setSetting("pathToHadoop", configJSON.getString("pathToHadoop"), true);

        performed = true;
    }

    private static void renameOldFile()
    {
        LOGGER.warning("\tFile will be renamed and setup has to be performed again");
    }
}
