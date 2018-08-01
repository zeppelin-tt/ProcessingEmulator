package servlet;

public class Actions {

    private String type;
    private String lastName;
    private String firstName;
    private String patronymic;
    private String accNum;
    private String secondAccNum;
    private String money;

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public void setAccNum(String accNum) {
        this.accNum = accNum;
    }

    public void setSecondAccNum(String secondAccNum) {
        this.secondAccNum = secondAccNum;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getLastName() {

        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public String getAccNum() {
        return accNum;
    }

    public String getSecondAccNum() {
        return secondAccNum;
    }

    public String getMoney() {
        return money;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
