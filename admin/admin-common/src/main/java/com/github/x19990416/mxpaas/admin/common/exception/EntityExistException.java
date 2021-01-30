/**
 * create by Guo Limin on 2021/1/29.
 */
package com.github.x19990416.mxpaas.admin.common.exception;

import org.springframework.util.StringUtils;

public class EntityExistException extends RuntimeException {

    public EntityExistException(Class clazz, String field, String val) {
        super(EntityExistException.generateMessage(clazz.getSimpleName(), field, val));
    }

    private static String generateMessage(String entity, String field, String val) {
        return StringUtils.capitalize(entity)
                + " with " + field + " "+ val + " existed";
    }
}