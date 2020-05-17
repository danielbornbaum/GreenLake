package rest.services;

import org.json.JSONObject;
import processing.DataProcessor;
import processing.ProcessorPool;
import util.LoggedClientCompatibleException;
import util.RestRequestManager;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/jobs")
public class JobService
{
    private static final String[] minimumFieldsForProcessor = new String[]{
            "title",
            "source",
            "destination",
            "javascript",
            "minDataSetSize",
            "maxDataSetSize",
            "topicOrDataIn",
            "topicOrDataOut",
            "forgetting",
            "timeout",
            "schedulingTime"};

    @POST
    @Path("/add")
    public Response add(String params)
    {
        return new RestRequestManager()
                .setParameters(params)
                .assertKeys(minimumFieldsForProcessor)
                .execute(restRequestManager -> {
                    if ("Kafka".equals(restRequestManager.getString("source")))
                    {
                        restRequestManager.assertKeys(new String[]{"consumerGroup"});
                    }
                }).execute(restRequestManager -> {
                    String consumerGroup = "Kafka".equals(restRequestManager.getString("source")) ?
                                           restRequestManager.getString("consumerGroup") : null;

                    DataProcessor processor = new DataProcessor(
                            DataProcessor.SourceDestination.fromString(restRequestManager.getString("source")),
                            DataProcessor.SourceDestination.fromString(restRequestManager.getString("destination")),
                            restRequestManager.getString("javascript"),
                            restRequestManager.getInt("minDataSetSize"),
                            restRequestManager.getInt("maxDataSetSize"),
                            consumerGroup,
                            restRequestManager.getString("topicOrDataIn"),
                            restRequestManager.getString("topicOrDataOut"),
                            restRequestManager.getBoolean("forgetting"),
                            restRequestManager.getLong("timeout"),
                            restRequestManager.getString("title")
                    );

                    ProcessorPool.getInstance()
                            .addDataProcessor(processor, restRequestManager.getLong("schedulingTime"));

                    restRequestManager.setMessage(new JSONObject().put("uuid", processor.getId()));
                })
                .generateResponse();
    }

    @POST
    @Path("/alter")
    public Response alter(String params)
    {
        return new RestRequestManager()
                .setParameters(params)
                .assertKeys(minimumFieldsForProcessor)
                .assertKeys(new String[]{"id"})
                .execute(restRequestManager -> {
                    if ("Kafka".equals(restRequestManager.getString("source")))
                    {
                        restRequestManager.assertKeys(new String[]{"consumerGroup"});
                    }
                })
                .execute(restRequestManager -> {
                    String consumerGroup = "Kafka".equals(restRequestManager.getString("source")) ?
                                           restRequestManager.getString("consumerGroup") : null;

                    ProcessorPool.getInstance().alterProcess(
                            UUID.fromString(restRequestManager.getString("id")),
                            restRequestManager.getLong("schedulingTime"),
                            DataProcessor.SourceDestination.fromString(restRequestManager.getString("source")),
                            DataProcessor.SourceDestination.fromString(restRequestManager.getString("destination")),
                            restRequestManager.getString("javascript"),
                            restRequestManager.getInt("minDataSetSize"),
                            restRequestManager.getInt("maxDataSetSize"),
                            consumerGroup,
                            restRequestManager.getString("topicOrDataIn"),
                            restRequestManager.getString("topicOrDataOut"),
                            restRequestManager.getBoolean("forgetting"),
                            restRequestManager.getLong("timeout"),
                            restRequestManager.getString("title"));
                })
                .generateResponse();
    }

    @DELETE
    @Path("/remove")
    public Response remove(String params)
    {
        return new RestRequestManager()
                .setParameters(params)
                .assertKeys(new String[]{"id"})
                .execute(restRequestManager -> {
                    try
                    {
                        ProcessorPool.getInstance()
                                .removeDataProcessor(UUID.fromString(restRequestManager.getString("id")));
                    }
                    catch (LoggedClientCompatibleException e)
                    {
                        restRequestManager.setCustomError(e);
                    }
                })
                .generateResponse();
    }

    @GET
    @Path("/list")
    public Response list()
    {
        return new RestRequestManager()
                .execute(restRequestManager -> {
                    restRequestManager
                            .setMessage(new JSONObject().put("jobs", ProcessorPool.getInstance().listDataProcessors()));
                })
                .generateResponse();
    }
}
