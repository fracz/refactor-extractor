package com.intellij.refactoring.typeCook.deductive.resolver;

import com.intellij.refactoring.typeCook.deductive.PsiTypeVariableFactory;
import com.intellij.psi.PsiTypeVariable;
import com.intellij.refactoring.typeCook.deductive.PsiExtendedTypeVisitor;
import com.intellij.refactoring.typeCook.deductive.builder.Constraint;
import com.intellij.refactoring.typeCook.deductive.builder.Subtype;
import com.intellij.refactoring.typeCook.Util;
import com.intellij.psi.Bottom;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.project.Project;
import com.intellij.util.IncorrectOperationException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: Jan 13, 2005
 * Time: 3:46:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class BindingFactory {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.typeCook.deductive.resolver.BindingFactory");

  private HashSet<PsiTypeVariable> myBoundVariables;
  private Project myProject;
  private PsiTypeVariableFactory myFactory;

  private PsiClass[] getGreatestLowerClasses(final PsiClass aClass, final PsiClass bClass) {
    if (InheritanceUtil.isInheritorOrSelf(aClass, bClass, true)) {
      return new PsiClass[]{aClass};
    }

    if (InheritanceUtil.isInheritorOrSelf(bClass, aClass, true)) {
      return new PsiClass[]{bClass};
    }

    final Set<PsiClass> descendants = new LinkedHashSet<PsiClass>();

    new Object() {
      public void getGreatestLowerClasses(final PsiClass aClass, final PsiClass bClass, final Set<PsiClass> descendants) {
        if (bClass.hasModifierProperty(PsiModifier.FINAL)) return;
        if (aClass.isInheritor(bClass, true)) {
          descendants.add(aClass);
        }
        else {
          final PsiSearchHelper helper = aClass.getManager().getSearchHelper();
          final PsiClass[] bSubs = helper.findInheritors(bClass, GlobalSearchScope.allScope(myProject), false);
          for (int i = 0; i < bSubs.length; i++) {
            getGreatestLowerClasses(bSubs[i], aClass, descendants);
          }
        }
      }
    }.getGreatestLowerClasses(aClass, bClass, descendants);

    return descendants.toArray(new PsiClass[descendants.size()]);
  }

  private class BindingImpl extends Binding {
    private HashMap<Integer, PsiType> myBindings;
    private boolean myCyclic;

    BindingImpl(final PsiTypeVariable var, final PsiType type) {
      myBindings = new HashMap<Integer, PsiType>();
      myCyclic = type instanceof PsiTypeVariable;

      myBindings.put(new Integer(var.getIndex()), type);
    }

    BindingImpl(final int index, final PsiType type) {
      myBindings = new HashMap<Integer, PsiType>();
      myCyclic = type instanceof PsiTypeVariable;

      myBindings.put(new Integer(index), type);

      if (type instanceof Bottom) {
        final HashSet<PsiTypeVariable> cluster = myFactory.getClusterOf(index);

        if (cluster != null) {
          for (PsiTypeVariable var : cluster) {
            myBindings.put(new Integer(var.getIndex()), type);
          }
        }
      }
    }

    BindingImpl() {
      myBindings = new HashMap<Integer, PsiType>();
      myCyclic = false;
    }

    public PsiType apply(final PsiType type) {
      if (type instanceof PsiTypeVariable) {
        final PsiType t = myBindings.get(new Integer(((PsiTypeVariable)type).getIndex()));
        return t == null ? type : t;
      }
      else if (type instanceof PsiArrayType) {
        return apply(((PsiArrayType)type).getComponentType()).createArrayType();
      }
      else if (type instanceof PsiClassType) {
        final PsiClassType.ClassResolveResult result = Util.resolveType(type);
        final PsiClass theClass = result.getElement();
        final PsiSubstitutor aSubst = result.getSubstitutor();

        PsiSubstitutor theSubst = PsiSubstitutor.EMPTY;

        if (theClass != null) {
          for (final PsiTypeParameter aParm : aSubst.getSubstitutionMap().keySet()) {
            final PsiType aType = aSubst.substitute(aParm);

            theSubst = theSubst.put(aParm, apply(aType));
          }

          return theClass.getManager().getElementFactory().createType(theClass, theSubst);
        }
        else {
          return type;
        }
      }
      else if (type instanceof PsiWildcardType) {
        final PsiWildcardType wcType = (PsiWildcardType)type;
        final PsiType bound = wcType.getBound();

        if (bound != null) {
          final PsiType abound = apply(bound);

          if (abound instanceof PsiWildcardType) {
            return null;
          }

          return
            wcType.isExtends()
            ? PsiWildcardType.createExtends(PsiManager.getInstance(myProject), abound)
            : PsiWildcardType.createSuper(PsiManager.getInstance(myProject), abound);
        }

        return type;
      }
      else {
        return type;
      }
    }

    public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof BindingImpl)) return false;

      final BindingImpl binding = (BindingImpl)o;

      if (!myBindings.equals(binding.myBindings)) {
        return false;
      }

      return true;
    }

    public Binding compose(final Binding b) {
      LOG.assertTrue(b instanceof BindingImpl);

      final BindingImpl b1 = this;
      final BindingImpl b2 = (BindingImpl)b;

      final BindingImpl b3 = new BindingImpl();

      for (PsiTypeVariable boundVariable : myBoundVariables) {
        final Integer i = new Integer(boundVariable.getIndex());

        final PsiType b1i = b1.myBindings.get(i);
        final PsiType b2i = b2.myBindings.get(i);

        final int flag = (b1i == null ? 0 : 1) + (b2i == null ? 0 : 2);

        switch (flag) {
          case 0:
            break;

          case 1: /* b1(i)\b2(i) */
            {
              final PsiType type = b2.apply(b1i);

              if (type == null) {
                return null;
              }

              b3.myBindings.put(i, type);
              b3.myCyclic = type instanceof PsiTypeVariable;
            }
            break;

          case 2: /* b2(i)\b1(i) */
            {
              final PsiType type = b1.apply(b2i);

              if (type == null) {
                return null;
              }

              b3.myBindings.put(i, type);
              b3.myCyclic = type instanceof PsiTypeVariable;
            }
            break;

          case 3:  /* b2(i) \cap b1(i) */
          {
            final Binding common = rise(b1i, b2i, null);

            if (common == null) {
              return null;
            }

            final PsiType type = common.apply(b1i);
            if (type == null) {
              return null;
            }

            final PsiType type1 = type;//b2.apply(type);

            if (type1 == null) {
              return null;
            }

            b3.myBindings.put(i, type1);
            b3.myCyclic = type instanceof PsiTypeVariable;
          }
        }
      }

      return b3;
    }

    public String toString() {
      final StringBuffer buffer = new StringBuffer();

      for (PsiTypeVariable boundVariable : myBoundVariables) {
        final Integer i = new Integer(boundVariable.getIndex());
        final PsiType binding = myBindings.get(i);

        if (binding != null) {
          buffer.append("#" + i + " -> " + binding.getPresentableText() + "; ");
        }
      }

      return buffer.toString();
    }

    private PsiType normalize(final PsiType t) {
      if (t == null || t instanceof PsiTypeVariable) {
        return Bottom.BOTTOM;
      }

      if (t instanceof PsiWildcardType) {
        return ((PsiWildcardType)t).getBound();
      }

      return t;
    }

    public int compare(final Binding binding) {
      final BindingImpl b2 = (BindingImpl)binding;
      final BindingImpl b1 = this;

      int directoin = Binding.NONCOMPARABLE;
      boolean first = true;

      for (PsiTypeVariable boundVariable : myBoundVariables) {
        final Integer index = new Integer(boundVariable.getIndex());

        final PsiType x = normalize(b1.myBindings.get(index));
        final PsiType y = normalize(b2.myBindings.get(index));

        final int comp = new Object() {
          int compare(final PsiType x, final PsiType y) {
            final int[] kinds = new Object() {
              private int classify(final PsiType x) {
                if (x == null) {
                  return 0;
                }

                if (x instanceof PsiPrimitiveType) {
                  return 1;
                }

                if (x instanceof PsiArrayType) {
                  return 2;
                }

                if (x instanceof PsiClassType) {
                  return 3;
                }

                return 4; // Bottom
              }

              int[] classify2(final PsiType x, final PsiType y) {
                return new int[]{classify(x), classify(y)};
              }
            }.classify2(x, y);

            final int kindX = kinds[0];
            final int kindY = kinds[1];

            // Break your brain here...
            if (kindX + kindY == 0) {
              return Binding.SAME;
            }

            if (kindX * kindY == 0) {
              if (kindX == 0) {
                return Binding.WORSE;
              }

              return Binding.BETTER;
            }

            if (kindX * kindY == 1) {
              if (x.equals(y)) {
                return Binding.SAME;
              }

              return Binding.NONCOMPARABLE;
            }

            if (kindX != kindY) {
              if (kindX == 4) {
                return Binding.WORSE;
              }

              if (kindY == 4) {
                return Binding.BETTER;
              }

              if (kindX + kindY == 5) {
                try {
                  final PsiElementFactory f = PsiManager.getInstance(myProject).getElementFactory();
                  final PsiType cloneable = f.createTypeFromText("java.lang.Cloneable", null);
                  final PsiType object = f.createTypeFromText("java.lang.Object", null);
                  final PsiType serializable = f.createTypeFromText("java.io.Serializable", null);

                  PsiType type;
                  int flag;

                  if (kindX == 3) {
                    type = x;
                    flag = Binding.WORSE;
                  }
                  else {
                    type = y;
                    flag = Binding.BETTER;
                  }

                  if (type.equals(object) || type.equals(cloneable) || type.equals(serializable)) {
                    return flag;
                  }
                }
                catch (IncorrectOperationException e) {
                  LOG.error(e);
                }
              }

              return Binding.NONCOMPARABLE;
            }

            if (kindX == 2) {
              return compare(((PsiArrayType)x).getComponentType(), ((PsiArrayType)y).getComponentType());
            }

            if (x.equals(y)) {
              return Binding.SAME;
            }
            // End of breaking...

            final PsiClassType.ClassResolveResult resultX = Util.resolveType(x);
            final PsiClassType.ClassResolveResult resultY = Util.resolveType(y);

            final PsiClass xClass = resultX.getElement();
            final PsiClass yClass = resultY.getElement();

            final PsiSubstitutor xSubst = resultX.getSubstitutor();
            final PsiSubstitutor ySubst = resultY.getSubstitutor();

            if (xClass == null || yClass == null) {
              return Binding.NONCOMPARABLE;
            }

            if (xClass.equals(yClass)) {
              boolean first = true;
              int direction = Binding.SAME;

              for (Iterator<PsiTypeParameter> i = xSubst.getSubstitutionMap().keySet().iterator(); i.hasNext();) {
                final PsiTypeParameter p = i.next();

                final PsiType xParm = xSubst.substitute(p);
                final PsiType yParm = ySubst.substitute(p);

                final int comp = compare(xParm, yParm);

                if (comp == Binding.NONCOMPARABLE) {
                  return Binding.NONCOMPARABLE;
                }

                if (first) {
                  first = false;
                  direction = comp;
                }

                if (direction != comp) {
                  return Binding.NONCOMPARABLE;
                }
              }

              return direction;
            }
            else {
              if (InheritanceUtil.isCorrectDescendant(xClass, yClass, true)) {
                return Binding.BETTER;
              }
              else if (InheritanceUtil.isCorrectDescendant(yClass, xClass, true)) {
                return Binding.WORSE;
              }

              return Binding.NONCOMPARABLE;
            }
          }
        }.compare(x, y);

        if (comp == Binding.NONCOMPARABLE) {
          return Binding.NONCOMPARABLE;
        }

        if (first) {
          first = false;
          directoin = comp;
        }

        if (directoin != SAME) {
          if (comp != Binding.SAME && directoin != comp) {
            return Binding.NONCOMPARABLE;
          }
        }
        else if (comp != SAME) {
          directoin = comp;
        }
      }

      return directoin;
    }

    public boolean nonEmpty() {
      return myBindings.size() > 0;
    }

    public boolean isCyclic() {
      return myCyclic;
    }

    public Binding reduceRecursive() {
      final BindingImpl binding = (BindingImpl)create();

      for (final PsiTypeVariable var : myBoundVariables) {
        final Integer index = new Integer(var.getIndex());
        final PsiType type = myBindings.get(index);

        if (type != null) {
          class Verifier extends PsiExtendedTypeVisitor {
            boolean myFlag = false;

            public Object visitTypeVariable(final PsiTypeVariable var) {
              if (var.getIndex() == index.intValue()) {
                myFlag = true;
              }

              return null;
            }
          }

          final Verifier verifier = new Verifier();

          type.accept(verifier);

          if (verifier.myFlag) {
            myBindings.put(index, Bottom.BOTTOM);
            binding.myBindings.put(index, Bottom.BOTTOM);
          }
          else {
            binding.myBindings.put(index, type);
          }
        }
        else {
          binding.myBindings.put(index, type);
        }
      }

      for (final PsiTypeVariable var : myBoundVariables) {
        final Integer index = new Integer(var.getIndex());
        final PsiType type = myBindings.get(index);

        if (type != null) {
          myBindings.put(index, binding.apply(type));
        }
      }

      return this;
    }

    public boolean binds(final PsiTypeVariable var) {
      return myBindings.get(new Integer(var.getIndex())) != null;
    }

    public void merge(final Binding b, final boolean removeObject) {
      for (final PsiTypeVariable var : b.getBoundVariables()) {
        final Integer index = new Integer(var.getIndex());

        if (myBindings.get(index) != null) {
          LOG.error("Oops... Binding conflict...");
        }
        else {
          final PsiType type = b.apply(var);
          final PsiClassType javaLangObject =
            PsiType.getJavaLangObject(PsiManager.getInstance(myProject), GlobalSearchScope.allScope(myProject));

          if (removeObject &&
              javaLangObject.equals(type)) {
            final HashSet<PsiTypeVariable> cluster = myFactory.getClusterOf(var.getIndex());

            if (cluster != null) {
              for (final PsiTypeVariable war : cluster) {
                final PsiType wtype = b.apply(war);

                if (!javaLangObject.equals(wtype)) {
                  myBindings.put(index, type);
                  break;
                }
              }
            }
          }
          else {
            myBindings.put(index, type);
          }
        }
      }
    }

    public HashSet<PsiTypeVariable> getBoundVariables() {
      return myBoundVariables;
    }

    public int getWidth() {
      int w = 0;

      for (PsiType type1 : myBindings.values()) {
        final PsiType type = substitute(type1);

        if (type != null) {
          w++;
        }
      }

      return w;
    }

    public boolean isValid() {
      for (final PsiTypeVariable var : myBoundVariables) {
        final PsiType type = substitute(var);

        if (!var.isValidInContext(type)) {
          return false;
        }
      }

      return true;
    }

    public void addTypeVariable(final PsiTypeVariable var) {
      myBoundVariables.add(var);
    }

    public PsiType substitute(final PsiType t) {
      if (t instanceof PsiWildcardType) {
        final PsiWildcardType wcType = (PsiWildcardType)t;
        final PsiType bound = wcType.getBound();

        if (bound == null) {
          return t;
        }

        final PsiManager manager = PsiManager.getInstance(myProject);
        final PsiType subst = substitute(bound);
        if (subst == null) return null;
        return subst instanceof PsiWildcardType ? subst : wcType.isExtends()
                                                          ? PsiWildcardType.createExtends(manager, subst)
                                                          : PsiWildcardType.createSuper(manager, subst);
      }
      else if (t instanceof PsiTypeVariable) {
        final PsiType b = apply(t);

        if (b instanceof Bottom || b instanceof PsiTypeVariable) {
          return null;
        }

        return substitute(b);
      }
      else if (t instanceof Bottom) {
        return null;
      }
      else if (t instanceof PsiArrayType) {
        return substitute(((PsiArrayType)t).getComponentType()).createArrayType();
      }
      else if (t instanceof PsiClassType) {
        final PsiClassType.ClassResolveResult result = ((PsiClassType)t).resolveGenerics();

        final PsiClass aClass = result.getElement();
        final PsiSubstitutor aSubst = result.getSubstitutor();

        if (aClass != null) {
          PsiSubstitutor theSubst = PsiSubstitutor.EMPTY;

          for (final PsiTypeParameter parm : aSubst.getSubstitutionMap().keySet()) {
            final PsiType type = aSubst.substitute(parm);

            theSubst = theSubst.put(parm, substitute(type));
          }

          return aClass.getManager().getElementFactory().createType(aClass, theSubst);
        }
      }
      return t;
    }
  }

  interface Balancer {
    Binding varType(PsiTypeVariable x, PsiType y);

    Binding varVar(PsiTypeVariable x, PsiTypeVariable y);

    Binding typeVar(PsiType x, PsiTypeVariable y);
  }

  interface Unifier {
    Binding unify(PsiType x, PsiType y);
  }

  public Binding balance(final PsiType x, final PsiType y, final Balancer balancer, final HashSet<Constraint> constraints) {
    final int indicator = (x instanceof PsiTypeVariable ? 1 : 0) + (y instanceof PsiTypeVariable ? 2 : 0);

    switch (indicator) {
    case 0:
         if (x instanceof PsiWildcardType || y instanceof PsiWildcardType) {
           final PsiType xType = x instanceof PsiWildcardType ? ((PsiWildcardType)x).getBound() : x;
           final PsiType yType = y instanceof PsiWildcardType ? ((PsiWildcardType)y).getBound() : y;

           switch ((x instanceof PsiWildcardType ? 1 : 0) + (y instanceof PsiWildcardType ? 2 : 0)) {
           case 1:
                if (((PsiWildcardType)x).isExtends()) {
                  /* ? extends T1, T2 */
                  return null;
                }
                else {
                  /* ? super T1, T2 */
                  if (xType != null && !"java.lang.Object".equals(xType.getCanonicalText())) {
                    return null;
                  }
                  return create();
                }

           case 2:
                if (((PsiWildcardType)y).isExtends()) {
                  /* T1, ? extends T2 */
                 if (yType instanceof PsiTypeVariable) {
                   final PsiTypeVariable beta = myFactory.create();

                   if (constraints != null) {
                     constraints.add (new Subtype(beta, yType));
                     if (x != null) {
                       constraints.add (new Subtype(x, yType));
                     }
                   }

                   return create ();
                  }
                  else {
                    if (constraints != null && xType != null && yType != null) {
                      constraints.add(new Subtype(xType, yType));
                    }

                    return balance(xType, yType, balancer, constraints);
                  }
                }
                else {/* T1, ? super T2 */
                  if (yType instanceof PsiTypeVariable) {
                    final PsiTypeVariable beta = myFactory.create();

                    if (x != null && constraints != null) constraints.add (new Subtype(x, beta));
                    constraints.add (new Subtype(yType, beta));

                    return create();
                  }
                  else {
                    if (constraints != null && yType != null && xType != null) {
                      constraints.add(new Subtype(yType, xType));
                    }

                    return balance(xType, yType, balancer, constraints);
                  }
                }

           case 3:
                switch ((((PsiWildcardType)x).isExtends() ? 0 : 1) + (((PsiWildcardType)y).isExtends() ? 0 : 2)) {
                case 0: /* ? super T1, ? super T2 */
                     if (constraints != null && xType != null && yType != null) {
                       constraints.add(new Subtype(yType, xType));
                     }
                     return balance(xType, yType, balancer, constraints);

                case 1: /* ? extends T1, ? super T2 */
                     if (constraints != null && xType != null && yType != null) {
                       constraints.add(new Subtype(xType, yType));
                     }
                     return balance(xType, yType, balancer, constraints);

                case 2: /* ? super T1, ? extends T2*/
                     return null;

                case 3: /* ? extends T1, ? extends T2*/
                     if (constraints != null && xType != null && yType != null) {
                       constraints.add(new Subtype(xType, yType));
                     }
                     return balance(xType, yType, balancer, constraints);
                }
           }

           return create();
         }
         else if (x instanceof PsiArrayType || y instanceof PsiArrayType) {
           final PsiType xType = x instanceof PsiArrayType ? ((PsiArrayType)x).getComponentType() : x;
           final PsiType yType = y instanceof PsiArrayType ? ((PsiArrayType)y).getComponentType() : y;

           return balance(xType, yType, balancer, constraints);
         }
         else if (x instanceof PsiClassType && y instanceof PsiClassType) {
           final PsiClassType.ClassResolveResult resultX = Util.resolveType(x);
           final PsiClassType.ClassResolveResult resultY = Util.resolveType(y);

           final PsiClass xClass = resultX.getElement();
           final PsiClass yClass = resultY.getElement();

           if (xClass != null && yClass != null) {
             final PsiSubstitutor ySubst = resultY.getSubstitutor();

             PsiSubstitutor xSubst = TypeConversionUtil.getClassSubstitutor(yClass, xClass, resultX.getSubstitutor());
             if (xSubst == null) return null;

             Binding b = create();

             for (final PsiTypeParameter aParm : xSubst.getSubstitutionMap().keySet()) {
               final PsiType xType = xSubst.substitute(aParm);
               final PsiType yType = ySubst.substitute(aParm);

               final Binding b1 = unify(xType, yType, new Unifier() {
                 public Binding unify(final PsiType x, final PsiType y) {
                   return balance(x, y, balancer, constraints);
                 }
               });

               if (b1 == null) {
                 return null;
               }

               b = b.compose(b1);
             }

             return b;
           }
         }
         else if (y instanceof Bottom) {
           return create();
         }
         else {
           return null;
         }
    break;

    case 1:
         return balancer.varType((PsiTypeVariable)x, y);

    case 2:
         return balancer.typeVar(x, (PsiTypeVariable)y);

    case 3:
         return balancer.varVar((PsiTypeVariable)x, (PsiTypeVariable)y);
    }

    return null;
  }

  private Binding unify(final PsiType x, final PsiType y, final Unifier unifier) {
    final int indicator = (x instanceof PsiTypeVariable ? 1 : 0) + (y instanceof PsiTypeVariable ? 2 : 0);

    switch (indicator) {
    case 0:
           if (x instanceof PsiWildcardType || y instanceof PsiWildcardType) {
             return unifier.unify(x, y);
           }
           else if (x instanceof PsiArrayType || y instanceof PsiArrayType) {
             final PsiType xType = x instanceof PsiArrayType ? ((PsiArrayType)x).getComponentType() : x;
             final PsiType yType = y instanceof PsiArrayType ? ((PsiArrayType)y).getComponentType() : y;

             return unify(xType, yType, unifier);
           }
           else if (x instanceof PsiClassType && y instanceof PsiClassType) {
             final PsiClassType.ClassResolveResult resultX = Util.resolveType(x);
             final PsiClassType.ClassResolveResult resultY = Util.resolveType(y);

             final PsiClass xClass = resultX.getElement();
             final PsiClass yClass = resultY.getElement();

             if (xClass != null && yClass != null) {
               final PsiSubstitutor ySubst = resultY.getSubstitutor();

               final PsiSubstitutor xSubst = resultX.getSubstitutor();

               if (!xClass.equals(yClass)) {
                 return null;
               }

               Binding b = create();

               for (final PsiTypeParameter aParm : xSubst.getSubstitutionMap().keySet()) {
                 final PsiType xType = xSubst.substitute(aParm);
                 final PsiType yType = ySubst.substitute(aParm);

                 final Binding b1 = unify(xType, yType, unifier);

                 if (b1 == null) {
                   return null;
                 }

                 b = b.compose(b1);
               }

               return b;
             }
           }
           else if (y instanceof Bottom) {
             return create();
           }
           else {
             return null;
           }

    default:
           return unifier.unify(x, y);
    }
  }

  public Binding riseWithWildcard(final PsiType x, final PsiType y, final HashSet<Constraint> constraints) {
    final Binding binding = balance(x, y, new Balancer() {
                                      public Binding varType(final PsiTypeVariable x, final PsiType y) {
                                        if (y instanceof Bottom) {
                                          return create();
                                        }

                                        if (y == null || y instanceof PsiWildcardType) {
                                          return null;
                                        }

                                        final PsiTypeVariable var = myFactory.create();
                                        final Binding binding =
                                        create(x, PsiWildcardType.createSuper(PsiManager.getInstance(myProject), var));

                                        binding.addTypeVariable(var);
                                        constraints.add(new Subtype(var, y));

                                        return binding;
                                      }

                                      public Binding varVar(final PsiTypeVariable x, final PsiTypeVariable y) {
                                        final int xi = x.getIndex();
                                        final int yi = y.getIndex();

                                        if (xi == yi)
                                          return create ();

                                        return create (y, PsiWildcardType.createExtends(PsiManager.getInstance(myProject), x));
                                        /* if (xi < yi) {
                                         return create(x, y);
                                       }
                                       else if (yi < xi) {
                                         return create(y, x);
                                       }
                                       else {
                                         return create();
                                       } */
                                      }

                                      public Binding typeVar(final PsiType x, final PsiTypeVariable y) {
                                        if (x == null) {
                                          return create(y, Bottom.BOTTOM);
                                        }

                                        if (x instanceof PsiWildcardType) {
                                          return null;
                                        }

                                        final PsiTypeVariable var = myFactory.create();
                                        final Binding binding =
                                        create(y, PsiWildcardType.createExtends(PsiManager.getInstance(myProject), var));

                                        binding.addTypeVariable(var);
                                        constraints.add(new Subtype(x, var));

                                        return binding;
                                      }
                                    }, constraints);

    return binding != null ? binding.reduceRecursive() : null;
  }

  public Binding rise(final PsiType x, final PsiType y, final HashSet<Constraint> constraints) {
    final Binding binding = balance(x, y, new Balancer() {
                                      public Binding varType(final PsiTypeVariable x, final PsiType y) {
                                        if (y instanceof Bottom) {
                                          return create();
                                        }

                                        return create(x, y);
                                      }

                                      public Binding varVar(final PsiTypeVariable x, final PsiTypeVariable y) {
                                        final int xi = x.getIndex();
                                        final int yi = y.getIndex();

                                        if (xi < yi) {
                                          return create(x, y);
                                        }
                                        else if (yi < xi) {
                                          return create(y, x);
                                        }
                                        else {
                                          return create();
                                        }
                                      }

                                      public Binding typeVar(final PsiType x, final PsiTypeVariable y) {
                                        if (x == null) {
                                          return create(y, Bottom.BOTTOM);
                                        }

                                        return create(y, x);
                                      }
                                    }, constraints);

    return binding != null ? binding.reduceRecursive() : null;
  }

  public Binding sink(final PsiType x, final PsiType y, final HashSet<Constraint> constraints) {
    return balance(x, y, new Balancer() {
                     public Binding varType(final PsiTypeVariable x, final PsiType y) {
                       return create(x, y);
                     }

                     public Binding varVar(final PsiTypeVariable x, final PsiTypeVariable y) {
                       return create(y, Bottom.BOTTOM);
                     }

                     public Binding typeVar(final PsiType x, final PsiTypeVariable y) {
                       return create(y, Bottom.BOTTOM);
                     }
                   }, constraints);
  }

  public LinkedList<Pair<PsiType, Binding>> union(final PsiType x, final PsiType y) {
    final LinkedList<Pair<PsiType, Binding>> list = new LinkedList<Pair<PsiType, Binding>>();

    new Object() {
      void union(final PsiType x, final PsiType y, final LinkedList<Pair<PsiType, Binding>> list) {
        if (x instanceof PsiArrayType && y instanceof PsiArrayType) {
          union(((PsiArrayType)x).getComponentType(), ((PsiArrayType)y).getComponentType(), list);
        }
        else if (x instanceof PsiClassType && y instanceof PsiClassType) {
          final PsiClassType.ClassResolveResult xResult = Util.resolveType(x);
          final PsiClassType.ClassResolveResult yResult = Util.resolveType(y);

          final PsiClass xClass = xResult.getElement();
          final PsiClass yClass = yResult.getElement();

          final PsiSubstitutor xSubst = xResult.getSubstitutor();
          final PsiSubstitutor ySubst = yResult.getSubstitutor();

          if (xClass == null || yClass == null) {
            return;
          }

          if (xClass.equals(yClass)) {
            final Binding risen = rise(x, y, null);

            if (risen == null) {
              return;
            }

            list.addFirst(new Pair<PsiType, Binding>(risen.apply(x), risen));
          }
          else {
            final PsiClass[] descendants = getGreatestLowerClasses(xClass, yClass);

            for (int i = 0; i < descendants.length; i++) {
              final PsiClass descendant = descendants[i];

              final PsiSubstitutor x2aSubst = TypeConversionUtil.getClassSubstitutor(xClass, descendant, xSubst);
              final PsiSubstitutor y2aSubst = TypeConversionUtil.getClassSubstitutor(yClass, descendant, ySubst);
              LOG.assertTrue(x2aSubst != null && y2aSubst != null);

              final PsiElementFactory factory = xClass.getManager().getElementFactory();

              union(factory.createType(descendant, x2aSubst), factory.createType(descendant, y2aSubst), list);
            }
          }
        }
      }
    }.union(x, y, list);

    return list;
  }

  public LinkedList<Pair<PsiType, Binding>> intersect(final PsiType x, final PsiType y) {
    final LinkedList<Pair<PsiType, Binding>> list = new LinkedList<Pair<PsiType, Binding>>();

    new Object() {
      void intersect(final PsiType x, final PsiType y, final LinkedList<Pair<PsiType, Binding>> list) {
        if (x instanceof PsiWildcardType || y instanceof PsiWildcardType) {
          final PsiType xType = x instanceof PsiWildcardType ? ((PsiWildcardType)x).getBound() : x;
          final PsiType yType = y instanceof PsiWildcardType ? ((PsiWildcardType)y).getBound() : y;

          intersect(xType, yType, list);
        }
        if (x instanceof PsiArrayType || y instanceof PsiArrayType) {
          if (x instanceof PsiClassType || y instanceof PsiClassType) {
            try {
              final PsiElementFactory f = PsiManager.getInstance(myProject).getElementFactory();
              final PsiType keyType = x instanceof PsiClassType ? x : y;

              final PsiType object = f.createTypeFromText("java.lang.Object", null);
              final PsiType cloneable = f.createTypeFromText("java.lang.Cloneable", null);
              final PsiType serializable = f.createTypeFromText("java.io.Serializable", null);

              intersect(keyType, object, list);
              intersect(keyType, cloneable, list);
              intersect(keyType, serializable, list);
            }
            catch (IncorrectOperationException e) {
              LOG.error("Exception " + e);
            }
          }
          else if (x instanceof PsiArrayType && y instanceof PsiArrayType) {
            intersect(((PsiArrayType)x).getComponentType(), ((PsiArrayType)y).getComponentType(), list);
          }
        }
        else if (x instanceof PsiClassType && y instanceof PsiClassType) {
          final PsiClassType.ClassResolveResult xResult = Util.resolveType(x);
          final PsiClassType.ClassResolveResult yResult = Util.resolveType(y);

          final PsiClass xClass = xResult.getElement();
          final PsiClass yClass = yResult.getElement();

          final PsiSubstitutor xSubst = xResult.getSubstitutor();
          final PsiSubstitutor ySubst = yResult.getSubstitutor();

          if (xClass == null || yClass == null) {
            return;
          }

          if (xClass.equals(yClass)) {
            final Binding risen = rise(x, y, null);

            if (risen == null) {
              final PsiElementFactory factory = xClass.getManager().getElementFactory();

              list.addFirst(new Pair<PsiType, Binding>(Util.banalize(factory.createType(xClass, factory.createRawSubstitutor(xClass))),
                                                       create()));
            }
            else {
              list.addFirst(new Pair<PsiType, Binding>(risen.apply(x), risen));
            }
          }
          else {
            final PsiClass[] ancestors = GenericsUtil.getLeastUpperClasses(xClass, yClass);

            for (int i = 0; i < ancestors.length; i++) {
              final PsiClass ancestor = ancestors[i];

              if ("java.lang.Object".equals(ancestor.getQualifiedName()) && ancestors.length > 1) {
              continue;
              }

              final PsiSubstitutor x2aSubst = TypeConversionUtil.getSuperClassSubstitutor(ancestor, xClass, xSubst);
              final PsiSubstitutor y2aSubst = TypeConversionUtil.getSuperClassSubstitutor(ancestor, yClass, ySubst);

              final PsiElementFactory factory = xClass.getManager().getElementFactory();

              intersect(factory.createType(ancestor, x2aSubst), factory.createType(ancestor, y2aSubst), list);
            }
          }
        }
      }
    }.intersect(x, y, list);

    return list;
  }

  public BindingFactory(final com.intellij.refactoring.typeCook.deductive.builder.System system) {
    myBoundVariables = system.getBoundVariables();
    myProject = system.getProject();
    myFactory = system.getVariableFactory();
  }

  public Binding create(final PsiTypeVariable var, final PsiType type) {
    return new BindingImpl(var, type);
  }

  public Binding create() {
    return new BindingImpl();
  }

  public HashSet<PsiTypeVariable> getBoundVariables() {
    return myBoundVariables;
  }
}