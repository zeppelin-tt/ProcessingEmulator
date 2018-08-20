package servlet.classes;

public class FilteredRequest {
    private String numPage;

    private String limitRows;
    private String accNum;
    private String initials;
    private String balance;
    private String action;
    private String lastOpTime;
    private String createTime;
    private String hideClosed;

    public String getNumPage() {
        return numPage;
    }

    public String getLimitRows() { return limitRows; }

    public String getAccNum() {
        return accNum;
    }

    public String getInitials() {
        return initials;
    }

    public String getBalance() {
        return balance;
    }

    public String getAction() {
        return action;
    }

    public String getLastOpTime() {
        return lastOpTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getHideClosed() {
        return hideClosed;
    }
}
