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
package com.github.x19990416.mxpaas.tool.generator.utils;

import com.google.common.collect.Maps;

import java.util.Map;

public class ConvertUtil {
  private static Map<String, String> mysqlConvertor = Maps.newHashMap();

  public ConvertUtil() {
    if (mysqlConvertor.isEmpty()) {
      mysqlConvertor.put("tinyint", "Integer");
      mysqlConvertor.put("smallint", "Integer");
      mysqlConvertor.put("mediumint", "Integer");
      mysqlConvertor.put("int", "Integer");
      mysqlConvertor.put("integer", "Integer");

      mysqlConvertor.put("bigint", "Long");

      mysqlConvertor.put("float", "Float");

      mysqlConvertor.put("double", "Double");

      mysqlConvertor.put("decimal", " BigDecimal");

      mysqlConvertor.put("bit", "Boolean");

      mysqlConvertor.put("char", "String");
      mysqlConvertor.put("varchar", "String");
      mysqlConvertor.put("tinytext", "String");
      mysqlConvertor.put("text", "String");
      mysqlConvertor.put("mediumtext", "String");
      mysqlConvertor.put("longtext", "String");

      mysqlConvertor.put("date", "Timestamp");
      mysqlConvertor.put("datetime", "Timestamp");
      mysqlConvertor.put("timestamp", "Timestamp");
    }
  }

  public String mysqlToJava(String mysqlType) {
    return mysqlConvertor.get(mysqlType);
  }
}
