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
package com.github.x19990416.mxpaas.tool.generator.service;

import com.github.x19990416.mxpaas.tool.generator.domain.ColumnInfo;
import com.github.x19990416.mxpaas.tool.generator.domain.GenConfig;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface GeneratorService {
  Object getTables(String name, int[] startEnd);

  List<ColumnInfo> getColumns(String name);

  void sync(List<ColumnInfo> columnInfos, List<ColumnInfo> columnInfoList);

  void save(List<ColumnInfo> columnInfos);

  Object getTables();

  void generator(GenConfig genConfig, List<ColumnInfo> columns);

  ResponseEntity<Object> preview(GenConfig genConfig, List<ColumnInfo> columns) throws Exception;

  void download(
      GenConfig genConfig,
      List<ColumnInfo> columns,
      HttpServletRequest request,
      HttpServletResponse response);

  List<ColumnInfo> query(String table);
}
