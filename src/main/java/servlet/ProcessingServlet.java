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
//                result = connect.createAcc(params[0], params[1], params[2]);
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

//    @GET
//    @Path("/action")
//    @Produces("application/json")
//    public Actions getCreateAcc() {
//
//        Actions actions = new Actions();
//        actions.setLastName("Щенников");
//        actions.setFirstName("Максим");
//        actions.setPatronymic("Владимирович");
//
//        return actions;
//
//    }

    @POST
    @Path("/action")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServerResponse createAcc(Actions actions) {
        boolean result = false;
        String message = null;
        try {
            switch (actions.getType()) {
                case "create":
                    result = connect.createAcc(actions.getLastName(), actions.getFirstName(), actions.getPatronymic());
                    break;
                case "close":
                    result = connect.closeAcc(actions.getAccNum());
                    break;
                case "block":
                    result = connect.blockAcc(actions.getAccNum());
                    break;
                case "transfer_minus":
                    result = connect.transfer(actions.getAccNum(), -Float.valueOf(actions.getMoney()));
                    break;
                case "transfer_plus":
                    result = connect.transfer(actions.getAccNum(), Float.valueOf(actions.getMoney()));
                    break;
                case "transfer_to":
                    result = connect.transfer(actions.getAccNum(), actions.getSecondAccNum(), Float.valueOf(actions.getMoney()));
                    break;
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        return new ServerResponse(String.valueOf(result), message);
    }


//    @POST
//    @Path("/create")
//    @Consumes("application/json")
//    @Produces("application/json")
//    public boolean createAcc(Actions actions) throws SQLException {
//        return connect.createAcc(actions.getLastName(), actions.getFirstName(), actions.getPatronymic());
//    }
//
//    @POST
//    @Path("/close")
//    @Consumes("application/json")
//    @Produces("application/json")
//    public boolean closeAcc (Actions actions) throws SQLException {
//        return connect.closeAcc(actions.getAccNum());
//
//    }
//
//    @POST
//    @Path("/block")
//    @Consumes("application/json")
//    @Produces("application/json")
//    public boolean blockAcc(Actions actions) throws SQLException {
//        return connect.blockAcc(actions.getAccNum());
//    }
//
//    @POST
//    @Path("/transfer")
//    @Consumes("application/json")
//    @Produces("application/json")
//    public boolean transfer(Actions actions) throws SQLException {
//        if (actions.getSecondAccNum() != null) {
//            return connect.transfer(actions.getAccNum(), actions.getSecondAccNum(), Float.valueOf(actions.getMoney()));
//        } else {
//            return connect.transfer(actions.getAccNum(), Float.valueOf(actions.getMoney()));
//        }
//    }


}
