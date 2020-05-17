package processing;

import org.json.JSONArray;
import util.HTTPStatusCodes;
import util.LoggedClientCompatibleException;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessorPool extends ScheduledThreadPoolExecutor
{
    private static ProcessorPool instance;
    private static final int defaultPoolSize = 100;
    private static final Logger LOGGER = Logger.getLogger(ProcessorPool.class.getName());

    private HashMap<UUID, Process> processors = new HashMap<>();

    private ProcessorPool(int corePoolSize)
    {
        super(corePoolSize);
    }

    public static ProcessorPool getInstance()
    {
        if (instance == null)
        {
            instance = new ProcessorPool(defaultPoolSize);
        }

        return instance;
    }

    public void addDataProcessor(DataProcessor processor, long schedulingTime)
    {
        ScheduledFuture<?> scheduledFuture = scheduleAtFixedRate(processor, schedulingTime, schedulingTime,
                                                                 TimeUnit.MILLISECONDS);
        processors.put(processor.getId(), new Process(processor, schedulingTime, scheduledFuture));
    }

    public void alterProcess(UUID processorId, long schedulingTime,
                             DataProcessor.SourceDestination source,
                             DataProcessor.SourceDestination destination,
                             String javascript,
                             int minDataSetSize,
                             int maxDataSetSize,
                             String consumerGroup,
                             String topicOrDataIn,
                             String topicOrDataOut,
                             boolean forgetting,
                             long timeout,
                             String title)
    {
        Process processor = processors.get(processorId);
        processor.getFuture().cancel(false);
        processor.getProcessor()
                .update(source, destination, javascript, minDataSetSize, maxDataSetSize, consumerGroup, topicOrDataIn,
                        topicOrDataOut, forgetting, timeout, title);
        ScheduledFuture<?> scheduledFuture = scheduleAtFixedRate(processor.getProcessor(), schedulingTime,
                                                                 schedulingTime, TimeUnit.MILLISECONDS);
        processor.setFuture(scheduledFuture);
    }

    public void removeDataProcessor(UUID uuid) throws LoggedClientCompatibleException
    {
        if (!processors.containsKey(uuid))
        {
            throw new LoggedClientCompatibleException(new IllegalArgumentException("This process is not present"),
                                                      "Der Job exisitert (nicht?) mehr.",
                                                      HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST,
                                                      LOGGER, Level.WARNING);
        }

        processors.get(uuid).getFuture().cancel(false);
        remove(processors.get(uuid).getProcessor());
        processors.remove(uuid);
    }

    public JSONArray listDataProcessors()
    {
        JSONArray processorsAsJSON = new JSONArray();
        processors.values().forEach(processor -> processorsAsJSON
                .put(processor.getProcessor().getAsJSON().put("schedulingTime", processor.getScheduleTime())));
        return processorsAsJSON;
    }

    public void unregisterAll(){
        processors.values().forEach(process -> {
            process.getFuture().cancel(true);
            process.getProcessor().purge();
        });
    }
}
