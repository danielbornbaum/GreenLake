package rest;

import rest.services.KafkaService;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

/**
 * Application that provides the rest backend of this app
 */
@ApplicationPath("")
public class KafkaConfiguration extends Application
{
    /**
     * @return all classes that contain code for a rest call
     */
    @Override
    public Set<Class<?>> getClasses()
    {
        return new HashSet<>(Collections.singletonList(KafkaService.class));
    }
}
