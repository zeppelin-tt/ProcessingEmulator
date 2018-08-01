package servlet;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("")
public class MainApplication extends Application {

    private Set<Object> singletons = new HashSet<Object>();
    private HashSet<Class<?>> classes = new HashSet<Class<?>>();

    public MainApplication() throws SQLException {
        CorsFilter corsFilter = new CorsFilter();
        corsFilter.getAllowedOrigins().add("*");
        corsFilter.setAllowedMethods("OPTIONS, GET, POST, DELETE, PUT, PATCH");
        singletons.add(corsFilter);
        singletons.add(new ProcessingServlet());

    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Override
    public HashSet<Class<?>> getClasses() {
        return classes;
    }
}

