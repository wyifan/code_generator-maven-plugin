package ${templateConfig.packageInfo};

import com.example.project.dto.ApiResponse;
import com.example.project.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

/**
* 全局异常处理器
*/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
    * 捕获自定义业务异常
    */
    @ExceptionHandler(value = BizException.class)
    public ApiResponse handleBizException(BizException e) {
        log.error("业务异常: {}", e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    /**
    * 捕获参数校验异常
    */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResponse handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.joining(", "));
        log.error("参数校验失败: {}", errorMessage);
        return ApiResponse.fail(400, errorMessage);
    }

    /**
    * 捕获所有未处理的异常
    */
    @ExceptionHandler(value = Exception.class)
    public ApiResponse handleException(Exception e) {
        log.error("系统异常:", e);
        return ApiResponse.fail(500, "服务器错误，请联系管理员");
    }
}
