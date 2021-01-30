/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.service;

import com.github.x19990416.mxpaas.admin.modules.system.domain.Dict;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.DictDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.DictQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DictService {

  Map<String, Object> queryAll(DictQueryCriteria criteria, Pageable pageable);


  List<DictDto> queryAll(DictQueryCriteria dict);


  void create(Dict resources);


  void update(Dict resources);


  void delete(Set<Long> ids);

  void download(List<DictDto> queryAll, HttpServletResponse response) throws IOException;
}
