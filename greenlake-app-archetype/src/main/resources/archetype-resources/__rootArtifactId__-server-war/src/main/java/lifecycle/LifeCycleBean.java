package lifecycle;

import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.ObjectName;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Bean that manages the lifecycle with deployment of this app
 */
@Singleton
@Startup
@SuppressWarnings("unused")
public class LifeCycleBean
{
    private static final Logger LOGGER = Logger.getLogger(LifeCycleBean.class.getName());
    private String address;
    private int port;

    // Change these Settings to your liking ////////////////////////////////////////////////////////////////////////////
    private static final String appId = "${rootArtifactId}"; // id inside the platform to deploy the app
    private static final int orderNumber = Integer.MAX_VALUE; // position of the app inside the menu
    private static final String name = "${rootArtifactId}"; // display name of the app
    private static final String iconPath = "${rootArtifactId}/images/icons/app-icon.jpg"; // path to the apps icon
    private static final String url = "/${rootArtifactId}"; // path to the app
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public LifeCycleBean()
    {
        try
        {
            //http port
            port = (int) ManagementFactory.getPlatformMBeanServer()
                    .getAttribute(new ObjectName(
                                          "jboss.as:socket-binding-group=standard-sockets,socket-binding=http"),
                                  "port");
            //http adress
            address = "http://".concat(String.valueOf(ManagementFactory.getPlatformMBeanServer()
                                                              .getAttribute(new ObjectName("jboss.as:interface=public"),
                                                                            "inet-address")));
            //port offset
            port += (int) ManagementFactory.getPlatformMBeanServer()
                    .getAttribute(new ObjectName(
                                          "jboss.as:socket-binding-group=standard-sockets"),
                                  "port-offset");
        }
        catch (Exception e)
        {
            //failDeployment(e);
        }
    }

    /**
     * Code executed after deployment Registration at the platform via rest client app_name, etc defined as constants
     * above
     */
    @PostConstruct
    @SuppressWarnings("unused")
    public void postConstruct()
    {
        HttpURLConnection conn = null;

        try
        {
            URL url = new URL(String.format("%s:%d/greenlake-platform/apps/register", address, port));

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
            writer.write(new JSONObject()
                                 .put("appId", appId)
                                 .put("orderNumber", orderNumber)
                                 .put("name", name)
                                 .put("iconPath", iconPath)
                                 .put("url", LifeCycleBean.url)
                                 .toString());
            writer.close();

            if (conn.getResponseCode() / 100 != 2)
            {
                failDeployment(new IllegalStateException(String.format(
                        "The app registration service responded with code %d. Message: %s",
                        conn.getResponseCode(), new String(conn.getInputStream().readAllBytes()))));
            }

            LOGGER.info("\n\n\nRegistered app ${rootArtifactId}");
        }
        catch (IOException e)
        {
            failDeployment(e);
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    private void failDeployment(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        LOGGER.severe(sw.toString());
        throw new RuntimeException("Could not deploy");
    }


    @PreDestroy
    public void preDestroy()
    {
        HttpURLConnection conn = null;

        try
        {
            URL url = new URL(String.format("%s:%d/greenlake-platform/apps/unregister", address, port));

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
            writer.write(new JSONObject()
                                 .put("appId", appId)
                                 .toString());
            writer.close();

            if (conn.getResponseCode() / 100 != 2)
            {
                throw new IllegalStateException(
                        String.format("Could not unregister the app. Response code was %d. Message: %s",
                                      conn.getResponseCode(), new String(conn.getInputStream().readAllBytes())));
            }
        }
        catch (IOException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();

            LOGGER.severe("Could not unregister app: \n".concat(sw.toString()));
        }
    }
}
