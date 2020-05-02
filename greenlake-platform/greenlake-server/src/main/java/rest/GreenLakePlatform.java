package rest;

import rest.services.AppService;
import rest.services.SetupService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS REST-Application that provides the backend for this application
 */
@ApplicationPath("")
public class GreenLakePlatform extends Application
{

    /**
     * @return set of jax-rs webservice relevant classes
     */
    @Override
    public Set<Class<?>> getClasses()
    {
        return new HashSet<>(Arrays.asList(AppService.class, SetupService.class));
    }

}
