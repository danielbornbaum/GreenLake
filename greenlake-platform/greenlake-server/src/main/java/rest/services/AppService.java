package rest.services;

import apputil.AppManager;
import rest.util.HTTPStatusCodes.CLIENT_ISSUES;
import rest.util.HTTPStatusCodes.SUCCESS_CODES;
import rest.util.RestRequestManager;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/app")
public class AppService
{
    @POST
    @Path("/register")
    public Response registerApp(String parameters)
    {
        RestRequestManager manager = new RestRequestManager(parameters)
                .assertKeys(new String[]{"appId", "translationKey", "iconPath", "url"});

        boolean registered = AppManager.getInstance()
                .registerApp(manager.getString("appId"), manager.getString("translationKey"),
                             manager.getString("iconPath"), manager.getString("url"));

        if (registered)
        {
            manager.setStatusCode(SUCCESS_CODES.OK)
                    .setMessage(String.format("Successfully registered App %s", manager.getString("appId")));
        }
        else
        {
            manager.setStatusCode(CLIENT_ISSUES.CONFLICT);
            manager.setMessage(String.format("An app with id %s is already registered", manager.getString("appId")));
        }

        return manager.generateResponse();
    }

    @DELETE
    @Path("/unregister")
    public Response unregisterApp(String parameters)
    {
        RestRequestManager manager = new RestRequestManager(parameters).assertKeys(new String[]{"appId"});

        boolean unregistered = AppManager.getInstance().unregisterApp(manager.getString("appId"));

        if (unregistered)
        {
            manager.setStatusCode(SUCCESS_CODES.OK)
                    .setMessage(String.format("Successfully unregistered App %s", manager.getString("appId")));
        }
        else
        {
            manager.setStatusCode(CLIENT_ISSUES.CONFLICT);
            manager.setMessage(String.format("An app with the id %s is not registered.", manager.getString("appId")));
        }

        return manager.generateResponse();
    }
}
