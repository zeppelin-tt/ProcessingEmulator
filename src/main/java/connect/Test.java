package connect;

import org.apache.commons.io.IOUtils;
import utils.Props;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;

import static utils.Action.CREATE;


public class Test {
    public static void main(String[] args) throws SQLException, NoSuchFieldException, IOException {

        System.out.println(CREATE.getAction());
    }


}
