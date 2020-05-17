package rest;

import rest.services.JobService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Application that provides the rest backend of this app
 */
@ApplicationPath("")
public class DataProcessing extends Application
{
    /**
     * @return all classes that contain code for a rest call
     */
    @Override
    public Set<Class<?>> getClasses()
    {
        return new HashSet<>(Collections.singletonList(JobService.class));
    }
}
