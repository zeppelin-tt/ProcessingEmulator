package connect;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;

public class Test {
    public static void main(String[] args) throws SQLException {
        Connect c = new Connect();
        c.getPresentationView().forEach(System.out::println);
    }
}
