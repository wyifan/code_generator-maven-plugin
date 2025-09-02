package ${templateConfig.packageInfo};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * @Description: ${entityName}修改DTO
 * @Author: Code Generator By Shawn Wang
 * @Date: ${.now?string["yyyy-MM-dd HH:mm:ss"]}
 */
@Data
@Accessors(chain = true)
@Schema(description = "${entityName}修改DTO")
public class ${entityName}UpdateDto {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

<#list tableConfig.columns as column>
    /**
     * ${column.comment}
     */
    @Schema(description = "${column.comment}")
    private ${column.javaType} ${column.javaName};
</#list>

    /**
     * 版本号（乐观锁）
     */
    @Schema(description = "版本号（乐观锁）")
    private Integer version;
}
