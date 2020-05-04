package setup;

import settings.SettingsManager;
import util.LoggedClientCompatibleException;

import java.io.IOException;

/**
 * Subclass of SetupBean that handles the Kafka installation
 */
public class KafkaSetupBean extends SetupBean
{
    private static KafkaSetupBean instance;

    /**
     * Private constructor for singleton pattern
     */
    private KafkaSetupBean()
    {
    }

    /**
     * @return KafkaSetupBean instance for singleton pattern
     */
    public static KafkaSetupBean getInstance()
    {
        if (instance == null)
        {
            instance = new KafkaSetupBean();
        }

        return instance;
    }

    /**
     * Installs Kafka and sets the path in the settings
     *
     * @param path, path to install Kafka to
     * @throws IOException inherited from super install
     * @throws LoggedClientCompatibleException inherited from super methods
     */
    @Override
    public void install(String path) throws IOException, LoggedClientCompatibleException
    {
        super.install(path, SettingsManager.getInstance().getSetting("kafkaDownload"));
        SettingsManager.getInstance().setSetting("pathToKafka", super.toAbsolutePath(path), true);
    }

    /**
     * Method needed in super class to get the name of the installation goal for logs and stuff
     *
     * @return Name of the installation goal as String
     */
    @Override
    protected String getNameOfInstallationGoal()
    {
        return "Kafka";
    }

    /**
     * Validates the installation of Kafka in a given folder
     *
     * @param folder folder to validate Kafka in
     * @return if Kafka is present in that folder
     * @throws LoggedClientCompatibleException inherited from super method
     */
    public boolean validateFolder(String folder) throws LoggedClientCompatibleException
    {
        return super.validateFolder(folder, "kafkaHash");
    }
}
