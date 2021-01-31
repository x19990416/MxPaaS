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
package com.github.x19990416.mxpaas.admin.common.config;

import com.fasterxml.classmate.TypeResolver;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Pageable;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRuleConvention;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

@Configuration
@EnableOpenApi
public class SwaggerConfig {
  @Value("${jwt.header}")
  private String tokenHeader;

  @Value("${jwt.token-start-with}")
  private String tokenStartWith;

  @Value("${swagger.enabled}")
  private Boolean enabled;

  @Bean
  @SuppressWarnings("all")
  public Docket createRestApi() {
    RequestParameterBuilder ticketPar = new RequestParameterBuilder();
    List<RequestParameter> pars = new ArrayList<>();
    ticketPar.name(tokenHeader).description("token").in("header").required(true).build();
    pars.add(ticketPar.build());

    return new Docket(DocumentationType.OAS_30)
        .enable(enabled)
        .apiInfo(apiInfo())
        .select()
        .paths(PathSelectors.regex("/error.*").negate())
        .build()
        .globalRequestParameters(pars);
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .description("一个简单且易上手的 Spring boot 后台管理框架")
        .title("PaaS 接口文档")
        .version("2.4.2")
        .build();
  }
}

@Configuration
@SuppressWarnings("all")
class SwaggerDataConfig {

  @Bean
  public AlternateTypeRuleConvention pageableConvention(final TypeResolver resolver) {
    return new AlternateTypeRuleConvention() {
      @Override
      public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
      }

      @Override
      public List<AlternateTypeRule> rules() {
        return newArrayList(
            newRule(resolver.resolve(Pageable.class), resolver.resolve(Page.class)));
      }
    };
  }

  @ApiModel
  @Data
  private static class Page {
    @Schema(name = "页码 (0..N)")
    private Integer page;

    @Schema(name = "每页显示的数目")
    private Integer size;

    @Schema(name = "以下列格式排序标准：property[,asc | desc]。 默认排序顺序为升序。 支持多种排序条件：如：id,asc")
    private List<String> sort;
  }
}
