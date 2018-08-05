package servlet;

public class ServerResponse {

    private String success;
    private String errorMessage;
    private String data;

    public ServerResponse(String success, String errorMessage, String data) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public void setData(String success) {
        this.data = data;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getData() {
        return data;
    }
}
