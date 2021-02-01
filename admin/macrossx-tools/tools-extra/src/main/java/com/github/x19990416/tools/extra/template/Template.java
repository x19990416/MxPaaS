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

import java.io.*;
import java.util.Map;

public interface Template {
  public void render(Map<?, ?> toRend, Writer writer) throws Exception;

  public void render(Map<?, ?> toRend, OutputStream outputStream) throws Exception;

  public default void render(Map<?, ?> bindingMap, File file) throws Exception {
    if(!file.exists()){
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    this.render(bindingMap, new BufferedOutputStream(new FileOutputStream(file)));
  }

  public default String render(Map<?, ?> toRend) throws Exception {
    StringWriter writer = new StringWriter();
    this.render(toRend, writer);
    return writer.toString();
  }
}
