package processing;

import java.util.concurrent.ScheduledFuture;

public class Process
{
    private DataProcessor processor;
    private long scheduleTime;
    private ScheduledFuture<?> future;

    public Process(DataProcessor processor, long scheduleTime, ScheduledFuture<?> future){
        this.processor = processor;
        this.future = future;
        this.scheduleTime = scheduleTime;
    }

    public DataProcessor getProcessor()
    {
        return processor;
    }

    public long getScheduleTime()
    {
        return scheduleTime;
    }

    public ScheduledFuture<?> getFuture()
    {
        return future;
    }

    public void setFuture(ScheduledFuture<?> future){
        this.future = future;
    }
}
