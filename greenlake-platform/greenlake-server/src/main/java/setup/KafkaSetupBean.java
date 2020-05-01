package setup;

import settings.SettingsManager;

import javax.ejb.Stateless;
import java.io.IOException;
import java.util.HashMap;

@Stateless
public class KafkaSetupBean extends SetupBean<KafkaSetupBean>
{
    private static KafkaSetupBean instance;

    private static HashMap<String, Class<?>> requiredProperties = new HashMap<>();

    private KafkaSetupBean()
    {
    }

    public static KafkaSetupBean getInstance()
    {
        if (instance == null)
        {
            instance = new KafkaSetupBean();
        }

        return instance;
    }

    @Override
    public void install(String path) throws IOException
    {
        super.install(path, SettingsManager.getInstance().getSetting("kafkaDownload"));
        path = path.replace("\\", "/");
        SettingsManager.getInstance().setSetting("pathToKafka", path, true);
    }

    @Override
    protected String getNameOfInstallationGoal()
    {
        return "Kafka";
    }

    public boolean validateFolder(String folder) throws IOException
    {
        return super.validateFolder(folder, "kafkaHash");
    }
}
