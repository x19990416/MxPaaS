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
package com.github.x19990416.mxpaas.admin.common.utils;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class StringUtil {
  private static final String UNKNOWN = "unknown";
  private static boolean ipLocal = false;
  private static DbConfig config;
  private static File file = null;

  static {
    SpringContextHolder.addCallBacks(
        () -> {
          StringUtil.ipLocal =
              SpringContextHolder.getProperties("ip.local-parsing", false, Boolean.class);
          if (ipLocal) {
            /*
             * 此文件为独享 ，不必关闭
             */
            String path = "ip2region/ip2region.db";
            String name = "ip2region.db";
            try {
              config = new DbConfig();
              file = FileUtil.inputStreamToFile(new ClassPathResource(path).getInputStream(), name);
            } catch (Exception e) {
              log.error(e.getMessage(), e);
            }
          }
        });
  }

  public static String getIp(HttpServletRequest request) {
    String ip = request.getHeader("x-forwarded-for");
    if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    String comma = ",";
    String localhost = "127.0.0.1";
    if (ip.contains(comma)) {
      ip = ip.split(",")[0];
    }
    if (localhost.equals(ip)) {
      // 获取本机真正的ip地址
      try {
        ip = InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e) {
        log.error(e.getMessage(), e);
      }
    }
    return ip;
  }

  public static String getBrowser(HttpServletRequest request) {
    UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
    Browser browser = userAgent.getBrowser();
    return browser.getName();
  }

  public static String getCityInfo(String ip) {
    if (ipLocal) {
      return getLocalCityInfo(ip);
    } else {
      return getHttpCityInfo(ip);
    }
  }
  /** 根据ip获取详细地址 */
  public static String getHttpCityInfo(String ip) {
    String api = String.format(SysAdminConstant.Url.IP_URL, ip);
    // JSONObject object = JSONObject.parse(HttpRequest.get(api).timeout(1000l).execute().body());
    return "还未实现，需要封装 http client";
  }

  /** 根据ip获取详细地址 */
  public static String getLocalCityInfo(String ip) {
    try {
      DataBlock dataBlock = new DbSearcher(config, file.getPath()).binarySearch(ip);
      String region = dataBlock.getRegion();
      String address = region.replace("0|", "");
      char symbol = '|';
      if (address.charAt(address.length() - 1) == symbol) {
        address = address.substring(0, address.length() - 1);
      }
      return address.equals(SysAdminConstant.REGION) ? "内网IP" : address;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return "";
  }
}
