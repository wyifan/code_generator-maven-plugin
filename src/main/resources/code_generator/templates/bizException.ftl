package ${templateConfig.packageInfo};

/**
* 自定义业务异常
*/
public class BizException extends RuntimeException {

    private Integer code;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}