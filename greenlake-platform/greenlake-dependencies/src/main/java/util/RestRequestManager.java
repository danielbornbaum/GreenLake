package util;

import org.json.JSONException;
import org.json.JSONObject;

import javax.ejb.Stateful;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Class to manage REST requests with
 */
@Stateful
public class RestRequestManager extends JSONObject
{
    private String message = "{}";
    private boolean successful = true;
    private static final Logger LOGGER = Logger.getLogger(RestRequestManager.class.getName());

    private int statusCode = HTTPStatusCodes.SUCCESS_CODES.OK.getCode();

    /**
     * Sets a default value for the request parameters
     */
    public RestRequestManager()
    {
        super("{}");
    }

    /**
     * Set parameters from the rest call to this RestRequestManager given as JSON
     *
     * @param parameters in json format
     * @return this object for builder pattern
     */
    public RestRequestManager setParameters(String parameters)
    {
        keySet().forEach(this::remove);
        JSONObject parametersJSON;

        try
        {
            parametersJSON = new JSONObject(parameters);
        }
        catch (JSONException e)
        {
            setCustomError(new LoggedClientCompatibleException(e, "Ihrer Anfrage enthielt kein valides JSON",
                                                               HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST, LOGGER));
            return this;
        }

        parametersJSON.keySet().forEach(key -> put(key, parametersJSON.get(key)));
        return this;
    }

    /**
     * Asserts that keys are present in request throws error otherwise
     *
     * @param keys keys to validate
     * @return this object for builder pattern
     */
    public RestRequestManager assertKeys(String[] keys)
    {
        if (successful)
        {
            StringBuilder missingKeys = new StringBuilder();

            for (String key : keys)
            {
                if (!has(key))
                {
                    if (!"".equals(missingKeys.toString()))
                    {
                        missingKeys.append(", ");
                    }
                    missingKeys.append(key);
                }
            }

            if (!"".equals(missingKeys.toString()))
            {
                setCustomError(new LoggedClientCompatibleException(new IllegalStateException(String.format(
                        "A client started the following request, missing this/these key(s) '%s': \n\t %s",
                        missingKeys.toString(),
                        toString())), "Ihrer Anfrage fehlen der/die folgende(n) Parameter: "
                                                                           .concat(missingKeys.toString()),
                                                                   HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST, LOGGER));
            }
        }

        return this;
    }

    /**
     * executes code only if the previous steps in the constructor pattern evaluated to successful (asserting keys
     * etc.)
     *
     * @param consumer element that contains the code, gets this object for reference
     * @return this object for builder pattern purposes
     */
    public RestRequestManager execute(Consumer<RestRequestManager> consumer)
    {
        if (successful)
        {
            try
            {
                consumer.accept(this);
            }
            catch (Exception e)
            {
                setCustomError(new LoggedClientCompatibleException(e, LOGGER));

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                LOGGER.severe(sw.toString());
            }
        }

        return this;
    }

    /**
     * Sets a custom success code for the rest request other than 200
     *
     * @param successCode
     * @return
     */
    public RestRequestManager setCustomSuccessCode(HTTPStatusCodes.SUCCESS_CODES successCode)
    {
        statusCode = successCode.getCode();
        return this;
    }

    /**
     * Set a custom error other than 500
     *
     * @param exception exception that occured as LoggedClientCompatibleException
     * @return this object for builder pattern purposes
     */
    public RestRequestManager setCustomError(LoggedClientCompatibleException exception)
    {
        successful = false;
        statusCode = exception.getStatusCode().getCode();
        message = new JSONObject().put("message", exception.getClientMessage()).toString();
        return this;
    }

    /**
     * set message that is delivered to the client, if the rest request manager evaluates to successful
     *
     * @param message message to send to the client
     * @return this object for builder pattern purposes
     */
    public RestRequestManager setMessage(JSONObject message)
    {
        if (successful)
        {
            this.message = message.toString();
        }
        else
        {
            LOGGER.warning("A manual set message has been overwritten due to the occurrence of an error");
        }

        return this;
    }

    /**
     * @return javax Response object for this rest request, can be returned by the jax-rs method
     */
    public Response generateResponse()
    {
        return Response.status(statusCode).header("Content-Type", MediaType.APPLICATION_JSON).entity(message).build();
    }
}
