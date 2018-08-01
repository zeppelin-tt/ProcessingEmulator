package servlet;

public class ServerResponse {

    private String success;
    private String message;

    public ServerResponse(String success, String message) {
        this.success = success;
        this.message = message;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
