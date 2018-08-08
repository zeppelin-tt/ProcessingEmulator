package servlet;

import static utils.DataTimeUtils.formatDateTime;

public class TableFields {
    private String id;
    private String accNum;
    private String initials;
    private String balance;
    private String action;
    private String lastOpTime;
    private String createTime;


    public TableFields(String id, String accNum, String initials, String balance, String action, String lastOpTime, String createTime) {
        this.id = id;
        this.accNum = accNum;
        this.initials = initials;
        this.balance = balance;
        this.action = action;
        this.lastOpTime = formatDateTime(lastOpTime, "yyyy-MM-dd HH:mm:ss.SSSSSS", "yyyy.MM.dd hh:mm");
        this.createTime = formatDateTime(createTime, "yyyy-MM-dd HH:mm:ss.SSSSSS", "yyyy.MM.dd hh:mm");
    }

    public String getId() {
        return id;
    }

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

}
