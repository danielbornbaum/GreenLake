package setup;

import settings.SettingsManager;
import util.LoggedClientCompatibleException;

import java.io.IOException;

/**
 * Subclass of SetupBean that handles the Hadoop installation
 */
public class HadoopSetupBean extends SetupBean
{
    private static HadoopSetupBean instance;

    /**
     * Private constructor for singleton pattern
     */
    private HadoopSetupBean()
    {
    }

    /**
     * @return HadoopSetupBean instance for singleton pattern
     */
    public static HadoopSetupBean getInstance()
    {
        if (instance == null)
        {
            instance = new HadoopSetupBean();
        }

        return instance;
    }

    /**
     * Installs hadoop and sets the path in the settings
     *
     * @param path, path to install Hadoop to
     * @throws IOException inherited from super install
     * @throws LoggedClientCompatibleException inherited from super methods
     */
    @Override
    public void install(String path) throws IOException, LoggedClientCompatibleException
    {
        super.install(path, SettingsManager.getInstance().getSetting("hadoopDownload"));
        SettingsManager.getInstance().setSetting("pathToHadoop", super.toAbsolutePath(path), true);
    }

    /**
     * Method needed in super class to get the name of the installation goal for logs and stuff
     *
     * @return Name of the installation goal as String
     */
    @Override
    protected String getNameOfInstallationGoal()
    {
        return "Hadoop";
    }

    /**
     * Validates the installation of Hadoop in a given folder
     *
     * @param folder folder to validate Hadoop in
     * @return if Hadoop is present in that folder
     * @throws LoggedClientCompatibleException inherited from super method
     */
    public boolean validateFolder(String folder) throws LoggedClientCompatibleException
    {
        return super.validateFolder(folder, "hadoopHash");
    }
}
