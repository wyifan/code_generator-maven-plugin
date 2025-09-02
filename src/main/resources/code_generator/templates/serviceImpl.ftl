package ${templateConfig.packageInfo};

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ${dtoPackage}.${entityName}AddDto;
import ${dtoPackage}.${entityName}QueryDto;
import ${dtoPackage}.${entityName}UpdateDto;
import ${voPackage}.${entityName}Vo;
import ${entityPackage}.${entityName};
import ${mapperPackage}.${entityName}Mapper;
import ${servicePackage}.I${entityName}Service;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: ${entityName}Service实现类
 * @Author: Code Generator By Shawn Wang
 * @Date: ${.now?string["yyyy-MM-dd HH:mm:ss"]}
 */
@Service
public class ${entityName}ServiceImpl extends ServiceImpl<${entityName}Mapper, ${entityName}> implements I${entityName}Service {

    @Override
    public void add${entityName}(${entityName}AddDto ${entityName?uncap_first}Dto) {
        ${entityName} ${entityName?uncap_first} = new ${entityName}();
        BeanUtils.copyProperties(${entityName?uncap_first}Dto, ${entityName?uncap_first});
        this.save(${entityName?uncap_first});
    }

    @Override
    public void update${entityName}(${entityName}UpdateDto ${entityName?uncap_first}Dto) {
        ${entityName} ${entityName?uncap_first} = new ${entityName}();
        BeanUtils.copyProperties(${entityName?uncap_first}Dto, ${entityName?uncap_first});
        this.updateById(${entityName?uncap_first});
    }

    @Override
    public void delete${entityName}(Long id) {
        this.removeById(id);
    }

    @Override
    public ${entityName}Vo get${entityName}ById(Long id) {
        ${entityName} ${entityName?uncap_first} = this.getById(id);
        if (${entityName?uncap_first} == null) {
            return null;
        }
        ${entityName}Vo ${entityName?uncap_first}Vo = new ${entityName}Vo();
        BeanUtils.copyProperties(${entityName?uncap_first}, ${entityName?uncap_first}Vo);
        return ${entityName?uncap_first}Vo;
    }

    @Override
    public List<${entityName}Vo> list${entityName}s(${entityName}QueryDto queryDto) {
        LambdaQueryWrapper<${entityName}> queryWrapper = new LambdaQueryWrapper<>();
<#list tableConfig.columns as column>
    <#if column.javaType == "String">
        if (StringUtils.hasText(queryDto.get${column.javaName?cap_first}())) {
            queryWrapper.like(${entityName}::get${column.javaName?cap_first}, queryDto.get${column.javaName?cap_first}());
        }
    <#else>
        if (queryDto.get${column.javaName?cap_first}() != null) {
            queryWrapper.eq(${entityName}::get${column.javaName?cap_first}, queryDto.get${column.javaName?cap_first}());
        }
    </#if>
</#list>

        List<${entityName}> ${entityName?uncap_first}List = this.list(queryWrapper);
        return ${entityName?uncap_first}List.stream()
                .map(${entityName?uncap_first} -> {
                    ${entityName}Vo ${entityName?uncap_first}Vo = new ${entityName}Vo();
                    BeanUtils.copyProperties(${entityName?uncap_first}, ${entityName?uncap_first}Vo);
                    return ${entityName?uncap_first}Vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void update${entityName}NoVersion(${entityName}UpdateDto ${entityName?uncap_first}Dto) {
        LambdaUpdateWrapper<${entityName}> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(${entityName}::getId, ${entityName?uncap_first}Dto.getId());

<#list tableConfig.columns as column>
    <#if column.javaName != "id" && column.javaName != "version">
        updateWrapper.set(${entityName}::get${column.javaName?cap_first}, ${entityName?uncap_first}Dto.get${column.javaName?cap_first}());
    </#if>
</#list>
        
        this.update(updateWrapper);
    }
}
