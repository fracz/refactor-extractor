/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package com.intellij.codeInspection.bytecodeAnalysis;

import com.intellij.util.ArrayFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Represents a lattice product of a constant {@link #value} and all {@link #ids}.
 */
final class Component {
  static final Component[] EMPTY_ARRAY = new Component[0];
  static final ArrayFactory<Component> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new Component[count];
  @NotNull Value value;
  @NotNull final EKey[] ids;

  Component(@NotNull Value value, @NotNull Set<EKey> ids) {
    this(value, ids.toArray(new EKey[0]));
  }

  Component(@NotNull Value value, @NotNull EKey[] ids) {
    this.value = value;
    this.ids = ids;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Component that = (Component)o;

    return value == that.value && Arrays.equals(ids, that.ids);
  }

  @Override
  public int hashCode() {
    return 31 * value.hashCode() + Arrays.hashCode(ids);
  }

  public boolean remove(@NotNull EKey id) {
    boolean removed = false;
    for (int i = 0; i < ids.length; i++) {
      if (id.equals(ids[i])) {
        ids[i] = null;
        removed = true;
      }
    }
    return removed;
  }

  public boolean isEmpty() {
    for (EKey id : ids) {
      if (id != null) return false;
    }
    return true;
  }

  @NotNull
  public Component copy() {
    return new Component(value, ids.clone());
  }
}

final class Equation {
  @NotNull final EKey key;
  @NotNull final Result result;

  Equation(@NotNull EKey key, @NotNull Result result) {
    this.key = key;
    this.result = result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Equation equation = (Equation)o;
    return key.equals(equation.key) && result.equals(equation.result);
  }

  @Override
  public int hashCode() {
    return 31 * key.hashCode() + result.hashCode();
  }

  @Override
  public String toString() {
    return "Equation{" + "key=" + key + ", result=" + result + '}';
  }
}

class Equations {
  @NotNull final List<DirectionResultPair> results;
  final boolean stable;

  Equations(@NotNull List<DirectionResultPair> results, boolean stable) {
    this.results = results;
    this.stable = stable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Equations that = (Equations)o;
    return stable == that.stable && results.equals(that.results);
  }

  @Override
  public int hashCode() {
    return 31 * results.hashCode() + (stable ? 1 : 0);
  }
}

class DirectionResultPair {
  final int directionKey;
  @NotNull
  final Result hResult;

  DirectionResultPair(int directionKey, @NotNull Result hResult) {
    this.directionKey = directionKey;
    this.hResult = hResult;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DirectionResultPair that = (DirectionResultPair)o;
    return directionKey == that.directionKey && hResult.equals(that.hResult);
  }

  @Override
  public int hashCode() {
    return 31 * directionKey + hResult.hashCode();
  }
}

interface Result {}
final class Final implements Result {
  @NotNull final Value value;

  Final(@NotNull Value value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    return value == ((Final)o).value;
  }

  @Override
  public int hashCode() {
    return value.ordinal();
  }

  @Override
  public String toString() {
    return "Final{" + "value=" + value + '}';
  }
}

final class Pending implements Result {
  @NotNull final Component[] delta; // sum

  Pending(Collection<Component> delta) {
    this(delta.toArray(Component.EMPTY_ARRAY));
  }

  Pending(@NotNull Component[] delta) {
    this.delta = delta;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return Arrays.equals(delta, ((Pending)o).delta);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(delta);
  }

  @NotNull
  Pending copy() {
    Component[] copy = new Component[delta.length];
    for (int i = 0; i < delta.length; i++) {
      copy[i] = delta[i].copy();
    }
    return new Pending(copy);
  }
}

final class Effects implements Result {
  @NotNull final Set<EffectQuantum> effects;

  Effects(@NotNull Set<EffectQuantum> effects) {
    this.effects = effects;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return this.effects.equals(((Effects)o).effects);
  }

  @Override
  public int hashCode() {
    return effects.hashCode();
  }
}