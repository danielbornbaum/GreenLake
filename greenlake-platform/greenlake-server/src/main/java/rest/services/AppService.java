package rest.services;

import apputil.AppManager;
import org.json.JSONObject;
import util.HTTPStatusCodes;
import util.LoggedClientCompatibleException;
import util.RestRequestManager;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Webservice to register, unregister and list installed apps
 */
@Path("/apps")
public class AppService
{
    private static final Logger LOGGER = Logger.getLogger(AppService.class.getName());

    /**
     * register an app
     *
     * @param parameters a valid app definition in json format with keys 'appId', 'orderNumber', 'name',
     *         'iconPath', 'url'
     * @return status code 200 and empty body or error code and error message
     */
    @POST
    @Path("/register")
    public Response registerApp(String parameters)
    {
        return new RestRequestManager()
                .setParameters(parameters)
                .assertKeys(new String[]{"appId", "orderNumber", "name", "iconPath", "url"})
                .execute(restRequestManager -> {
                    if (restRequestManager.getInt("orderNumber") < 1)
                    {
                        restRequestManager.setCustomError(new LoggedClientCompatibleException(
                                new IllegalArgumentException(
                                        "A client tried to register an app. The orderNumber was < 1."),
                                "Die orderNumber muss 1 oder größer 1 sein.", HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST,
                                LOGGER, Level.WARNING));
                        return;
                    }

                    boolean registered = AppManager.getInstance()
                            .registerApp(restRequestManager.getString("appId"),
                                         restRequestManager.getInt("orderNumber"),
                                         restRequestManager.getString("name"),
                                         restRequestManager.getString("url"),
                                         restRequestManager.getString("iconPath"));

                    if (!registered)
                    {
                        restRequestManager.setCustomError(new LoggedClientCompatibleException(
                                new IllegalArgumentException(
                                        String.format(
                                                "A client tried to register an app, but an app with id %s is already registered",
                                                restRequestManager.getString("appId"))),
                                String.format("Eine App mit der id %s ist bereits registriert",
                                              restRequestManager.getString("appId")),
                                HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST,
                                LOGGER, Level.WARNING));
                    }
                })
                .generateResponse();
    }

    /**
     * unregister an app
     *
     * @param parameters a request in json format with key 'appId'
     * @return status code 200 and empty body or error code and error message
     */
    @DELETE
    @Path("/unregister")
    public Response unregisterApp(String parameters)
    {
        return new RestRequestManager()
                .setParameters(parameters)
                .assertKeys(new String[]{"appId"})
                .execute(restRequestManager -> {
                    boolean unregistered = AppManager.getInstance()
                            .unregisterApp(restRequestManager.getString("appId"));

                    if (!unregistered)
                    {
                        restRequestManager.setCustomError(new LoggedClientCompatibleException(
                                new IllegalArgumentException(
                                        String.format(
                                                "A client tried to unregister an app, but an app with the id %s is not registered.",
                                                restRequestManager.getString("appId"))),
                                String.format("Eine App mit der id %s ist nicht registriert",
                                              restRequestManager.getString("appId")),
                                HTTPStatusCodes.CLIENT_ISSUES.CONFLICT,
                                LOGGER, Level.WARNING));
                    }
                })
                .generateResponse();
    }

    /**
     * get all registered apps
     *
     * @return response of type {apps:[json array of apps]}
     */
    @GET
    @Path("/get")
    public Response getApps()
    {
        return new RestRequestManager().execute(restRequestManager -> {
            restRequestManager.setMessage(new JSONObject().put("apps", AppManager.getInstance().getAppsAsJSON()));
        }).generateResponse();
    }
}
