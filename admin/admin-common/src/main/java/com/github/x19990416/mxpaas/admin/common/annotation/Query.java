/*
 *  Copyright (c) 2020-2021 Guo Limin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.github.x19990416.mxpaas.admin.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

  String propName() default "";

  Type type() default Type.EQUAL;

  /** 连接查询的属性名 */
  String joinName() default "";

  /** 默认左连接 */
  Join join() default Join.LEFT;

  /** 多字段模糊搜索，多个用逗号隔开, 如@Query(blurry = "email,username") */
  String blurry() default "";

  enum Type {
    EQUAL,
    GREATER_THAN,
    LESS_THAN,
    INNER_LIKE,
    LEFT_LIKE,
    RIGHT_LIKE,
    LESS_THAN_NQ,
    IN,
    NOT_EQUAL,
    BETWEEN,
    NOT_NULL,
    IS_NULL
  }

  /** 适用于简单连接查询，复杂的请自定义该注解，或者使用sql查询 */
  enum Join {
    LEFT,
    RIGHT,
    INNER
  }
}
