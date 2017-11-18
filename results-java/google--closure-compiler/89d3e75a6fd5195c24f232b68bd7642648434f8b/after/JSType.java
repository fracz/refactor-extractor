/*
 * Copyright 2013 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp.newtypes;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 *
 * @author blickly@google.com (Ben Lickly)
 * @author dimvar@google.com (Dimitris Vardoulakis)
 */
public class JSType {
  private static final int BOTTOM_MASK = 0x0;
  private static final int STRING_MASK = 0x1;
  private static final int NUMBER_MASK = 0x2;
  private static final int UNDEFINED_MASK = 0x4;
  private static final int TRUE_MASK = 0x8;
  private static final int FALSE_MASK = 0x10;
  private static final int NULL_MASK = 0x20;
  private static final int NON_SCALAR_MASK = 0x40;
  private static final int END_MASK = NON_SCALAR_MASK * 2;
  // When either of the next two bits is set, the rest of the type isn't
  // guaranteed to be in a consistent state.
  private static final int TRUTHY_MASK = 0x100;
  private static final int FALSY_MASK = 0x200;
  // Room to grow.
  private static final int UNKNOWN_MASK = 0x7fffffff; // @type {?}
  private static final int TOP_MASK = 0xffffffff; // @type {*}

  private static final int BOOLEAN_MASK = TRUE_MASK | FALSE_MASK;
  private static final int TOP_SCALAR_MASK =
      NUMBER_MASK | STRING_MASK | BOOLEAN_MASK | NULL_MASK | UNDEFINED_MASK;

  private final int type;
  // objs is null for scalar types
  private final ImmutableSet<ObjectType> objs;
  private final String location;

  private JSType(int type, String location, ImmutableSet<ObjectType> objs) {
    this.location = location;
    if (objs == null) {
      this.type = type;
      this.objs = null;
    } else if (objs.size() == 0) {
      this.type = type & ~NON_SCALAR_MASK;
      this.objs = null;
    } else {
      this.type = type | NON_SCALAR_MASK;
      this.objs = objs;
    }
    Preconditions.checkState(this.isValidType());
  }

  private JSType(int type) {
    this(type, null, null);
  }

  // Factory method for wrapping a function in a JSType
  public static JSType fromFunctionType(FunctionType fn) {
    return new JSType(NON_SCALAR_MASK, null,
        ImmutableSet.of(ObjectType.fromFunction(fn)));
  }

  public static JSType fromObjectType(ObjectType obj) {
    return new JSType(NON_SCALAR_MASK, null, ImmutableSet.of(obj));
  }

  boolean isValidType() {
    return isUnknown() || isTop() ||
        ((type & NON_SCALAR_MASK) != 0 && !objs.isEmpty()) ||
        ((type & NON_SCALAR_MASK) == 0 && objs == null);
  }

  public static final JSType BOOLEAN = new JSType(TRUE_MASK | FALSE_MASK);
  public static final JSType BOTTOM = new JSType(BOTTOM_MASK);
  public static final JSType FALSE_TYPE = new JSType(FALSE_MASK);
  public static final JSType FALSY = new JSType(FALSY_MASK);
  public static final JSType NULL = new JSType(NULL_MASK);
  public static final JSType NUMBER = new JSType(NUMBER_MASK);
  public static final JSType STRING = new JSType(STRING_MASK);
  public static final JSType TOP = new JSType(TOP_MASK);
  public static final JSType TOP_SCALAR = new JSType(TOP_SCALAR_MASK);
  public static final JSType TRUE_TYPE = new JSType(TRUE_MASK);
  public static final JSType TRUTHY = new JSType(TRUTHY_MASK);
  public static final JSType UNDEFINED = new JSType(UNDEFINED_MASK);
  public static final JSType UNKNOWN = new JSType(UNKNOWN_MASK);

  public static final JSType TOP_OBJECT = fromObjectType(ObjectType.TOP_OBJECT);
  public static final JSType NULL_OR_UNDEF =
      new JSType(NULL_MASK | UNDEFINED_MASK);
  private static JSType TOP_FUNCTION = null;

  private static final JSType TOP_MINUS_NULL = new JSType(
      TRUE_MASK | FALSE_MASK | NUMBER_MASK | STRING_MASK | UNDEFINED_MASK |
      NON_SCALAR_MASK,
      null, ImmutableSet.of(ObjectType.TOP_OBJECT));
  private static final JSType TOP_MINUS_UNDEF = new JSType(
      TRUE_MASK | FALSE_MASK | NUMBER_MASK | STRING_MASK | NULL_MASK |
      NON_SCALAR_MASK,
      null, ImmutableSet.of(ObjectType.TOP_OBJECT));

  public static JSType topFunction() {
    if (TOP_FUNCTION == null) {
      TOP_FUNCTION = fromFunctionType(FunctionType.TOP_FUNCTION);
    }
    return TOP_FUNCTION;
  }

  public boolean isTop() {
    return TOP_MASK == type;
  }

  public boolean isBottom() {
    return BOTTOM_MASK == type;
  }

  public boolean isUnknown() {
    return UNKNOWN_MASK == type;
  }

  public boolean isTrue() {
    return TRUE_MASK == type;
  }

  public boolean isFalse() {
    return FALSE_MASK == type;
  }

  public boolean isTruthy() {
    return TRUTHY_MASK == type || TRUE_MASK == type;
  }

  public boolean isFalsy() {
    return FALSY_MASK == type || FALSE_MASK == type;
  }

  public boolean isBoolean() {
    return (type & ~BOOLEAN_MASK) == 0 && (type & BOOLEAN_MASK) != 0;
  }

  public boolean isNullOrUndef() {
    int mask = NULL_MASK | UNDEFINED_MASK;
    return type != 0 && (type | mask) == mask;
  }

  public boolean isScalar() {
    return type == NUMBER_MASK ||
        type == STRING_MASK ||
        type == NULL_MASK ||
        type == UNDEFINED_MASK ||
        this.isBoolean();
  }

  // True iff there exists a value that can have this type
  public boolean isInhabitable() {
    if (isBottom()) {
      return false;
    } else if (objs == null) {
      return true;
    }
    for (ObjectType obj: objs) {
      if (!obj.isInhabitable()) {
        return false;
      }
    }
    return true;
  }

  public boolean hasNonScalar() {
    return objs != null;
  }

  public static boolean areCompatibleScalarTypes(JSType lhs, JSType rhs) {
    Preconditions.checkArgument(
        lhs.isSubtypeOf(TOP_SCALAR) || rhs.isSubtypeOf(TOP_SCALAR));
    if (lhs.isBottom() || rhs.isBottom() ||
        lhs.isUnknown() || rhs.isUnknown() ||
        (lhs.isBoolean() && rhs.isBoolean()) ||
        lhs.equals(rhs)) {
      return true;
    }
    return false;
  }

  // When joining w/ TOP or UNKNOWN, avoid setting more fields on them, eg, obj.
  public static JSType join(JSType lhs, JSType rhs) {
    if (lhs.isTop() || rhs.isTop()) {
      return TOP;
    } else if (lhs.isUnknown() || rhs.isUnknown()) {
      return UNKNOWN;
    }
    return new JSType(
        lhs.type | rhs.type,
        Objects.equal(lhs.location, rhs.location) ? lhs.location : null,
        ObjectType.joinSets(lhs.objs, rhs.objs));
  }

  // Specialize this type by meeting with other, but keeping location
  public JSType specialize(JSType other) {
    if (other.isTop() || other.isUnknown()) {
      return this;
    } else if (other.isTruthy()) {
      return makeTruthy();
    } else if (other.isFalsy()) {
      return makeFalsy();
    } else if (this.isTop() || this.isUnknown()) {
      return other.withLocation(this.location);
    }
    return new JSType(this.type & other.type, this.location,
        ObjectType.specializeSet(this.objs, other.objs));
  }

  private JSType makeTruthy() {
    if (this.isTop() || this.isUnknown()) {
      return this;
    }
    return new JSType(type & ~NULL_MASK & ~FALSE_MASK & ~UNDEFINED_MASK,
        location, objs);
  }

  private JSType makeFalsy() {
    if (this.isTop() || this.isUnknown()) {
      return this;
    }
    return new JSType(type & ~TRUE_MASK & ~NON_SCALAR_MASK, location, null);
  }

  // Meet two types, location agnostic
  public static JSType meet(JSType lhs, JSType rhs) {
    if (lhs.isTop()) {
      return rhs;
    } else if (rhs.isTop()) {
      return lhs;
    } else if (lhs.isUnknown()) {
      return rhs;
    } else if (rhs.isUnknown()) {
      return lhs;
    }
    return new JSType(lhs.type & rhs.type, null,
        ObjectType.meetSets(lhs.objs, rhs.objs));
  }

  public static JSType plus(JSType lhs, JSType rhs) {
    int newtype = (lhs.type | rhs.type) & STRING_MASK;
    if ((lhs.type & ~STRING_MASK) != 0 && (rhs.type & ~STRING_MASK) != 0) {
      newtype |= NUMBER_MASK;
    }
    return new JSType(newtype);
  }

  public JSType negate() {
    if (isTruthy()) {
      return FALSY;
    } else if (isFalsy()) {
      return TRUTHY;
    }
    return this;
  }

  public JSType toBoolean() {
    if (isTruthy()) {
      return TRUE_TYPE;
    } else if (isFalsy()) {
      return FALSE_TYPE;
    }
    return BOOLEAN;
  }

  public boolean isSubtypeOf(JSType other) {
    if (isUnknown() || other.isUnknown() || other.isTop()) {
      return true;
    } else if ((type | other.type) != other.type) {
      return false;
    } else if (this.objs == null) {
      return true;
    }
    // Because of optional properties,
    //   x \le y \iff x \join y = y does not hold.
    return ObjectType.isUnionSubtype(this.objs, other.objs);
  }

  public JSType removeType(JSType other) {
    if ((isTop() || isUnknown()) && other.equals(NULL)) {
      return TOP_MINUS_NULL;
    }
    if ((isTop() || isUnknown()) && other.equals(UNDEFINED)) {
      return TOP_MINUS_UNDEF;
    }
    if (other.equals(NULL) || other.equals(UNDEFINED)) {
      return new JSType(type & ~other.type, location, objs);
    }
    if (objs == null) {
      return this;
    }
    Preconditions.checkState(
        (other.type & ~NON_SCALAR_MASK) == 0 && other.objs.size() == 1);
    NominalType otherKlass =
        Iterables.getOnlyElement(other.objs).getClassType();
    ImmutableSet.Builder<ObjectType> newObjs = ImmutableSet.builder();
    for (ObjectType obj: objs) {
      if (!Objects.equal(obj.getClassType(), otherKlass)) {
        newObjs.add(obj);
      }
    }
    return new JSType(type, location, newObjs.build());
  }

  public JSType withLocation(String location) {
    return new JSType(type, location, objs);
  }

  public String getLocation() {
    return location;
  }

  public FunctionType getFunTypeIfSingletonObj() {
    if (type != NON_SCALAR_MASK || objs.size() > 1) {
      return null;
    }
    return Iterables.getOnlyElement(objs).getFunType();
  }

  public FunctionType getFunType() {
    if (objs == null) {
      return null;
    }
    Preconditions.checkState(objs.size() == 1);
    return Iterables.getOnlyElement(objs).getFunType();
  }

  NominalType getClassTypeIfUnique() {
    if (objs == null || objs.size() > 1) {
      return null;
    }
    return Iterables.getOnlyElement(objs).getClassType();
  }

  public JSType withLoose() {
    Preconditions.checkNotNull(this.objs);
    return new JSType(
        this.type, this.location, ObjectType.withLooseObjects(this.objs));
  }

  public JSType getProp(String qName) {
    if (isUnknown()) {
      return UNKNOWN;
    }
    Preconditions.checkState(objs != null);
    JSType ptype = BOTTOM;
    for (ObjectType o : objs) {
      if (o.mayHaveProp(qName)) {
        ptype = join(ptype, o.getProp(qName));
      }
    }
    if (ptype.isBottom()) {
      return null;
    }
    return ptype;
  }

  public boolean mayHaveProp(String qName) {
    if (objs == null) {
      return false;
    }
    for (ObjectType o : objs) {
      if (o.mayHaveProp(qName)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasProp(String qName) {
    if (objs == null) {
      return false;
    }
    for (ObjectType o : objs) {
      if (!o.hasProp(qName)) {
        return false;
      }
    }
    return true;
  }

  public JSType getDeclaredProp(String qName) {
    if (isUnknown()) {
      return UNKNOWN;
    }
    Preconditions.checkState(objs != null);
    JSType ptype = BOTTOM;
    for (ObjectType o : objs) {
      JSType declType = o.getDeclaredProp(qName);
      if (declType != null) {
        ptype = join(ptype, declType);
      }
    }
    return ptype.isBottom() ? null : ptype;
  }

  public JSType withoutProperty(String qname) {
    Preconditions.checkState(this.objs != null);
    return new JSType(this.type, this.location,
        ObjectType.withoutProperty(this.objs, qname));
  }

  public JSType withProperty(String qname, JSType type) {
    if (isUnknown()) {
      return this;
    }
    Preconditions.checkState(this.objs != null);
    return new JSType(this.type, this.location,
        ObjectType.withProperty(this.objs, qname, type));
  }

  public JSType withDeclaredProperty(String qname, JSType type) {
    Preconditions.checkState(this.objs != null && this.location == null);
    return new JSType(this.type, null,
        ObjectType.withDeclaredProperty(this.objs, qname, type));
  }

  public JSType withPropertyRequired(String pname) {
    if (isUnknown()) {
      return this;
    }
    Preconditions.checkState(this.objs != null);
    return new JSType(this.type, this.location,
        ObjectType.withPropertyRequired(this.objs, pname));
  }

  @Override
  public String toString() {
    return typeToString() + locationPostfix(location);
  }

  private String typeToString() {
    switch (type) {
      case BOTTOM_MASK:
      case TOP_MASK:
      case UNKNOWN_MASK:
        return tagToString(type, null);
      default:
        int tags = type;
        Set<String> types = Sets.newTreeSet();
        for (int mask = 1; mask != END_MASK; mask <<= 1) {
          if ((tags & mask) != 0) {
            types.add(tagToString(mask, objs));
            tags = tags & ~mask;  // Remove current mask from union
          }
        }
        if (tags == 0) { // Found all types in the union
          return Joiner.on("|").join(types);
        } else {
          return "Unrecognized type: " + tags;
        }
    }
  }

  private static String locationPostfix(String location) {
    if (location == null) {
      return "";
    } else {
      return "@" + location;
    }
  }

  /**
   * Takes a type tag with a single bit set (including the non-scalar bit),
   * and prints the string representation of that single type.
   */
  private static String tagToString(int tag, Set<ObjectType> objs) {
    switch (tag) {
      case TRUE_MASK:
      case FALSE_MASK:
        return "boolean";
      case BOTTOM_MASK:
        return "bottom";
      case STRING_MASK:
        return "string";
      case NON_SCALAR_MASK:
        Set<String> strReps = Sets.newHashSet();
        for (ObjectType obj : objs) {
          strReps.add(obj.toString());
        }
        return Joiner.on("|").join(strReps);
      case NULL_MASK:
        return "null";
      case NUMBER_MASK:
        return "number";
      case TOP_MASK:
        return "top";
      case UNDEFINED_MASK:
        return "undefined";
      case UNKNOWN_MASK:
        return "unknown";
      default: // Must be a union type.
        return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    Preconditions.checkArgument(o instanceof JSType);
    JSType t2 = (JSType) o;
    return this.type == t2.type && Objects.equal(this.objs, t2.objs);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(type, objs);
  }
}