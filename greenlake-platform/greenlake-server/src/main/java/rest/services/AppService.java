package rest.services;

import apputil.AppManager;
import org.json.JSONObject;
import rest.util.HTTPStatusCodes.CLIENT_ISSUES;
import rest.util.RestRequestManager;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/apps")
public class AppService
{
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
                        restRequestManager
                                .setCustomError(CLIENT_ISSUES.BAD_REQUEST, "orderNumber must be 1 or greater");
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
                        restRequestManager.setCustomError(CLIENT_ISSUES.CONFLICT,
                                                          String.format("An app with id %s is already registered",
                                                                        restRequestManager.getString("appId")));
                    }
                    else
                    {
                        restRequestManager.setMessage(new JSONObject(parameters));
                    }
                })
                .generateResponse();
    }

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
                        restRequestManager.setCustomError(CLIENT_ISSUES.CONFLICT,
                                                          String.format("An app with the id %s is not registered.",
                                                                        restRequestManager.getString("appId")));
                    }
                    else
                    {
                        restRequestManager.setMessage(new JSONObject(parameters));
                    }
                })
                .generateResponse();
    }

    @GET
    @Path("/get")
    public Response getApps()
    {
        return new RestRequestManager().execute(restRequestManager -> {
            restRequestManager.setMessage(new JSONObject().put("apps", AppManager.getInstance().getAppsAsJSON()));
        }).generateResponse();
    }
}
