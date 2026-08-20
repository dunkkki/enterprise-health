package com.enterprise.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.health.entity.UserRole;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Select("SELECT role_id FROM user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(Long userId);
}
