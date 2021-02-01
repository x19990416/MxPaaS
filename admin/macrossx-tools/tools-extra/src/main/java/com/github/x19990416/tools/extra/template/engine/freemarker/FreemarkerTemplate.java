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
package com.github.x19990416.tools.extra.template.engine.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

public class FreemarkerTemplate implements com.github.x19990416.tools.extra.template.Template {
  private Template freemarkerTemplate;

  public FreemarkerTemplate(Template freemarkerTemplate) {
    this.freemarkerTemplate = freemarkerTemplate;
  }

  @Override
  public void render(Map<?, ?> toRend, Writer writer) throws IOException, TemplateException {
    this.freemarkerTemplate.process(toRend, writer);
  }
  @Override
  public void render(Map<?, ?> toRend, OutputStream outputStream)
      throws IOException, TemplateException {
    this.render(toRend, new OutputStreamWriter(outputStream));
  }
}
