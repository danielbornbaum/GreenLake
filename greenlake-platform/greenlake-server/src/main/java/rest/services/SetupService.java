package rest.services;

import org.json.JSONObject;
import settings.SettingsManager;
import setup.HadoopSetupBean;
import setup.KafkaSetupBean;
import setup.SetupBean;
import setup.SetupManager;
import util.HTTPStatusCodes;
import util.RestRequestManager;

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
    private JSONObject config = new JSONObject();

    @GET
    @Path("/status")
    public Response status()
    {
        return new RestRequestManager()
                .execute(restRequestManager -> {
                    try
                    {
                        restRequestManager
                                .setMessage(new JSONObject().put("performed", SetupManager.isPerformed()));
                    }
                    catch (IOException e)
                    {
                        restRequestManager.setCustomError(HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                                                          "Ein serverseitiger Fehler ist aufgetreten");
                        LOGGER.severe(e.getLocalizedMessage());
                    }
                })
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
                    boolean present = false;
                    try
                    {
                        present = KafkaSetupBean.getInstance()
                                .validateFolder(restRequestManager.getString("path"));
                    }
                    catch (IOException e)
                    {
                        restRequestManager.setCustomError(HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                                                          e.getLocalizedMessage());
                        return;
                    }

                    String path = restRequestManager.getString("path").replace("\\", "/");
                    SettingsManager.getInstance().setSetting("pathToKafka", path, true);
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
                    boolean present = false;
                    try
                    {
                        present = HadoopSetupBean.getInstance()
                                .validateFolder(restRequestManager.getString("path"));
                    }
                    catch (IOException e)
                    {
                        restRequestManager.setCustomError(HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                                                          e.getLocalizedMessage());
                        return;
                    }

                    String path = restRequestManager.getString("path").replace("\\", "/");
                    SettingsManager.getInstance().setSetting("pathToHadoop", path, true);
                    restRequestManager.setMessage(new JSONObject().put("present", present));
                })
                .generateResponse();
    }

    @POST
    @Path("/setKafkaProperties")
    public Response setKafkaProperties(String params)
    {
        if (SettingsManager.getInstance().hasSetting("pathToKafka"))
        {
            String pathToKafkaConfigFile = SettingsManager.getInstance().getSetting("pathToKafka")
                    .concat("/config/server.properties");
            String pathToZookeeperConfigFile = SettingsManager.getInstance().getSetting("pathToKafka")
                    .concat("/config/zookeeper.properties");
            return setProperties(params, new String[]{pathToKafkaConfigFile, pathToZookeeperConfigFile},
                                 KafkaSetupBean.class);
        }
        else
        {
            return new RestRequestManager()
                    .setCustomError(HTTPStatusCodes.CLIENT_ISSUES.FORBIDDEN,
                                    "Pfad zu Kafka wurde noch nicht gesetzt")
                    .generateResponse();
        }
    }

    @POST
    @Path("/finish")
    public Response finish()
    {
        if (!SettingsManager.getInstance().hasSetting("pathToKafka"))
        {
            return new RestRequestManager().setCustomError(HTTPStatusCodes.CLIENT_ISSUES.METHOD_NOT_ALLOWED,
                                                           "Pfad zu Kafka wurde noch nicht gesetzt")
                    .generateResponse();
        }

        if (!SettingsManager.getInstance().hasSetting("pathToHadoop"))
        {
            return new RestRequestManager().setCustomError(HTTPStatusCodes.CLIENT_ISSUES.METHOD_NOT_ALLOWED,
                                                           "Pfad zu Kafka wurde noch nicht gesetzt")
                    .generateResponse();
        }

        return new RestRequestManager()
                .execute(restRequestManager ->
                         {
                             try
                             {
                                 SettingsManager.getInstance().persistSettings();
                             }
                             catch (IOException exception)
                             {
                                 LOGGER.severe(
                                         "Could not persist settings: \n".concat(exception.getLocalizedMessage()));
                                 restRequestManager.setCustomError(HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                                                                   "Ein serverseitiger Fehler ist aufgetreten");
                             }

                         }).generateResponse();
    }

    private Response installComponent(Class<?> component, String parameters)
    {
        return new RestRequestManager()
                .setParameters(parameters)
                .assertKeys(new String[]{"path"})
                .execute(restRequestManager -> {
                    File dir = new File(restRequestManager.getString("path"));

                    if (dir.exists())
                    {
                        if (!dir.isDirectory())
                        {
                            restRequestManager.setCustomError(HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST,
                                                              "Der angegebene Pfad ist kein Verzeichnis");
                            return;
                        }

                        if (Objects.requireNonNull(dir.list()).length > 0)
                        {
                            restRequestManager.setCustomError(HTTPStatusCodes.CLIENT_ISSUES.CONFLICT,
                                                              "Das angegebene Verzeichnis ist nicht leer");
                            return;
                        }
                    }

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
                                                "Das Kafka Setup wurde noch nicht gestartet");
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

        switch (component.getSimpleName())
        {
            case "KafkaSetupBean":
                bean = KafkaSetupBean.getInstance();
                break;
            case "HadoopSetupBean":
                bean = HadoopSetupBean.getInstance();
                break;
            default:
                throw new UnsupportedOperationException(
                        "There is no installer bean for this component: " + component.getSimpleName());
        }

        return bean;
    }

    private Response setProperties(String params, String[] pathsToConfigFiles, Class<?> setupBean)
    {
        RestRequestManager manager = new RestRequestManager()
                .setParameters(params)
                .assertKeys(new String[]{"config"});


        for (String pathToConfigFile : pathsToConfigFiles)
        {
            manager.execute(restRequestManager -> {
                File configFile = new File(pathToConfigFile);

                if (configFile.exists())
                {
                    try
                    {
                        getBeanFromName(setupBean)
                                .setProperties(new JSONObject(params).getJSONObject("config"), configFile);
                    }
                    catch (IOException e)
                    {
                        restRequestManager.setCustomError(HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                                                          "Konnte nicht auf die Datei zugreifen");
                        LOGGER.severe(e.getLocalizedMessage());
                    }
                }
                else
                {
                    restRequestManager.setCustomError(HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                                                      "Konfigurationsdatei existiert nicht im konfigurierten Pfad"
                                                              .concat(pathToConfigFile));
                }
            });
        }
        return manager.generateResponse();
    }
}
