package servlet;


import connect.Connect;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;


@Path("/account")
public class ProcessingServlet {

    private Connect connect = new Connect();
    private boolean result = false;
    private String message = null;
    private ResponseData responseData = null;

    public ProcessingServlet() throws SQLException {
    }

    @GET
    @Path("/view/{numPage}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServerResponse getView(@PathParam("numPage") String numPage) {
        try {
            responseData = connect.getResponseDataByPage(numPage);
            result = true;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return new ServerResponse(String.valueOf(result), message, responseData);
    }

    @POST
    @Path("/filter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServerResponse getFilteredView(FilteredRequest filteredRequest) {
        try {
            responseData = connect.getResponseDataByPage(filteredRequest);
            result = true;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return new ServerResponse(String.valueOf(result), message, responseData);
    }

    @POST
    @Path("/action")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServerResponse doAction(Action action) {
        try {
            switch (action.getType()) {
                case "create":
                    result = connect.createAcc(action.getLastName(), action.getFirstName(), action.getPatronymic());
                    break;
                case "close":
                    result = connect.closeAcc(action.getAccNum());
                    break;
                case "block":
                    result = connect.blockAcc(action.getAccNum());
                    break;
                case "transfer_minus":
                    result = connect.transfer(action.getAccNum(), -Float.valueOf(action.getMoney()));
                    break;
                case "transfer_plus":
                    result = connect.transfer(action.getAccNum(), Float.valueOf(action.getMoney()));
                    break;
                case "transfer_to":
                    result = connect.transfer(action.getAccNum(), action.getSecondAccNum(), Float.valueOf(action.getMoney()));
                    break;
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        return new ServerResponse(String.valueOf(result), message, null);
    }

}
