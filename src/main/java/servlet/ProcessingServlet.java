package servlet;


import connect.CConnect;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Arrays;


@Path("/action")
public class ProcessingServlet {

    CConnect connect = new CConnect();

    public ProcessingServlet() throws SQLException {
    }


    @GET
    // TODO: 30.07.2018 нужна кириллица
    @Path("/{param}")
//    @Consumes("application/json")
//    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response doGet(@PathParam("param") String query) throws SQLException {
        String operation = query.substring(0, query.indexOf("="));
        String paramStr = query.substring(query.indexOf("=") + 1);
        String[] params = paramStr.split("&");
        boolean result = false;
        switch (operation) {
            case "create":
                result = connect.createAcc(params[0], params[1], params[2]);
                break;
            case "close":
                result = connect.closeAcc(paramStr);
                break;
            case "block":
                result = connect.blockAcc(paramStr);
                break;
            case "transfer_minus":
                result = connect.transfer(params[0], -Float.valueOf(params[1]));
                break;
            case "transfer_plus":
                result = connect.transfer(params[0], Float.valueOf(params[1]));
                break;
            case "transfer_to":
                result = connect.transfer(params[0], params[1], Float.valueOf(params[2]));
                break;
        }

        if (result) {
            String resultStr = "Операция успешно выполнена!";
            return Response.status(200).entity(resultStr).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

    }

}