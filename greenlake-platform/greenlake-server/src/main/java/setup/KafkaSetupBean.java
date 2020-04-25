package setup;

import javax.ejb.Stateless;
import java.io.IOException;

@Stateless
public class KafkaSetupBean extends SetupBean<KafkaSetupBean>
{
    private static KafkaSetupBean instance;

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
        super.install(path, "http://mirror.23media.de/apache/kafka/2.4.1/kafka_2.11-2.4.1.tgz");
    }

    @Override
    protected String getNameOfInstallationGoal()
    {
        return "Kafka";
    }
}
