package setup;

import javax.ejb.Stateless;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

@Stateless
public class SetupBean
{
    private static SetupBean instance = null;
    private static final Logger LOGGER = Logger.getLogger(SetupBean.class.getName());

    private int kafkaSetupProgress = -1;

    private SetupBean()
    {
    }

    public static SetupBean getInstance()
    {
        if (instance == null)
        {
            instance = new SetupBean();
        }

        return instance;
    }

    public void installKafka() throws IOException
    {
        String strUrl = "http://mirror.23media.de/apache/kafka/2.4.1/kafka_2.11-2.4.1.tgz";
        URL url = new URL(strUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300)
        {
            int contentLength = connection.getContentLength();

            if (contentLength <= 0)
            {
                throw new IOException(String.format("Content length of kafka download was %d", contentLength));
            }

            InputStream webInputStream = connection.getInputStream();

            int bytesRead = -1;
            long bytesReadTotal = 0;
            byte[] buffer = new byte[4096];
            while ((bytesRead = webInputStream.read(buffer)) != -1)
            {
                bytesReadTotal += bytesRead;
                kafkaSetupProgress = (int) Math.round((double) bytesReadTotal / contentLength * 10000) / 100;
            }

            webInputStream.close();
        }
        else
        {
            throw new IOException(String.format("Response Code was %d", connection.getResponseCode()));
        }
    }

    public int getKafkaSetupProgress()
    {
        if (kafkaSetupProgress == 100)
        {
            kafkaSetupProgress = -1;
            return 100;
        }
        return kafkaSetupProgress;
    }
}
