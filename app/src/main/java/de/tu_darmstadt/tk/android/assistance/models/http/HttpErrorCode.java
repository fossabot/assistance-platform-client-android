package de.tu_darmstadt.tk.android.assistance.models.http;

import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 * HTTP error codes
 * More info: https://github.com/Telecooperation/server_platform_assistance/wiki/API#client-erorrs
 * <p/>
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class HttpErrorCode {

    private static final Map<Integer, ErrorCode> CODE_MAP = new ArrayMap<>();

    static {
        for (ErrorCode type : ErrorCode.values()) {
            CODE_MAP.put(type.getCode(), type);
        }
    }

    public static ErrorCode fromCode(int code) {
        return CODE_MAP.get(code);
    }

    public enum ErrorCode {
        LOGIN_NO_VALID(2),
        EMAIL_ALREADY_EXISTS(3),
        WRONG_PARAMETER_LIST(4),
        WRONG_MODULE_REQUIREMENTS(5),
        MODULE_ALREADY_EXISTS(6);

        private int code;

        ErrorCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private HttpErrorCode() {
    }
}
