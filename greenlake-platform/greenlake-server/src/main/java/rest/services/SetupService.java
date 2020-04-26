package rest.services;

import org.json.JSONObject;
import rest.util.HTTPStatusCodes;
import rest.util.RestRequestManager;
import setup.HadoopSetupBean;
import setup.KafkaSetupBean;
import setup.SetupBean;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.logging.Logger;

@Path("/setup")
public class SetupService
{
    private static final Logger LOGGER = Logger.getLogger(SetupService.class.getName());

    @GET
    @Path("/status")
    public Response status()
    {
        return new RestRequestManager()
                .setMessage(new JSONObject().put("performed", false))
                .generateResponse();
    }

    @PUT
    @Path("/installKafka")
    public Response installKafka(String parameters)
    {
        return installComponent(KafkaSetupBean.class, parameters);
    }

    @PUT
    @Path("/installHadoop")
    public Response installHadoop(String parameters)
    {
        return installComponent(HadoopSetupBean.class, parameters);
    }

    @GET
    @Path("/kafkaSetupProgress")
    public Response kafkaStatus()
    {
        return getProgress(KafkaSetupBean.class);
    }

    @GET
    @Path("/hadoopSetupProgress")
    public Response hadoopStatus()
    {
        return getProgress(HadoopSetupBean.class);
    }

    @POST
    @Path("/validateKafkaAtLocation")
    public Response validateKafkaAtLocation(String params)
    {
        return new RestRequestManager()
                .setParameters(params)
                .assertKeys(new String[]{"path"})
                .execute(restRequestManager -> {
                    boolean present = KafkaSetupBean.getInstance()
                            .validateFolder(restRequestManager.getString("path"), Objects.requireNonNull(
                                    getClass().getClassLoader().getResource("kafka.hash")).getFile());

                    restRequestManager.setMessage(new JSONObject().put("present", present));
                })
                .generateResponse();
    }


    @POST
    @Path("/validateHadoopAtLocation")
    public Response validateHadoopAtLocation(String params)
    {
        return new RestRequestManager()
                .setParameters(params)
                .assertKeys(new String[]{"path"})
                .execute(restRequestManager -> {
                    boolean present = KafkaSetupBean.getInstance()
                            .validateFolder(restRequestManager.getString("path"), Objects.requireNonNull(
                                    getClass().getClassLoader().getResource("hadoop.hash")).getFile());

                    restRequestManager.setMessage(new JSONObject().put("present", present));
                })
                .generateResponse();
    }


    private Response installComponent(Class<?> component, String parameters)
    {
        return new RestRequestManager()
                .setParameters(parameters)
                .assertKeys(new String[]{"path"})
                .execute(restRequestManager -> {
                    File dir = new File(restRequestManager.getString("path"));

                    if (dir.exists() && Objects.requireNonNull(dir.list()).length > 0)
                    {
                        restRequestManager.setCustomError(HTTPStatusCodes.CLIENT_ISSUES.CONFLICT,
                                                          "Das angegebene Verzeichnis ist nicht leer");
                    }
                    else
                    {
                        try
                        {
                            getBeanFromName(component).install(restRequestManager.getString("path"));
                        }
                        catch (IOException e)
                        {
                            restRequestManager.setCustomError(HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                                                              e.getLocalizedMessage());

                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            LOGGER.severe(sw.toString());
                        }
                    }
                })
                .generateResponse();
    }

    private Response getProgress(Class<?> component)
    {
        return new RestRequestManager()
                .execute(restRequestManager -> {
                    int progress = getBeanFromName(component).getProgress();

                    if (progress == -1)
                    {
                        restRequestManager
                                .setCustomError(HTTPStatusCodes.CLIENT_ISSUES.FORBIDDEN,
                                                "The kafka setup was not started yet");
                    }
                    else
                    {
                        restRequestManager.setMessage(new JSONObject().put("progress", progress));
                    }
                })
                .generateResponse();
    }

    private SetupBean<?> getBeanFromName(Class<?> component)
    {
        SetupBean<?> bean;

        switch (component.getName())
        {
            case "KafkaSetupBean":
                bean = KafkaSetupBean.getInstance();
                break;
            case "HadoopSetupBean":
                bean = HadoopSetupBean.getInstance();
                break;
            default:
                throw new UnsupportedOperationException(
                        "There is no installer bean for this component");
        }

        return bean;
    }
}
