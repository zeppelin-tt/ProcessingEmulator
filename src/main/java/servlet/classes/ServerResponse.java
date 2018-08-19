package servlet.classes;

import servlet.classes.ResponseData;

public class ServerResponse {

    private String success;
    private String errorMessage;
    private ResponseData data;

    public ServerResponse(String success, String errorMessage, ResponseData data) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public void setData(ResponseData data) {
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

    public ResponseData getData() {
        return data;
    }
}
