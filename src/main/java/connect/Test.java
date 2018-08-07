package connect;

import servlet.FilteredRequest;
import servlet.ResponseData;

import java.sql.SQLException;


public class Test {
    public static void main(String[] args) throws SQLException, NoSuchFieldException {
        Connect c = new Connect();
//        FilteredRequest fr = new FilteredRequest("0", null, "щен", null, null, null, null);
//        ResponseData rd = c.getResponseDataByPage(fr);
//        rd.getView().forEach(e -> System.out.println(e.getInitials()));

//        System.out.println(c.getResponseDataByPage("1"));
//        c.getPresentationView(String.valueOf(0)).forEach(System.out::println);

    }
}
