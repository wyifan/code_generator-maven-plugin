package ${templateConfig.packageInfo};

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
* @Description:统一API响应实体
* @Author: Code Generator By Shawn Wang
* @Date: ${.now?string["yyyy-MM-dd HH:mm:ss"]}
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 忽略空值
@Schema(description = "统一API响应实体")
public class ApiResponse<T> implements Serializable {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "消息", example = "操作成功")
    private String message;

    @Schema(description = "返回数据")
    private T data;

    @Schema(description = "时间戳")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
    * 成功响应
    * @param data 响应数据
    * @param <T>  数据类型
    * @return 响应实体
    */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data, LocalDateTime.now());
    }

    /**
    * 失败响应
    * @param code    状态码
    * @param message 消息
    * @param <T>     数据类型
    * @return 响应实体
    */
    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now();
    }
}