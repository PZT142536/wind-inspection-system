package com.shanghaiwind.admin.dto;

import lombok.Data;

/**
 * 用户查询参数
 */
@Data
public class UserQuery {

    private String keyword;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
