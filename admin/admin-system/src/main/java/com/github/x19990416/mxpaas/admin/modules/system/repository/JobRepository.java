/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.repository;

import com.github.x19990416.mxpaas.admin.modules.system.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Set;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
  Job findByName(String name);

  void deleteAllByIdIn(Set<Long> ids);
}
