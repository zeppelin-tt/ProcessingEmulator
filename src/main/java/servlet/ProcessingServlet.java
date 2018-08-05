package servlet;


import connect.Connect;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;


@Path("/account")
public class ProcessingServlet {

    Connect connect = new Connect();

    public ProcessingServlet() throws SQLException {
    }


//    @GET
//    // TODO: 30.07.2018 нужна кириллица
//    @Path("/{param}")
////    @Consumes("application/json")
////    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
//    public Response doGet(@PathParam("param") String query) throws SQLException {
//        String operation = query.substring(0, query.indexOf("="));
//        String paramStr = query.substring(query.indexOf("=") + 1);
//        String[] params = paramStr.split("&");
//        boolean result = false;
//        switch (operation) {
//            case "create":
//                result = connect.doAction(params[0], params[1], params[2]);
//                break;
//            case "close":
//                result = connect.closeAcc(paramStr);
//                break;
//            case "block":
//                result = connect.blockAcc(paramStr);
//                break;
//            case "transfer_minus":
//                result = connect.transfer(params[0], -Float.valueOf(params[1]));
//                break;
//            case "transfer_plus":
//                result = connect.transfer(params[0], Float.valueOf(params[1]));
//                break;
//            case "transfer_to":
//                result = connect.transfer(params[0], params[1], Float.valueOf(params[2]));
//                break;
//        }
//
//        if (result) {
//            String resultStr = "Операция успешно выполнена!";
//            return Response.status(200).entity(resultStr).build();
//        } else {
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }
//
//    }

    @GET
    @Path("/{numPage}/view/{numPage}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServerResponse getView(@PathParam("numPage") String numPage) {
        boolean result = false;
        String message = null;
        ResponseData rd = null;
        try {
            rd = connect.getResponseDataByPage(numPage);
            result = true;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return new ServerResponse(String.valueOf(result), message, rd);
    }

    @POST
    @Path("/action")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServerResponse doAction(Action action) {
        boolean result = false;
        String message = null;
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


//    @POST
//    @Path("/create")
//    @Consumes("application/json")
//    @Produces("application/json")
//    public boolean doAction(Action actions) throws SQLException {
//        return connect.doAction(actions.getLastName(), actions.getFirstName(), actions.getPatronymic());
//    }
//
//    @POST
//    @Path("/close")
//    @Consumes("application/json")
//    @Produces("application/json")
//    public boolean closeAcc (Action actions) throws SQLException {
//        return connect.closeAcc(actions.getAccNum());
//
//    }
//
//    @POST
//    @Path("/block")
//    @Consumes("application/json")
//    @Produces("application/json")
//    public boolean blockAcc(Action actions) throws SQLException {
//        return connect.blockAcc(actions.getAccNum());
//    }
//
//    @POST
//    @Path("/transfer")
//    @Consumes("application/json")
//    @Produces("application/json")
//    public boolean transfer(Action actions) throws SQLException {
//        if (actions.getSecondAccNum() != null) {
//            return connect.transfer(actions.getAccNum(), actions.getSecondAccNum(), Float.valueOf(actions.getMoney()));
//        } else {
//            return connect.transfer(actions.getAccNum(), Float.valueOf(actions.getMoney()));
//        }
//    }


}
