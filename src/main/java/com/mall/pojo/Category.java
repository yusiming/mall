package com.mall.pojo;

import lombok.*;

import java.util.Date;

/**
 * 注意：Category需要放在set中，所以需要重写equals和hashcode方法
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Category {
    private Integer id;

    private Integer parentId;

    private String name;

    private Boolean status;

    private Integer sortOrder;

    private Date createTime;

    private Date updateTime;
}