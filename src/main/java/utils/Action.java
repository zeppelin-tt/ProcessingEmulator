package utils;

public enum Action {
    CREATE("create"),
    CLOSE("close"),
    BLOCK("block"),
    TRANSFER_MINUS("transfer_minus"),
    TRANSFER_PLUS("transfer_plus"),
    TRANSFER_TO("transfer_to");

    private String action;

    Action(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
