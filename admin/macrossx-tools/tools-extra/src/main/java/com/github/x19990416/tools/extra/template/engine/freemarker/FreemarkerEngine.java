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

import com.github.x19990416.tools.extra.template.Template;
import com.github.x19990416.tools.extra.template.TemplateConfig;
import com.github.x19990416.tools.extra.template.TemplateEngine;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FreemarkerEngine implements TemplateEngine {
  private Configuration cfg;

  @Override
  public TemplateEngine init(TemplateConfig config) {
    try {
      if (config == null) {
        config =
            new TemplateConfig(null, StandardCharsets.UTF_8, TemplateConfig.ResourceMode.STRING);
      }
      createCfg(config);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }

  @Override
  public Template getInstance(String templateName) throws IOException {
    return new FreemarkerTemplate(cfg.getTemplate(templateName));
  }

  private void createCfg(TemplateConfig config) throws IOException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
    cfg.setLocalizedLookup(false);
    cfg.setDefaultEncoding(config.getCharset().displayName());
    switch (config.getResourceMode()) {
      case FILE:
        {
          cfg.setTemplateLoader(new FileTemplateLoader(new File(config.getPath())));
          break;
        }
      case CLASSPATH:
        {
          cfg.setTemplateLoader(new ClassTemplateLoader( Thread.currentThread().getContextClassLoader(), config.getPath()));
          break;
        }
    }
    this.cfg = cfg;
  }
}
