package com.thinkaurelius.titan.hadoop.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.hadoop.util.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.hadoop.HadoopGraph;

public class HadoopCompatLoader {

    private static final Logger log =
            LoggerFactory.getLogger(HadoopCompatLoader.class);

    private static volatile HadoopCompat defaultCompat;

    public static HadoopCompat getDefaultCompat() {
        // Volatile semantics are enough -- it's OK if we create and return more than one
        if (null == defaultCompat) {
            defaultCompat = getCompat();
        }
        return defaultCompat;
    }

    // TODO add a string argument that allows specifying a class instead of relying heuristics around VersionInfo.getVersion()
    // TODO add threadsafe caching that is aware of the string argument and instantiates a compat for each argument at most once (assuming the instantiation succeeds)
    public static HadoopCompat getCompat() {
        String ver = VersionInfo.getVersion();

        log.debug("Read Hadoop VersionInfo string {}", ver);

        final String pkgName = HadoopCompatLoader.class.getPackage().getName();
        final String className;

        if (ver.startsWith("1.")) {
            className = pkgName + ".Hadoop1Compat";
        } else {
            className = pkgName + ".Hadoop2Compat";
        }

        log.debug("Attempting to load class {} with a nullary constructor", className);
        try {
            Constructor<?> ctor = Class.forName(className).getConstructor();
            log.debug("Invoking constructor {}", ctor);
            return (HadoopCompat)ctor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}