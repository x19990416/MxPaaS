/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.repository;

import com.github.x19990416.mxpaas.admin.modules.system.domain.Dict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;

public interface DictRepository extends JpaRepository<Dict, Long>, JpaSpecificationExecutor<Dict> {

  void deleteByIdIn(Set<Long> ids);

  List<Dict> findByIdIn(Set<Long> ids);
}
