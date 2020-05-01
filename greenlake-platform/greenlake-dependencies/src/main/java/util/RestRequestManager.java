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

@Stateful
public class RestRequestManager extends JSONObject
{
    private String message = "{}";
    private boolean successful = true;
    private static final Logger LOGGER = Logger.getLogger(RestRequestManager.class.getName());

    private int statusCode = HTTPStatusCodes.SUCCESS_CODES.OK.getCode();

    public RestRequestManager()
    {
        super("{}");
    }

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
            setError(HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST, "No valid JSON given", false);
            return this;
        }

        parametersJSON.keySet().forEach(key -> put(key, parametersJSON.get(key)));
        return this;
    }

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
                setError(HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST,
                         "Your request is missing this/those key(s): " + missingKeys.toString(), false);
            }
        }

        return this;
    }

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
                setError(HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                         "Ein serverseitiger Fehler ist aufgetreten", false);

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                LOGGER.severe(sw.toString());
            }
        }

        return this;
    }

    public RestRequestManager setCustomSuccessCode(HTTPStatusCodes.SUCCESS_CODES successCode)
    {
        statusCode = successCode.getCode();
        return this;
    }

    public RestRequestManager setCustomError(HTTPStatusCodes.CLIENT_ISSUES issueCode, String message, boolean log)
    {
        setError(issueCode, message, log);
        return this;
    }

    public RestRequestManager setCustomError(HTTPStatusCodes.CLIENT_ISSUES issueCode, String message)
    {
        setError(issueCode, message, false);
        return this;
    }

    public RestRequestManager setCustomError(HTTPStatusCodes.SERVER_ISSUES issueCode, String message, boolean log)
    {
        setError(issueCode, message, log);
        return this;
    }

    public RestRequestManager setCustomError(HTTPStatusCodes.SERVER_ISSUES issueCode, String message)
    {
        setError(issueCode, message, false);
        return this;
    }

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

    public Response generateResponse()
    {
        return Response.status(statusCode).header("Content-Type", MediaType.APPLICATION_JSON).entity(message).build();
    }

    private void setError(HTTPStatusCodes.Codes code, String message, boolean log)
    {
        successful = false;
        statusCode = code.getCode();
        this.message = new JSONObject().put("message", message).toString();

        if (log)
        {
            LOGGER.severe(String.format("%d: %s", code.getCode(), message));
        }
    }
}
