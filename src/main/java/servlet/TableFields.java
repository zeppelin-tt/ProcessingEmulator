package servlet;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TableFields {
    private String id;
    private String accNum;
    private String initials;
    private String balance;
    private String action;
    private String lastOpTime;
    private String createTime;


    public TableFields(String id, String accNum, String initials, String balance, String action, Date lastOpTime, Date createTime) {
        this.id = id;
        this.accNum = accNum;
        this.initials = initials;
        this.balance = balance;
        this.action = action;
        this.lastOpTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(lastOpTime);
        this.createTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(createTime);
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

    @Override
    public String toString() {
        return "{" + " id: " + id + ", accNum: " +
                accNum + ", initials: " + initials +
                ", balance: " + balance + ", action: " +
                action + ", lastOpTime: " + lastOpTime +
                ", createTime: " + createTime + " }";
    }
}
