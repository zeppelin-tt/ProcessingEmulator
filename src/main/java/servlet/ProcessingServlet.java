package servlet;


import connect.Connect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlet.classes.Action;
import servlet.classes.FilteredRequest;
import servlet.classes.ResponseData;
import servlet.classes.ServerResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.sql.SQLException;


@Path("/account")
public class ProcessingServlet {
    Logger LOG = LoggerFactory.getLogger(Connect.class);

    private Connect connect = new Connect();
    private boolean result = true;
    private String message = null;
    private ResponseData responseData = null;

    public ProcessingServlet() throws SQLException {
    }

    @GET
    @Path("/view/page={numPage}&limitRows={limitRows}&hideClosed={hideClosed}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServerResponse getView(@PathParam("numPage") String numPage, @PathParam("limitRows") String limitRows, @PathParam("hideClosed") String hideClosed ) {
        try {
            responseData = connect.getResponseDataByPage(numPage, limitRows, hideClosed);
        } catch (Exception e) {
            result = false;
            message = e.getMessage();
            LOG.info(message);
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
        } catch (Exception e) {
            result = false;
            message = e.getMessage();
        }
        return new ServerResponse(String.valueOf(result), message, responseData);
    }

    @POST
    @Path("/action")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServerResponse doAction(Action action) {
        LOG.info(action.toString());
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
                    result = connect.transfer(action.getAccNum(), new BigDecimal("-".concat(action.getMoney())));
                    break;
                case "transfer_plus":
                    result = connect.transfer(action.getAccNum(), new BigDecimal(action.getMoney()));
                    break;
                case "transfer_to":
                    result = connect.transfer(action.getAccNum(), action.getSecondAccNum(), new BigDecimal(action.getMoney()));
                    break;
            }
        } catch (Exception e) {
            result = false;
            message = e.getMessage();
            LOG.info(message);
        }
        return new ServerResponse(String.valueOf(result), message, null);
    }

}
