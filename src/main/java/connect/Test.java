package connect;

import org.apache.commons.io.IOUtils;
import utils.Props;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;


public class Test {
    public static void main(String[] args) throws SQLException, NoSuchFieldException, IOException {

        String s = Props.get("name");
        System.out.println(s);
    }

    public static String getTextFromFile(String urlFile, String charset) throws IOException {
        FileInputStream inputStream = new FileInputStream(urlFile);
        return IOUtils.toString(inputStream, Charset.forName(charset));
    }
}
