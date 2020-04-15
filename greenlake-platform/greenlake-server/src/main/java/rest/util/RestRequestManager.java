package rest.util;

import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Consumer;

public class RestRequestManager extends JSONObject
{
    private String message = "";
    private String mediaType = MediaType.APPLICATION_JSON;
    private boolean implicitMediaType = true;

    private int statusCode = HTTPStatusCodes.SUCCESS_CODES.OK.getCode();

    public RestRequestManager(String json)
    {
        super(json);
    }

    public RestRequestManager assertKeys(String[] keys)
    {
        StringBuilder missingKeys = new StringBuilder();

        for (String key : keys)
        {
            if (!has(key))
            {
                statusCode = HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST.getCode();
                if (!"".equals(missingKeys.toString()))
                {
                    missingKeys.append(", ");
                }
                missingKeys.append(key);
            }
        }

        if ("".equals(missingKeys.toString()))
        {
            message = "Your request is missing this/those key(s): " + missingKeys.toString();
            mediaType = MediaType.TEXT_PLAIN;
        }

        return this;
    }

    public RestRequestManager setMediaType(String mediaType)
    {
        this.mediaType = mediaType;
        implicitMediaType = false;
        return this;
    }

    public RestRequestManager setMessage(String message)
    {
        this.message = message;

        if (implicitMediaType)
        {
            mediaType = MediaType.TEXT_PLAIN;
        }

        return this;
    }

    public RestRequestManager setMessage(JSONObject message)
    {
        this.message = message.toString();

        if (implicitMediaType)
        {
            mediaType = MediaType.APPLICATION_JSON;
        }

        return this;
    }

    public RestRequestManager setStatusCode(HTTPStatusCodes.Codes code)
    {
        statusCode = code.getCode();
        return this;
    }

    public RestRequestManager execute(Consumer<RestRequestManager> consumer)
    {
        consumer.accept(this);
        return this;
    }

    public Response generateResponse()
    {
        return Response.status(statusCode).header("Content-Type", mediaType).entity(message).build();
    }
}
