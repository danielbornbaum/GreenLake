package util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An exception that is both suitable for the client as well as the server logging
 */
public class LoggedClientCompatibleException extends Exception
{
    private String clientMessage;
    private HTTPStatusCodes.Codes statusCode;

    /**
     * Constructor with the most detailed parameters using SERVER_ISSUE
     *
     * @param exception exception that is thrown
     * @param clientMessage message that is supposed to go to the client
     * @param issue issue code that is supposed to go to the client
     * @param logger logger with which the error is supposed to be logged
     * @param level log level of the message
     */
    public LoggedClientCompatibleException(Exception exception, String clientMessage,
                                           HTTPStatusCodes.SERVER_ISSUES issue, Logger logger, Level level)
    {
        handleConstructor(exception, clientMessage, issue, logger, level);
    }

    /**
     * Constructor abstracting on the detailed constructor with SERVER_ISSUE by setting the log level to SEVERE
     *
     * @param exception exception that is thrown
     * @param clientMessage message that is supposed to go to the client
     * @param issue issue code that is supposed to go to the client
     * @param logger logger with which the error is supposed to be logged
     */
    public LoggedClientCompatibleException(Exception exception, String clientMessage,
                                           HTTPStatusCodes.SERVER_ISSUES issue, Logger logger)
    {
        handleConstructor(exception, clientMessage, issue, logger, Level.SEVERE);
    }

    /**
     * Constructor with the most detailed parameters using CLIENT_ISSUES
     *
     * @param exception exception that is thrown
     * @param clientMessage message that is supposed to go to the client
     * @param issue issue code that is supposed to go to the client
     * @param logger logger with which the error is supposed to be logged
     * @param level log level of the message
     */
    public LoggedClientCompatibleException(Exception exception, String clientMessage,
                                           HTTPStatusCodes.CLIENT_ISSUES issue, Logger logger, Level level)
    {
        handleConstructor(exception, clientMessage, issue, logger, level);
    }

    /**
     * Constructor abstracting on the detailed constructor with CLIENT_ISSUES by setting the log level to SEVERE
     *
     * @param exception exception that is thrown
     * @param clientMessage message that is supposed to go to the client
     * @param issue issue code that is supposed to go to the client
     * @param logger logger with which the error is supposed to be logged
     */
    public LoggedClientCompatibleException(Exception exception, String clientMessage,
                                           HTTPStatusCodes.CLIENT_ISSUES issue, Logger logger)
    {
        handleConstructor(exception, clientMessage, issue, logger, Level.SEVERE);
    }

    /**
     * Constructor abstracting on the detailed constructor by setting the client message and status code to match 500
     *
     * @param exception
     * @param logger
     * @param level
     */
    public LoggedClientCompatibleException(Exception exception, Logger logger, Level level)
    {
        handleConstructor(exception, "Eine serverseitiger Fehler ist aufgetreten",
                          HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR, logger, level);
    }

    /**
     * Constructor abstracting on the constructor that presets client message and status code to match 500 by setting
     * the log level to SEVERE
     *
     * @param exception
     * @param logger
     */
    public LoggedClientCompatibleException(Exception exception, Logger logger)
    {
        this(exception, logger, Level.SEVERE);
    }

    /**
     * @return message that is supposed to be delivered to the client
     */
    public String getClientMessage()
    {
        return clientMessage;
    }

    /**
     * sets the status code that is supposed to be delivered to the client
     *
     * @return
     */
    public HTTPStatusCodes.Codes getStatusCode()
    {
        return statusCode;
    }

    /**
     * helper method to explicitly call the detailed constructor equivalent avoiding recursive constructor calls
     *
     * @param exception exception that is thrown
     * @param clientMessage message that is supposed to go to the client
     * @param issue issue code that is supposed to go to the client
     * @param logger logger with which the error is supposed to be logged
     * @param level log level of the message
     */
    private void handleConstructor(Exception exception, String clientMessage, HTTPStatusCodes.Codes issue,
                                   Logger logger, Level level)
    {
        this.clientMessage = clientMessage;

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        statusCode = issue;
        logger.log(level, exceptionAsString);
    }
}
