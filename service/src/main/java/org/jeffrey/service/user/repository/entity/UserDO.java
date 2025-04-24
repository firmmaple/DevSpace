package org.jeffrey.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class UserDO {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "username")
    private String username;
    @TableField(value = "password")
    private String password;
    @TableField(value = "is_admin")
    private Boolean isAdmin;
    @TableField(value = "email")
    private String email;
    @TableField(value = "avatar_url")
    private String avatarUrl;
    @TableField(value = "bio")
    private String bio;
}
