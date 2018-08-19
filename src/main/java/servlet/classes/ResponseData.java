package servlet.classes;

import java.util.List;

public class ResponseData{
    private List<TableFields> view;
    private String countRows;

    public ResponseData(List<TableFields> view, String countRows) {
        this.view = view;
        this.countRows = countRows;
    }

    public List<TableFields> getView() {
        return view;
    }

    public String getCountRows() {
        return countRows;
    }

    public void setView(List<TableFields> view) {
        this.view = view;
    }

    public void setCountRows(String countRows) {
        this.countRows = countRows;
    }
}
