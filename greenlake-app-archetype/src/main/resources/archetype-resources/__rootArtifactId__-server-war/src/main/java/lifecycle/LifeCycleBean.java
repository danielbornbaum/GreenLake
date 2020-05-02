import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.ObjectName;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
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
    private static final String iconPath = "${rootArtifactId}/images/icon/app-icon.jpg"; // path to the apps icon
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
            address = String.valueOf(ManagementFactory.getPlatformMBeanServer()
                                             .getAttribute(new ObjectName("jboss.as:interface=public"),
                                                           "inet-address"));
            //port offset
            port += (int) ManagementFactory.getPlatformMBeanServer()
                    .getAttribute(new ObjectName(
                                          "jboss.as:socket-binding-group=standard-sockets"),
                                  "port-offset");
        }
        catch (Exception e)
        {
            failDeployment(e);
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
        Client client = ClientBuilder.newClient();
        WebTarget webTarget
                = client.target(String.format("%s:%d/greenlake-platform/apps/register", address, port));
        Invocation.Builder invocationBuilder
                = webTarget.request(MediaType.APPLICATION_JSON);
        Response response
                = invocationBuilder.post(Entity.json(
                new JSONObject().put("appId", appId)
                        .put("orderNumber", orderNumber)
                        .put("name", name)
                        .put("iconPath", iconPath)
                        .put("url", url)));

        if (response.getStatus() != 200)
        {
            failDeployment(new IllegalStateException(
                    "Response code from app registration service was ".concat(String.valueOf(response.getStatus()))));
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
        Client client = ClientBuilder.newClient();
        WebTarget webTarget
                = client.target(String.format("%s:%d/greenlake-platform/apps/unregister", address, port));
        Invocation.Builder invocationBuilder
                = webTarget.request(MediaType.APPLICATION_JSON);
        Response response
                = invocationBuilder.post(Entity.json(
                new JSONObject().put("appId", appId)));

        if (response.getStatus() / 100 != 2)
        {
            LOGGER.warning("Could not unregister app, because service responded: "
                                   .concat(new JSONObject(response.getEntity().toString()).getString("message")));
        }
    }
}
