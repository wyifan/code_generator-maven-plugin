package ${templateConfig.packageInfo};

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: 基础实体类，包含通用字段和自动填充逻辑
 * @Author: Code Generator By Shawn Wang
 * @Date: ${.now?string["yyyy-MM-dd HH:mm:ss"]}
 */
@Data
@Accessors(chain = true)
public abstract class BaseEntity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 版本号（乐观锁）
     */
    @TableField(value = "version", fill = FieldFill.INSERT)
    private Integer version;

    /**
     * 逻辑删除（0-未删除，1-已删除）
     */
    @TableField(value = "deleted")
    @JsonIgnore
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建人姓名
     */
    @TableField(value = "created_by_name", fill = FieldFill.INSERT)
    private String createdByName;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 更新人ID
     */
    @TableField(value = "updated_by", fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新人姓名
     */
    @TableField(value = "updated_by_name", fill = FieldFill.UPDATE)
    private String updatedByName;

    /**
     * 自动填充处理器
     */
    @Component
    public static class MyMetaObjectHandler implements MetaObjectHandler {
        @Override
        public void insertFill(MetaObject metaObject) {
            // 新增时自动填充
            this.setFieldValByName("version", 0, metaObject);
            this.setFieldValByName("deleted", 0, metaObject);
            this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
            // TODO: 从安全上下文获取当前用户ID并填充
            this.setFieldValByName("createdBy", 1L, metaObject); // 假设当前用户ID为1
            // TODO: createdByName 通常需要通过createdBy查询用户服务获取，此处不自动填充，待集成安全框架后处理
            // this.setFieldValByName("createdByName", "Admin", metaObject);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            // 更新时自动填充
            this.setFieldValByName("updatedTime", LocalDateTime.now(), metaObject);
            // TODO: 从安全上下文获取当前用户ID并填充
            this.setFieldValByName("updatedBy", 1L, metaObject); // 假设当前用户ID为1
            // TODO: updatedByName 通常需要通过updatedBy查询用户服务获取，此处不自动填充，待集成安全框架后处理
            // this.setFieldValByName("updatedByName", "Admin", metaObject);
        }
    }
}
