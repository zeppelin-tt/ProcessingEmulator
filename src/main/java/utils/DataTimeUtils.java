package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataTimeUtils {

    public static String formatDateTime(String d, String inputF, String outputF) {
        DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern(inputF);
        DateTimeFormatter formatterOutput = DateTimeFormatter.ofPattern(outputF);
        LocalDateTime ld = LocalDateTime.parse(d, formatterInput);
        return ld.format(formatterOutput);
    }

}
