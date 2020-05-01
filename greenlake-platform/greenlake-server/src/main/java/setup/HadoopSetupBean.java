package setup;

import settings.SettingsManager;

import java.io.IOException;

public class HadoopSetupBean extends SetupBean<HadoopSetupBean>
{
    private static HadoopSetupBean instance;

    private HadoopSetupBean()
    {
    }

    public static HadoopSetupBean getInstance()
    {
        if (instance == null)
        {
            instance = new HadoopSetupBean();
        }

        return instance;
    }

    @Override
    public void install(String path) throws IOException
    {
        super.install(path, SettingsManager.getInstance().getSetting("hadoopDownload"));
        path = path.replace("\\", "/");
        SettingsManager.getInstance().setSetting("pathToHadoop", path, true);
    }

    @Override
    protected String getNameOfInstallationGoal()
    {
        return "Hadoop";
    }

    public boolean validateFolder(String folder) throws IOException
    {
        return super.validateFolder(folder, "hadoopHash");
    }
}
