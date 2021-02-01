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
package com.github.x19990416.tools.extra.template;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;

@Data
@RequiredArgsConstructor
public class TemplateConfig {
  private final String path;
  private final Charset charset;
  private final ResourceMode resourceMode;
  private Class<? extends TemplateEngine> engine;


  public static enum ResourceMode {
    CLASSPATH,
    FILE,
    WEB_ROOT,
    STRING,
    COMPOSITE
  }
}
