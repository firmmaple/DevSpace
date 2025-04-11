package org.jeffrey.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.jeffrey.service.user.repository.entity.UserDO;

import java.util.List;

/*
 * insert, selectById, selectBatchIds, selectByMap, selectOne,
 * selectList, selectPage, updateById, update, deleteById, deleteBatchIds, deleteByMap
 */
public interface UserMapper extends BaseMapper<UserDO> {
    @Select("SELECT * FROM user")
    List<UserDO> findAll();

    @Select("SELECT * FROM user WHERE username = #{username}")
    List<UserDO> findByUsername(String username);
}
