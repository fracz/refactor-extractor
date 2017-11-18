package com.grosner.processor.model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ReflectionUtils {


    public static boolean isSubclassOf(String columnFieldType, Class<?> enumClass) {
        boolean isSubClass = false;
        try {
            Class type = Class.forName(columnFieldType);
            isSubClass = type.getSuperclass() != null && (type.getSuperclass().equals(enumClass) ||
                    isSubclassOf(type.getSuperclass().getTypeName(), enumClass));
        } catch (ClassNotFoundException e) {
        }
        return isSubClass;
    }
}