package ${templateConfig.packageInfo};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * @Description: ${entityName}返回视图对象VO
 * @Author: Code Generator By Shawn Wang
 * @Date: ${.now?string["yyyy-MM-dd HH:mm:ss"]}
 */
@Data
@Accessors(chain = true)
@Schema(description = "${entityName}返回视图对象VO")
public class ${entityName}Vo {

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
     * 版本号
     */
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
