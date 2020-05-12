package rest.services;

import kafka.KafkaUtil;
import org.json.JSONObject;
import util.LoggedClientCompatibleException;
import util.RestRequestManager;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Interface for dealing with Kafka and Zookeeper
 */
@Path("/kafka")
public class KafkaService
{
    /**
     * Whether Kafka could be accessed
     *
     * @return client response with key available
     */
    @GET
    @Path("/kafkaAvailable")
    public Response kafkaAvailable()
    {
        return new RestRequestManager()
                .execute(restRequestManager -> {
                        restRequestManager.setMessage(
                                new JSONObject().put("available", KafkaUtil.getInstance().kafkaAvailable()));
                })
                .generateResponse();
    }

    /**
     * Whether Zookeeper could be accessed
     *
     * @return client response with key available
     */
    @GET
    @Path("/zookeeperAvailable")
    public Response zookeeperAvailable()
    {
        return new RestRequestManager()
                .execute(restRequestManager -> {
                        restRequestManager
                                .setMessage(new JSONObject()
                                                    .put("available", KafkaUtil.getInstance().zookeeperAvailable()));
                })
                .generateResponse();
    }

    /***
     * @param params name, partition and replications as JSON
     * @return 200 and no message or error code and error message
     */
    @POST
    @Path("/addTopic")
    public Response addTopic(String params)
    {
        return new RestRequestManager()
                .setParameters(params)
                .assertKeys(new String[]{"name", "partitions", "replications"})
                .execute(restRequestManager -> {
                    KafkaUtil.getInstance().createTopic(restRequestManager.getString("name"),
                                                        restRequestManager.getInt("partitions"),
                                                        (short) restRequestManager.getInt("replications"));
                }).generateResponse();
    }

    /***
     * @return 200 and list of topic or error code and error message
     */
    @GET
    @Path("/listTopics")
    public Response listTopics()
    {
        return new RestRequestManager()
                .execute(restRequestManager -> {
                    try
                    {
                        restRequestManager
                                .setMessage(new JSONObject().put("topics", KafkaUtil.getInstance().listTopics()));
                    }
                    catch (LoggedClientCompatibleException e)
                    {
                        restRequestManager.setCustomError(e);
                    }
                })
                .generateResponse();
    }

    /**
     * @param params name of the topic to delete inside a json object
     * @return 200 or error message and error code
     */
    @DELETE
    @Path("/deleteTopic")
    public Response deleteTopic(String params)
    {
        return new RestRequestManager()
                .setParameters(params)
                .assertKeys(new String[]{"name"})
                .execute(restRequestManager -> {
                    KafkaUtil.getInstance().deleteTopic(restRequestManager.getString("name"));
                })
                .generateResponse();
    }
}
