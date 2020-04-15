package rest;

import rest.services.AppService;
import rest.services.SetupService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("")
public class GreenLakePlatform extends Application
{

    @Override
    public Set<Class<?>> getClasses()
    {
        return new HashSet<>(Arrays.asList(AppService.class, SetupService.class));
    }

}
