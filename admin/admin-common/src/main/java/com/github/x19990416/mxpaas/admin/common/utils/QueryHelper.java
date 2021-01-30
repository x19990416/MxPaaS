/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.common.utils;

import com.github.x19990416.mxpaas.admin.common.annotation.DataPermission;
import com.github.x19990416.mxpaas.admin.common.annotation.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
public class QueryHelper {
  public static <R, Q> Predicate getPredicate(Root<R> root, Q query, CriteriaBuilder cb) {
    List<Predicate> list = new ArrayList<>();
    if (query == null) {
      return cb.and(list.toArray(new Predicate[0]));
    }
    // 数据权限验证
    DataPermission permission = query.getClass().getAnnotation(DataPermission.class);
    if (permission != null) {
      // 获取数据权限
      List<Long> dataScopes = SecurityUtils.getCurrentUserDataScope();
      if (!CollectionUtils.isEmpty(dataScopes)) {
        if (Strings.isNotBlank(permission.joinName())
            && Strings.isNotBlank(permission.fieldName())) {
          Join join = root.join(permission.joinName(), JoinType.LEFT);
          list.add(getExpression(permission.fieldName(), join, root).in(dataScopes));
        } else if (Strings.isBlank(permission.joinName())
            && Strings.isNotBlank(permission.fieldName())) {
          list.add(getExpression(permission.fieldName(), null, root).in(dataScopes));
        }
      }
    }
    try {
      List<Field> fields = getAllFields(query.getClass(), new ArrayList<>());
      for (Field field : fields) {
        boolean accessible = field.trySetAccessible();
        if (accessible == false)
          // 设置对象的访问权限，保证对private的属性的访
          field.setAccessible(true);
        Query q = field.getAnnotation(Query.class);
        if (q != null) {
          String propName = q.propName();
          String joinName = q.joinName();
          String blurry = q.blurry();
          String attributeName = isBlank(propName) ? field.getName() : propName;
          Class<?> fieldType = field.getType();
          Object val = field.get(query);
          if (val == null || "".equals(val)) {
            continue;
          }
          Join join = null;
          // 模糊多字段
          if (Strings.isNotEmpty(blurry)) {
            List<Predicate> orPredicate = new ArrayList<>();
            for (String s : blurry.split(",")) {
              orPredicate.add(cb.like(root.get(s).as(String.class), "%" + val.toString() + "%"));
            }
            Predicate[] p = new Predicate[orPredicate.size()];
            list.add(cb.or(orPredicate.toArray(p)));
            continue;
          }
          if (Strings.isNotEmpty(joinName)) {
            String[] joinNames = joinName.split(">");
            for (String name : joinNames) {
              switch (q.join()) {
                case LEFT:
                  if (null != join && null != val) {
                    join = join.join(name, JoinType.LEFT);
                  } else {
                    join = root.join(name, JoinType.LEFT);
                  }
                  break;
                case RIGHT:
                  if (null != join && null != val) {
                    join = join.join(name, JoinType.RIGHT);
                  } else {
                    join = root.join(name, JoinType.RIGHT);
                  }
                  break;
                case INNER:
                  if (null != join && null != val) {
                    join = join.join(name, JoinType.INNER);
                  } else {
                    join = root.join(name, JoinType.INNER);
                  }
                  break;
                default:
                  break;
              }
            }
          }
          switch (q.type()) {
            case EQUAL:
              list.add(
                  cb.equal(
                      getExpression(attributeName, join, root)
                          .as((Class<? extends Comparable>) fieldType),
                      val));
              break;
            case GREATER_THAN:
              list.add(
                  cb.greaterThanOrEqualTo(
                      getExpression(attributeName, join, root)
                          .as((Class<? extends Comparable>) fieldType),
                      (Comparable) val));
              break;
            case LESS_THAN:
              list.add(
                  cb.lessThanOrEqualTo(
                      getExpression(attributeName, join, root)
                          .as((Class<? extends Comparable>) fieldType),
                      (Comparable) val));
              break;
            case LESS_THAN_NQ:
              list.add(
                  cb.lessThan(
                      getExpression(attributeName, join, root)
                          .as((Class<? extends Comparable>) fieldType),
                      (Comparable) val));
              break;
            case INNER_LIKE:
              list.add(
                  cb.like(
                      getExpression(attributeName, join, root).as(String.class),
                      "%" + val.toString() + "%"));
              break;
            case LEFT_LIKE:
              list.add(
                  cb.like(
                      getExpression(attributeName, join, root).as(String.class),
                      "%" + val.toString()));
              break;
            case RIGHT_LIKE:
              list.add(
                  cb.like(
                      getExpression(attributeName, join, root).as(String.class),
                      val.toString() + "%"));
              break;
            case IN:
              if (!CollectionUtils.isEmpty((Collection<Long>) val)) {
                list.add(getExpression(attributeName, join, root).in((Collection<Long>) val));
              }
              break;
            case NOT_EQUAL:
              list.add(cb.notEqual(getExpression(attributeName, join, root), val));
              break;
            case NOT_NULL:
              list.add(cb.isNotNull(getExpression(attributeName, join, root)));
              break;
            case IS_NULL:
              list.add(cb.isNull(getExpression(attributeName, join, root)));
              break;
            case BETWEEN:
              List<Object> between = new ArrayList<>((List<Object>) val);
              list.add(
                  cb.between(
                      getExpression(attributeName, join, root)
                          .as((Class<? extends Comparable>) between.get(0).getClass()),
                      (Comparable) between.get(0),
                      (Comparable) between.get(1)));
              break;
            default:
              break;
          }
        }
        field.setAccessible(accessible);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    int size = list.size();
    return cb.and(list.toArray(new Predicate[size]));
  }

  @SuppressWarnings("unchecked")
  private static <T, R> Expression<T> getExpression(String attributeName, Join join, Root<R> root) {
    if (null != join) {
      return join.get(attributeName);
    } else {
      return root.get(attributeName);
    }
  }

  private static boolean isBlank(final CharSequence cs) {
    int strLen;
    if (cs == null || (strLen = cs.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static List<Field> getAllFields(Class clazz, List<Field> fields) {
    if (clazz != null) {
      fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
      getAllFields(clazz.getSuperclass(), fields);
    }
    return fields;
  }
}
