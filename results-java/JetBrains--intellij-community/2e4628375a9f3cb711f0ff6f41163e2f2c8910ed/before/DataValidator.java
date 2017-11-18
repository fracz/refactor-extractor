package com.intellij.ide.impl;

import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.KeyedLazyInstanceEP;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Map;

public abstract class DataValidator <T> {
  private static boolean ourExtensionsLoaded;

  public static final ExtensionPointName<KeyedLazyInstanceEP> EP_NAME = ExtensionPointName.create("com.intellij.dataValidator");

  Logger LOG = Logger.getInstance("#com.intellij.ide.impl.DataValidator");

  private static final Map<String, DataValidator> ourValidators = new HashMap<String, DataValidator>();
  private static final DataValidator<VirtualFile> VIRTUAL_FILE_VALIDATOR = new DataValidator<VirtualFile>() {
    public VirtualFile findInvalid(final String dataId, VirtualFile file, final Object dataSource) {
      return file.isValid() ? null : file;
    }
  };

  @Nullable
  public abstract T findInvalid(final String dataId, T data, final Object dataSource);

  private static <T> DataValidator<T> getValidator(String dataId) {
    if (!ourExtensionsLoaded) {
      ourExtensionsLoaded = true;
      for(KeyedLazyInstanceEP ep: Extensions.getExtensions(EP_NAME)) {
        ourValidators.put(ep.key, (DataValidator) ep.getInstance());
      }
    }
    return ourValidators.get(dataId);
  }

  public static <T> T findInvalidData(String dataId, Object data, final Object dataSource) {
    if (data == null) return null;
    DataValidator<T> validator = getValidator(dataId);
    if (validator != null) return validator.findInvalid(dataId, (T) data, dataSource);
    return null;
  }

  static {
    ourValidators.put(DataConstants.VIRTUAL_FILE, VIRTUAL_FILE_VALIDATOR);
    ourValidators.put(DataConstants.VIRTUAL_FILE_ARRAY, new ArrayValidator<VirtualFile>(VIRTUAL_FILE_VALIDATOR));
  }

  public static class ArrayValidator<T> extends DataValidator<T[]> {
    private final DataValidator<T> myElementValidator;

    public ArrayValidator(DataValidator<T> elementValidator) {
      myElementValidator = elementValidator;
    }

    public T[] findInvalid(final String dataId, T[] array, final Object dataSource) {
      for (T element : array) {
        LOG.assertTrue(element != null, "Data isn't valid. " + dataId + "=null Provided by: " + dataSource.getClass().getName() + " (" +
                            dataSource.toString() + ")");
        T invalid = myElementValidator.findInvalid(dataId, element, dataSource);
        if (invalid != null) {
          T[] result = (T[])Array.newInstance(array[0].getClass(), 1);
          result[0] = invalid;
          return result;
        }
      }
      return null;
    }
  }

}