package org.jetbrains.jet.codegen;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.jet.lexer.JetTokens;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

/**
 * @author yole
 */
public abstract class StackValue {
    protected final Type type;

    public StackValue(Type type) {
        this.type = type;
    }

    public abstract void put(Type type, InstructionAdapter v);

    public static StackValue local(int index, Type type) {
        return new Local(index, type);
    }

    public static StackValue onStack(Type type) {
        assert type != Type.VOID_TYPE;
        return new OnStack(type);
    }

    public static StackValue constant(Object value, Type type) {
        return new Constant(value, type);
    }

    public static StackValue cmp(IElementType opToken) {
        return new NumberCompare(opToken);
    }

    public static StackValue not(StackValue stackValue) {
        return new Invert(stackValue);
    }

    public abstract void condJump(Label label, boolean jumpIfFalse, InstructionAdapter v);

    private static void box(final Type type, InstructionAdapter v) {
        if (type == Type.INT_TYPE) {
            v.invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        }
        else if (type == Type.BOOLEAN_TYPE) {
            v.invokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        }
        else if (type == Type.CHAR_TYPE) {
            v.invokestatic("java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
        }
        else if (type == Type.SHORT_TYPE) {
            v.invokestatic("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
        }
        else if (type == Type.LONG_TYPE) {
            v.invokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        }
        else if (type == Type.BYTE_TYPE) {
            v.invokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
        }
        else if (type == Type.FLOAT_TYPE) {
            v.invokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
        }
        else if (type == Type.DOUBLE_TYPE) {
            v.invokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        }
    }

    protected void coerce(Type type, InstructionAdapter v) {
        if (type.getSort() == Type.OBJECT) {
            box(this.type, v);
        }
        else if (type != this.type) {
            v.cast(this.type, type);
        }
    }

    protected void putAsBoolean(InstructionAdapter v) {
        Label ifTrue = new Label();
        Label end = new Label();
        condJump(ifTrue, false, v);
        v.iconst(0);
        v.goTo(end);
        v.mark(ifTrue);
        v.iconst(1);
        v.mark(end);
    }

    public static class Local extends StackValue {
        private final int index;

        public Local(int index, Type type) {
            super(type);
            this.index = index;
        }

        @Override
        public void put(Type type, InstructionAdapter v) {
            v.load(index, this.type);
            coerce(type, v);
            // TODO unbox
        }

        @Override
        public void condJump(Label label, boolean jumpIfFalse, InstructionAdapter v) {
            put(Type.INT_TYPE, v);
            if (jumpIfFalse) {
                v.ifeq(label);
            }
            else {
                v.ifne(label);
            }
        }
    }

    public static class OnStack extends StackValue {
        public OnStack(Type type) {
            super(type);
        }

        @Override
        public void put(Type type, InstructionAdapter v) {
            coerce(type, v);
        }

        @Override
        public void condJump(Label label, boolean jumpIfFalse, InstructionAdapter v) {
            if (this.type == Type.BOOLEAN_TYPE) {
                if (jumpIfFalse) {
                    v.ifeq(label);
                }
                else {
                    v.ifne(label);
                }
            }
            else {
                throw new UnsupportedOperationException("can't generate a cond jump for a non-boolean value on stack");
            }
        }
    }

    public static class Constant extends StackValue {
        private final Object value;

        public Constant(Object value, Type type) {
            super(type);
            this.value = value;
        }

        @Override
        public void put(Type type, InstructionAdapter v) {
            v.aconst(value);
            coerce(type, v);
        }

        @Override
        public void condJump(Label label, boolean jumpIfFalse, InstructionAdapter v) {
            if (value instanceof Boolean) {
                boolean boolValue = ((Boolean) value).booleanValue();
                if (boolValue ^ jumpIfFalse) {
                    v.goTo(label);
                }
            }
            else {
                throw new UnsupportedOperationException("don't know how to generate this condjump");
            }
        }
    }

    private static class NumberCompare extends StackValue {
        private final IElementType opToken;

        public NumberCompare(IElementType opToken) {
            super(Type.BOOLEAN_TYPE);
            this.opToken = opToken;
        }

        @Override
        public void put(Type type, InstructionAdapter v) {
            if (type != Type.BOOLEAN_TYPE) {
                throw new UnsupportedOperationException("don't know how to put a compare as a non-boolean type");
            }
            putAsBoolean(v);
        }

        @Override
        public void condJump(Label label, boolean jumpIfFalse, InstructionAdapter v) {
            int opcode;
            if (opToken == JetTokens.EQEQ) {
                opcode = jumpIfFalse ? Opcodes.IFNE : Opcodes.IFEQ;
            }
            else if (opToken == JetTokens.EXCLEQ) {
                opcode = jumpIfFalse ? Opcodes.IFEQ : Opcodes.IFNE;
            }
            else if (opToken == JetTokens.GT) {
                opcode = jumpIfFalse ? Opcodes.IFLE : Opcodes.IFGT;
            }
            else if (opToken == JetTokens.GTEQ) {
                opcode = jumpIfFalse ? Opcodes.IFLT : Opcodes.IFGE;
            }
            else if (opToken == JetTokens.LT) {
                opcode = jumpIfFalse ? Opcodes.IFGE : Opcodes.IFLT;
            }
            else if (opToken == JetTokens.LTEQ) {
                opcode = jumpIfFalse ? Opcodes.IFGT : Opcodes.IFLE;
            }
            else {
                throw new UnsupportedOperationException("don't know how to generate this condjump");
            }
            if (type == Type.FLOAT_TYPE || type == Type.DOUBLE_TYPE) {
                if (opToken == JetTokens.GT || opToken == JetTokens.GTEQ) {
                    v.cmpg(type);
                }
                else {
                    v.cmpl(type);
                }
            }
            else if (type == Type.LONG_TYPE) {
                v.lcmp();
            }
            else {
                opcode += (Opcodes.IF_ICMPEQ - Opcodes.IFEQ);
            }
            v.visitJumpInsn(opcode, label);
        }
    }

    private static class Invert extends StackValue {
        private StackValue myOperand;

        private Invert(StackValue operand) {
            super(operand.type);
            myOperand = operand;
        }

        @Override
        public void put(Type type, InstructionAdapter v) {
            if (type != Type.BOOLEAN_TYPE) {
                throw new UnsupportedOperationException("don't know how to put a compare as a non-boolean type");
            }
            putAsBoolean(v);
        }

        @Override
        public void condJump(Label label, boolean jumpIfFalse, InstructionAdapter v) {
            myOperand.condJump(label, !jumpIfFalse, v);
        }
    }
}