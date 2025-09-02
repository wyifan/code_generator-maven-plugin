package ${templateConfig.packageInfo};

import ${basePackage}.ApiResponse;
import ${dtoPackage}.${entityName}AddDto;
import ${dtoPackage}.${entityName}QueryDto;
import ${dtoPackage}.${entityName}UpdateDto;
import ${voPackage}.${entityName}Vo;
import ${servicePackage}.I${entityName}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: ${entityName}管理API
 * @Author: Code Generator By Shawn Wang
 * @Date: ${.now?string["yyyy-MM-dd HH:mm:ss"]}
 */
@RestController
@RequestMapping("/api/${tableName?uncap_first}s") <#-- Assuming endpoint is plural form of table name -->
@Tag(name = "${entityName}管理API", description = "${entityName}增删改查接口")
@RequiredArgsConstructor
public class ${entityName}Controller {

    private final I${entityName}Service ${entityName?uncap_first}Service;

    @PostMapping
    @Operation(summary = "新增${entityName}")
    public ApiResponse<Void> add${entityName}(@RequestBody ${entityName}AddDto ${entityName?uncap_first}AddDto) {
        ${entityName?uncap_first}Service.add${entityName}(${entityName?uncap_first}AddDto);
        return ApiResponse.success(null);
    }

    @PutMapping
    @Operation(summary = "修改${entityName}")
    public ApiResponse<Void> update${entityName}(@RequestBody ${entityName}UpdateDto ${entityName?uncap_first}UpdateDto) {
        ${entityName?uncap_first}Service.update${entityName}(${entityName?uncap_first}UpdateDto);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除${entityName}")
    public ApiResponse<Void> delete${entityName}(@Parameter(description = "主键ID") @PathVariable Long id) {
        ${entityName?uncap_first}Service.delete${entityName}(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询${entityName}")
    public ApiResponse<${entityName}Vo> get${entityName}ById(@Parameter(description = "主键ID") @PathVariable Long id) {
        ${entityName}Vo ${entityName?uncap_first}Vo = ${entityName?uncap_first}Service.get${entityName}ById(id);
        return ApiResponse.success(${entityName?uncap_first}Vo);
    }

    @GetMapping
    @Operation(summary = "查询${entityName}列表")
    public ApiResponse<List<${entityName}Vo>> list${entityName}s(${entityName}QueryDto queryDto) {
        List<${entityName}Vo> ${entityName?uncap_first}Vos = ${entityName?uncap_first}Service.list${entityName}s(queryDto);
        return ApiResponse.success(${entityName?uncap_first}Vos);
    }

    @PutMapping("/no-version")
    @Operation(summary = "修改${entityName}（不使用乐观锁）",
              description = "该接口不校验版本号，直接更新${entityName}数据")
    @ApiResponse(responseCode = "200", description = "更新成功",
                 content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    public ApiResponse<Void> update${entityName}NoVersion(@RequestBody ${entityName}UpdateDto ${entityName?uncap_first}UpdateDto) {
        ${entityName?uncap_first}Service.update${entityName}NoVersion(${entityName?uncap_first}UpdateDto);
        return ApiResponse.success(null);
    }
}
