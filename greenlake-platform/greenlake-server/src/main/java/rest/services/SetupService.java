package rest.services;

import org.json.JSONObject;
import rest.util.HTTPStatusCodes;
import rest.util.HTTPStatusCodes.SUCCESS_CODES;
import rest.util.RestRequestManager;
import setup.SetupBean;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Logger;

@Path("/setup")
public class SetupService
{
    private static final Logger LOGGER = Logger.getLogger(SetupService.class.getName());

    @GET
    @Path("/status")
    public Response status()
    {
        RestRequestManager manager = new RestRequestManager("{}");
        manager.setStatusCode(SUCCESS_CODES.OK);
        manager.setMessage(new JSONObject().put("performed", "false"));

        return manager.generateResponse();
    }

    @PUT
    @Path("/installKafka")
    public Response installKafka()
    {
        RestRequestManager manager = new RestRequestManager("{}");
        manager.setMessage(new JSONObject().put("message", "started installation"));
        try
        {
            SetupBean.getInstance().installKafka();
        }
        catch (IOException e)
        {
            LOGGER.severe(e.getLocalizedMessage());
            manager.setStatusCode(HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR);
            manager.setMessage(new JSONObject().put("message", e.getLocalizedMessage()));
        }
        return manager.generateResponse();
    }

    @GET
    @Path("/kafkaSetupProgress")
    public Response kafkaStatus()
    {
        RestRequestManager manager = new RestRequestManager("{}");
        int progress = SetupBean.getInstance().getKafkaSetupProgress();

        if (progress == -1)
        {
            manager.setStatusCode(HTTPStatusCodes.CLIENT_ISSUES.FORBIDDEN);
            manager.setMessage(new JSONObject().put("message", "The kafka setup was not started"));
        }
        else
        {
            manager.setStatusCode(SUCCESS_CODES.OK);
            manager.setMessage(new JSONObject().put("progress", progress));
        }

        return manager.generateResponse();
    }
}
