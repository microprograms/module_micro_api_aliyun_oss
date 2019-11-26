package module_micro_api_aliyun_oss.api;

import com.github.microprograms.micro_api_runtime.model.ResponseCode;

public enum ErrorCodeEnum implements ResponseCode {

    /**状态异常*/
    invalid_status("invalid_status", "状态异常");

    private ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private final String code;

    public String getCode() {
        return code;
    }

    private final String message;

    public String getMessage() {
        return message;
    }
}
