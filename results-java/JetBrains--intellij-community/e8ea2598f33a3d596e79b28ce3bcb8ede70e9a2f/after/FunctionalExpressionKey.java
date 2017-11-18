/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.psi.impl.java.stubs;

import com.google.common.base.MoreObjects;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.io.IOUtil;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author peter
 */
public class FunctionalExpressionKey {
  public static final int UNKNOWN_PARAM_COUNT = -1;
  private final int lambdaParameterCount;
  private final CoarseType lambdaReturnType;
  public final Location location;

  public FunctionalExpressionKey(int lambdaParameterCount, @NotNull CoarseType lambdaReturnType, @NotNull Location location) {
    this.location = location;
    this.lambdaParameterCount = lambdaParameterCount;
    this.lambdaReturnType = lambdaReturnType;
  }

  @NotNull
  public static FunctionalExpressionKey deserializeKey(@NotNull DataInput dataStream) throws IOException {
    int parameterCount = dataStream.readByte();
    CoarseType type = CoarseType.values()[dataStream.readByte()];
    return new FunctionalExpressionKey(parameterCount, type, deserializeLocation(dataStream));
  }

  public void serializeKey(@NotNull DataOutput dataStream) throws IOException {
    dataStream.writeByte(lambdaParameterCount);
    dataStream.writeByte(lambdaReturnType.ordinal());
    serializeLocation(dataStream);
  }

  private static Location deserializeLocation(DataInput dataStream) throws IOException {
    byte locationType = dataStream.readByte();
    if (locationType == 0) return Location.UNKNOWN;
    if (locationType == 1) return CallLocation.deserializeCall(dataStream);
    if (locationType == 2) return TypedLocation.deserializeField(dataStream);
    throw new AssertionError(locationType);
  }

  private void serializeLocation(@NotNull DataOutput dataStream) throws IOException {
    if (location == Location.UNKNOWN) {
      dataStream.writeByte(0);
    }
    else if (location instanceof CallLocation) {
      dataStream.writeByte(1);
      ((CallLocation)location).serializeCall(dataStream);
    }
    else if (location instanceof TypedLocation) {
      dataStream.writeByte(2);
      ((TypedLocation)location).serializeVariable(dataStream);
    }
  }

  public boolean canRepresent(int samParamCount, boolean booleanCompatible, boolean isVoid) {
    if (lambdaParameterCount >= 0 && samParamCount != lambdaParameterCount) return false;

    switch (lambdaReturnType) {
      case VOID: return isVoid;
      case NON_VOID: return !isVoid;
      case BOOLEAN: return booleanCompatible;
      default: return true;
    }
  }

  public static boolean isBooleanCompatible(PsiType samType) {
    return PsiType.BOOLEAN.equals(samType) || TypeConversionUtil.isAssignableFromPrimitiveWrapper(TypeConversionUtil.erasure(samType));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FunctionalExpressionKey)) return false;

    FunctionalExpressionKey key = (FunctionalExpressionKey)o;

    if (lambdaParameterCount != key.lambdaParameterCount) return false;
    if (lambdaReturnType != key.lambdaReturnType) return false;
    if (!location.equals(key.location)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = lambdaParameterCount;
    result = 31 * result + lambdaReturnType.ordinal();
    result = 31 * result + location.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("lambdaParameterCount", lambdaParameterCount)
      .add("type", lambdaReturnType)
      .add("location", location)
      .toString();
  }

  public interface Location {
    Location UNKNOWN = new Location() {
      @Override
      public String toString() {
        return "UNKNOWN";
      }

      @Override
      public int hashCode() {
        return 0;
      }
    };
  }

  public static class CallLocation implements Location {
    public static final int MAX_ARG_COUNT = 10;
    @NotNull public final String methodName;
    public final int methodArgsLength;
    public final int callArgIndex;
    public final boolean streamApi;

    public CallLocation(@NotNull String methodName, int methodArgsLength, int callArgIndex, boolean streamApi) {
      this.methodName = methodName;
      this.methodArgsLength = Math.min(methodArgsLength, MAX_ARG_COUNT);
      this.callArgIndex = Math.min(callArgIndex, MAX_ARG_COUNT - 1);
      this.streamApi = streamApi;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof CallLocation)) return false;

      CallLocation location = (CallLocation)o;

      if (methodArgsLength != location.methodArgsLength) return false;
      if (callArgIndex != location.callArgIndex) return false;
      if (!methodName.equals(location.methodName)) return false;
      if (streamApi != location.streamApi) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = methodName.hashCode();
      result = 31 * result + methodArgsLength;
      result = 31 * result + callArgIndex;
      result = 31 * result + (streamApi ? 0 : 1);
      return result;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("methodName", methodName)
        .add("methodArgsLength", methodArgsLength)
        .add("callArgIndex", callArgIndex)
        .add("streamApi", streamApi)
        .toString();
    }

    @NotNull
    private static Location deserializeCall(DataInput dataStream) throws IOException {
      String methodName = IOUtil.readUTF(dataStream);
      int methodArgsLength = dataStream.readByte();
      int argIndex = dataStream.readByte();
      boolean streamApi = dataStream.readBoolean();
      return new CallLocation(methodName, methodArgsLength, argIndex, streamApi);
    }

    private void serializeCall(@NotNull DataOutput dataStream) throws IOException {
      IOUtil.writeUTF(dataStream, methodName);
      dataStream.writeByte(methodArgsLength);
      dataStream.writeByte(callArgIndex);
      dataStream.writeBoolean(streamApi);
    }

  }

  public static class TypedLocation implements Location {
    @NotNull public final String varType;

    public TypedLocation(@NotNull String varType) {
      this.varType = varType;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TypedLocation)) return false;

      TypedLocation location = (TypedLocation)o;

      if (!varType.equals(location.varType)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return varType.hashCode();
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("fieldType", varType)
        .toString();
    }

    public static TypedLocation deserializeField(DataInput dataStream) throws IOException {
      return new TypedLocation(IOUtil.readUTF(dataStream));
    }

    public void serializeVariable(DataOutput dataStream) throws IOException {
      IOUtil.writeUTF(dataStream, varType);
    }
  }

  public enum CoarseType {
    VOID, UNKNOWN, BOOLEAN, NON_VOID
  }
}