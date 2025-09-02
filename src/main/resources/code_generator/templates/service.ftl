package ${templateConfig.packageInfo};

import com.baomidou.mybatisplus.extension.service.IService;
import ${dtoPackage}.${entityName}AddDto;
import ${dtoPackage}.${entityName}QueryDto;
import ${dtoPackage}.${entityName}UpdateDto;
import ${voPackage}.${entityName}Vo;
import ${entityPackage}.${entityName};

import java.util.List;

/**
 * @Description: ${entityName}Service接口
 * @Author: Code Generator By Shawn Wang
 * @Date: ${.now?string["yyyy-MM-dd HH:mm:ss"]}
 */
public interface I${entityName}Service extends IService<${entityName}> {

    /**
     * 新增${entityName}
     * @param ${entityName?uncap_first}Dto 新增DTO
     */
    void add${entityName}(${entityName}AddDto ${entityName?uncap_first}Dto);

    /**
     * 修改${entityName}
     * @param ${entityName?uncap_first}Dto 修改DTO
     */
    void update${entityName}(${entityName}UpdateDto ${entityName?uncap_first}Dto);

    /**
     * 删除${entityName}
     * @param id 主键ID
     */
    void delete${entityName}(Long id);

    /**
     * 根据ID获取${entityName}详情
     * @param id 主键ID
     * @return ${entityName}VO
     */
    ${entityName}Vo get${entityName}ById(Long id);

    /**
     * 查询${entityName}列表
     * @param queryDto 查询DTO
     * @return ${entityName}VO列表
     */
    List<${entityName}Vo> list${entityName}s(${entityName}QueryDto queryDto);

    /**
     * 修改${entityName}，不使用乐观锁
     * @param ${entityName?uncap_first}Dto 修改DTO
     */
    void update${entityName}NoVersion(${entityName}UpdateDto ${entityName?uncap_first}Dto);
}
