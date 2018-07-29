package servlet;

import connect.CConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/account")
public class ProcessingServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingServlet.class);
    CConnect connect = new CConnect();

    public ProcessingServlet() throws SQLException {
    }

    @GET
    @Path("/helloworld")
    @Consumes("application/json")
    @Produces("application/json")
    public Response helloworld() throws SQLException {
        boolean result = connect.createAcc("Вася", "Вася", "Вася");
        if(result) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }



}
