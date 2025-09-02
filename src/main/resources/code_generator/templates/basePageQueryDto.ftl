package ${templateConfig.packageInfo};

import lombok.Data;
import javax.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "通用分页查询实体")
public class BasePageQueryDto {

    @Schema(description = "当前页码", defaultValue = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    @Min(value = 1, message = "每页大小不能小于1")
    private Integer pageSize = 10;
}