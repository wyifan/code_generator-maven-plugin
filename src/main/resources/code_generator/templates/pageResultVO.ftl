package ${templateConfig.packageInfo};

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
* 通用分页结果返回实体
*/
@Data
@NoArgsConstructor
@Schema(description = "通用分页结果返回实体")
public class PageResultVO<T> implements Serializable {

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "当前页码")
    private Long pageNum;

    @Schema(description = "每页大小")
    private Long pageSize;

    @Schema(description = "总页数")
    private Long pages;

    @Schema(description = "记录列表")
    private List<T> records = Collections.emptyList();

    public PageResultVO(Page<?> page) {
        this.total = page.getTotal();
        this.pageNum = page.getCurrent();
        this.pageSize = page.getSize();
        this.pages = page.getPages();
    }
}