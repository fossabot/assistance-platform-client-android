package de.tudarmstadt.informatik.tk.android.assistance.models.api.error;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Error class for JSON response
 *
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class ErrorResponse {

    /**
     * Error pattern
     *
     *   HTTP/1.1 400 Bad Request
     *   Content-Length: 39
     *
     *   {
     *      "code" : integer,
     *      "message" : string
     *   }
     */

    private int statusCode;

    @SerializedName("code")
    @Expose
    private Integer code;

    @SerializedName("message")
    @Expose
    private String message;

    public ErrorResponse() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "statusCode=" + statusCode +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
