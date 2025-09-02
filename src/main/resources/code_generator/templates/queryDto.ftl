package ${templateConfig.packageInfo};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * @Description: ${entityName}查询DTO
 * @Author: Code Generator By Shawn Wang
 * @Date: ${.now?string["yyyy-MM-dd HH:mm:ss"]}
 */
@Data
@Accessors(chain = true)
@Schema(description = "${entityName}查询DTO")
public class ${entityName}QueryDto  extends BasePageQueryDTO {

<#list tableConfig.columns as column>
    /**
     * ${column.comment}
     */
    @Schema(description = "${column.comment}"<#-- You can add example here, e.g., , example = "示例值" -->)
    private ${column.javaType} ${column.javaName};
</#list>
}
