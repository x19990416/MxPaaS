/**
 * create by Guo Limin on 2021/1/30.
 */
package com.github.x19990416.mxpaas.admin.modules.system.repository;

import com.github.x19990416.mxpaas.admin.modules.system.domain.DictDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DictDetailRepository extends JpaRepository<DictDetail, Long>, JpaSpecificationExecutor<DictDetail> {

    List<DictDetail> findByDictName(String name);
}