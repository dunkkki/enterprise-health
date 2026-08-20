package com.enterprise.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.health.entity.RoleMenu;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    @Select("<script>SELECT DISTINCT menu_id FROM role_menu WHERE role_id IN "
            + "<foreach collection='list' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Long> selectMenuIdsByRoleIds(List<Long> roleIds);
}
