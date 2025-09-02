package ${templateConfig.packageInfo};

import com.baomidou.mybatisplus.annotation.TableName;
import ${basePackage}.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;


/**
* @Description: ${entityName} 实体类
* @Author: Code Generator By Shawn Wang project
* @Date: ${.now?string("yyyy-MM-dd HH:mm:ss")}
*/
@Data
<#if useBaseEntity>
@EqualsAndHashCode(callSuper = true)
<#else>
@EqualsAndHashCode(callSuper = false)
</#if>
@Accessors(chain = true)
@TableName("${tableConfig.tableName}")
public class ${tableConfig.entityName}<#if tableConfig.useBaseEntity> extends BaseEntity</#if> implements Serializable {

private static final long serialVersionUID = 1L;

<#list tableConfig.columns as column>
    /**
    * ${column.comment}
    */
    private ${column.javaType} ${column.javaName};
</#list>
}