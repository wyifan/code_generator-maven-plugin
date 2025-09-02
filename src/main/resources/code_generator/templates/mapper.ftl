package ${templateConfig.packageInfo};

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ${entityPackage}.${entityName};
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: ${entityName}Mapper接口
 * @Author: Code Generator By Shawn Wang
 * @Date: ${.now?string["yyyy-MM-dd HH:mm:ss"]}
 */
@Mapper
public interface ${entityName}Mapper extends BaseMapper<${entityName}> {
}
