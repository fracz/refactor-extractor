package com.googlecode.dex2jar.v3;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.CastExpr;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.NewExpr;
import com.googlecode.dex2jar.ir.expr.NewMutiArrayExpr;
import com.googlecode.dex2jar.ir.expr.RefExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.LiveAnalyze;
import com.googlecode.dex2jar.ir.ts.LiveAnalyze.Phi;
import com.googlecode.dex2jar.ir.ts.LocalType;

public class IrMethod2AsmMethod implements Opcodes {

    private static final boolean DEBUG = false;

    private void reIndexLocal(IrMethod ir) {

        if (DEBUG) {
            int i = 0;
            for (Stmt stmt = ir.stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
                if (stmt.st == ST.LABEL) {
                    LabelStmt ls = (LabelStmt) stmt;
                    ls.displayName = "L" + i++;
                }
            }
        }

        // 1. live local analyze
        LiveAnalyze la = new LiveAnalyze(ir);
        List<Phi> phis = la.analyze();
        if (DEBUG) {
            for (Local local : ir.locals) {
                if (local._ls_index >= 0) {
                    local.name = "a" + local._ls_index;
                } else {
                    local.name = "ignore";
                }
            }
        }

        // 2.1. assign existing index
        int index = 0;
        if ((ir.access & ACC_STATIC) == 0) {
            index++;
        }
        final int localSize = ir.locals.size();
        final int parameterIdx[] = new int[ir.args.length];
        for (int i = 0; i < ir.args.length; i++) {
            parameterIdx[i] = index;
            index += ir.args[i].getSize();
        }
        int[] maps = new int[localSize];
        for (int i = 0; i < localSize; i++) {
            maps[i] = -1;
        }
        Local _thisLocal = null;
        Local _parameterLocals[] = new Local[ir.args.length];
        for (Stmt stmt = ir.stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
            if (stmt.st == ST.IDENTITY || stmt.st == ST.ASSIGN) {
                E2Stmt e2 = (E2Stmt) stmt;
                switch (e2.op2.value.vt) {
                case THIS_REF: {
                    Local local = (Local) e2.op1.value;
                    maps[local._ls_index] = 0;
                    _thisLocal = local;
                }
                    break;
                case PARAMETER_REF: {
                    Local local = (Local) e2.op1.value;
                    int i = ((RefExpr) e2.op2.value).parameterIndex;
                    maps[local._ls_index] = parameterIdx[i];
                    _parameterLocals[i] = local;
                }
                    break;
                }
            }
        }

        // 2.2 assign other index
        createGraph(ir, phis.size());
        {// never reuse `this` and parameters index
            if ((ir.access & ACC_STATIC) == 0 && _thisLocal != null) {
                markPhiNeverReuse(phis, _thisLocal);
            }
            for (Local local : _parameterLocals) {
                if (local != null) {
                    markPhiNeverReuse(phis, local);
                }
            }
        }
        gradyColoring(phis, maps);

        for (Local local : ir.locals) {
            local._ls_index = maps[local._ls_index];
            if (DEBUG) {
                if (local._ls_index >= 0) {
                    local.name = "a" + local._ls_index;
                } else {
                    local.name = "ignore";
                }
            }
        }
    }

    private void markPhiNeverReuse(List<Phi> phis, Local local) {
        Phi nPhi = null;
        for (Phi phi : phis) {
            if (phi.local == local) {
                nPhi = phi;
                break;
            }
        }
        if (nPhi == null) {
            nPhi = new Phi();
            nPhi.local = local;
        }
        for (Phi phi : phis) {
            if (phi != nPhi) {
                phi.sets.add(nPhi);
                nPhi.sets.add(phi);
            }
        }
    }

    private int findNextColor(Phi v, int n, int max, int[] maps) {
        BitSet bs = new BitSet(max);
        bs.set(0, max);
        for (Phi one : v.sets) {
            int x = maps[one.local._ls_index];
            if (x >= 0) {
                bs.clear(x);
                if (sizeOf(one) > 1) {
                    bs.clear(x + 1);
                }
            }
        }
        boolean wide = sizeOf(v) > 1;
        for (int i = bs.nextSetBit(n); i >= 0; i = bs.nextSetBit(i + 1)) {
            if (wide) {
                if (i + 1 < bs.length() && bs.get(i + 1)) {
                    return i;
                }
            } else {
                return i;
            }
        }
        return -1;
    }

    private void gradyColoring(List<Phi> phis, int[] maps) {
        if (phis.size() <= 0) {
            return;
        }
        // sort
        Collections.sort(phis, new Comparator<Phi>() {
            @Override
            public int compare(Phi o1, Phi o2) {
                int r = o2.sets.size() - o1.sets.size();
                return r == 0 ? sizeOf(o2) - sizeOf(o1) : r;
            }
        });
        Phi first = phis.get(0);
        int size = sizeOf(first);
        for (Phi p : first.sets) {
            size += sizeOf(p);
        }

        BitSet toColor = new BitSet(phis.size());
        for (int i = 0; i < phis.size(); i++) {
            Phi p = phis.get(i);
            if (maps[p.local._ls_index] < 0) {
                toColor.set(i);
            }
        }

        while (!doColor(0, toColor, phis, size, maps)) {
            size += 1;
        }
    }

    private boolean doColor(int idx, BitSet toColor, List<Phi> phis, int size, int[] maps) {
        int x = toColor.nextSetBit(idx);
        if (x < 0) {
            return true;
        }
        Phi phi = phis.get(x);
        for (int i = findNextColor(phi, 0, size, maps); i >= 0; i = findNextColor(phi, i + 1, size, maps)) {
            maps[phi.local._ls_index] = i;
            if (doColor(x + 1, toColor, phis, size, maps)) {
                return true;
            }
            maps[phi.local._ls_index] = -1;
        }
        return false;
    }

    private static int sizeOf(Phi p) {
        return LocalType.typeOf(p.local).getSize();
    }

    private void createGraph(IrMethod ir, int localSize) {

        List<Phi> tmp = new ArrayList<Phi>(localSize);
        for (Stmt p = ir.stmts.getFirst(); p != null; p = p.getNext()) {
            tmp.clear();
            {
                Phi[] frame = (Phi[]) p._ls_forward_frame;
                p._ls_forward_frame = null;
                if (frame != null) {
                    for (int i = 0; i < frame.length; i++) {
                        Phi r = frame[i];
                        if (r != null) {
                            tmp.add(r);
                        }
                    }
                }
            }
            for (int i = 0; i < tmp.size() - 1; i++) {
                Phi a = tmp.get(i);
                for (int j = i + 1; j < tmp.size(); j++) {
                    Phi b = tmp.get(j);
                    if (a != b) {
                        a.sets.add(b);
                        b.sets.add(a);
                    }
                }
            }
        }

    }

    public void convert(IrMethod ir, MethodVisitor asm) {
        reIndexLocal(ir);
        reBuildInstructions(ir, asm);
        reBuildTryCatchBlocks(ir, asm);
    }

    private void reBuildTryCatchBlocks(IrMethod ir, MethodVisitor asm) {
        for (Trap trap : ir.traps) {
            asm.visitTryCatchBlock(trap.start.label, trap.end.label, trap.handler.label, trap.type == null ? null
                    : trap.type.getInternalName());
        }
    }

    private void reBuildInstructions(IrMethod ir, MethodVisitor asm) {
        for (Stmt st : ir.stmts) {
            switch (st.st) {
            case LABEL:
                asm.visitLabel(((LabelStmt) st).label);
                break;
            case ASSIGN: {
                E2Stmt e2 = (E2Stmt) st;
                Value v1 = e2.op1.value;
                Value v2 = e2.op2.value;
                switch (v1.vt) {
                case LOCAL:

                    Local local = ((Local) v1);
                    int i = local._ls_index;

                    boolean skipOrg = false;
                    if (LocalType.typeOf(v1).equals(Type.INT_TYPE)) {// check for IINC
                        if (v2.vt == VT.ADD) {
                            E2Expr e = (E2Expr) v2;
                            if ((e.op1.value == local && e.op2.value.vt == VT.CONSTANT)
                                    || (e.op2.value == local && e.op1.value.vt == VT.CONSTANT)) {
                                int increment = (Integer) ((Constant) (e.op1.value == local ? e.op2.value : e.op1.value)).value;
                                asm.visitIincInsn(i, increment);
                                skipOrg = true;
                            }
                        } else if (v2.vt == VT.SUB) {
                            E2Expr e = (E2Expr) v2;
                            if (e.op1.value == local && e.op2.value.vt == VT.CONSTANT) {
                                int increment = -(Integer) ((Constant) e.op2.value).value;
                                asm.visitIincInsn(i, increment);
                                skipOrg = true;
                            }
                        }
                    }
                    if (!skipOrg) {
                        accept(v2, asm);
                        if (i >= 0) {// skip void type locals
                            if (local._ls_read_count == 0) {// no read, just pop it
                                asm.visitInsn(LocalType.typeOf(v1).getSize() == 2 ? POP2 : POP);
                            } else {
                                asm.visitVarInsn(LocalType.typeOf(v1).getOpcode(ISTORE), i);
                            }
                        } else if (!LocalType.typeOf(v1).equals(Type.VOID_TYPE)) {
                            asm.visitInsn(LocalType.typeOf(v1).getSize() == 2 ? POP2 : POP);
                        }
                    }
                    break;
                case FIELD:
                    FieldExpr fe = (FieldExpr) v1;
                    if (fe.op == null) {// static field
                        accept(v2, asm);
                        asm.visitFieldInsn(PUTSTATIC, fe.fieldOwnerType.getInternalName(), fe.fieldName,
                                fe.fieldType.getDescriptor());
                    } else {// virtual field
                        accept(fe.op.value, asm);
                        accept(v2, asm);
                        asm.visitFieldInsn(PUTFIELD, fe.fieldOwnerType.getInternalName(), fe.fieldName,
                                fe.fieldType.getDescriptor());
                    }
                    break;
                case ARRAY:
                    ArrayExpr ae = (ArrayExpr) v1;
                    accept(ae.op1.value, asm);
                    accept(ae.op2.value, asm);
                    accept(v2, asm);
                    Type tp1 = LocalType.typeOf(ae.op1.value);
                    Type tp2 = LocalType.typeOf(ae);
                    if (tp1.getSort() == Type.ARRAY) {
                        asm.visitInsn(Type.getType(tp1.getDescriptor().substring(1)).getOpcode(IASTORE));
                    } else {
                        asm.visitInsn(tp2.getOpcode(IASTORE));
                    }
                    break;
                }
            }
                break;
            case IDENTITY: {
                E2Stmt e2 = (E2Stmt) st;
                if (e2.op2.value.vt == VT.EXCEPTION_REF) {
                    int index = ((Local) e2.op1.value)._ls_index;
                    if (index >= 0) {
                        asm.visitVarInsn(ASTORE, index);
                    } else {
                        asm.visitInsn(POP);
                    }
                }
            }
                break;
            case GOTO:
                asm.visitJumpInsn(GOTO, ((JumpStmt) st).target.label);
                break;
            case IF:
                reBuildJumpInstructions((JumpStmt) st, asm);
                break;
            case LOCK:
                accept(((UnopStmt) st).op.value, asm);
                asm.visitInsn(MONITORENTER);
                break;
            case UNLOCK:
                accept(((UnopStmt) st).op.value, asm);
                asm.visitInsn(MONITOREXIT);
                break;
            case NOP:
                break;
            case RETURN: {
                Value v = ((UnopStmt) st).op.value;
                accept(v, asm);
                asm.visitInsn(LocalType.typeOf(v).getOpcode(IRETURN));
            }
                break;
            case RETURN_VOID:
                asm.visitInsn(RETURN);
                break;
            case LOOKUP_SWITCH: {
                LookupSwitchStmt lss = (LookupSwitchStmt) st;
                accept(lss.op.value, asm);
                Label targets[] = new Label[lss.targets.length];
                for (int i = 0; i < targets.length; i++) {
                    targets[i] = lss.targets[i].label;
                }
                asm.visitLookupSwitchInsn(lss.defaultTarget.label, lss.lookupValues, targets);
            }
                break;
            case TABLE_SWITCH: {
                TableSwitchStmt tss = (TableSwitchStmt) st;
                accept(tss.op.value, asm);
                Label targets[] = new Label[tss.targets.length];
                for (int i = 0; i < targets.length; i++) {
                    targets[i] = tss.targets[i].label;
                }
                asm.visitTableSwitchInsn(tss.lowIndex, tss.highIndex, tss.defaultTarget.label, targets);
            }
                break;
            case THROW:
                accept(((UnopStmt) st).op.value, asm);
                asm.visitInsn(ATHROW);
                break;
            }
        }
    }

    private void reBuildJumpInstructions(JumpStmt st, MethodVisitor asm) {

        Label target = st.target.label;
        Value v = st.op.value;
        Value v1 = ((E2Expr) v).op1.value;
        Value v2 = ((E2Expr) v).op2.value;

        Type type = LocalType.typeOf(v1);

        switch (type.getSort()) {
        case Type.INT:
        case Type.BYTE:
        case Type.CHAR:
        case Type.SHORT:
        case Type.BOOLEAN:
            // IFx
            // IF_ICMPx
            if ((v1.vt == VT.CONSTANT && ((Constant) v1).value.equals(new Integer(0)))
                    || (v2.vt == VT.CONSTANT && ((Constant) v2).value.equals(new Integer(0)))) { // IFx
                if ((v2.vt == VT.CONSTANT && ((Constant) v2).value.equals(new Integer(0)))) {// v2 is zero
                    accept(v1, asm);
                } else {
                    accept(v2, asm);
                }
                switch (v.vt) {
                case NE:
                    asm.visitJumpInsn(IFNE, target);
                    break;
                case EQ:
                    asm.visitJumpInsn(IFEQ, target);
                    break;
                case GE:
                    asm.visitJumpInsn(IFGE, target);
                    break;
                case GT:
                    asm.visitJumpInsn(IFGT, target);
                    break;
                case LE:
                    asm.visitJumpInsn(IFLE, target);
                    break;
                case LT:
                    asm.visitJumpInsn(IFLT, target);
                    break;
                }
            } else { // IF_ICMPx
                accept(v1, asm);
                accept(v2, asm);
                switch (v.vt) {
                case NE:
                    asm.visitJumpInsn(IF_ICMPNE, target);
                    break;
                case EQ:
                    asm.visitJumpInsn(IF_ICMPEQ, target);
                    break;
                case GE:
                    asm.visitJumpInsn(IF_ICMPGE, target);
                    break;
                case GT:
                    asm.visitJumpInsn(IF_ICMPGT, target);
                    break;
                case LE:
                    asm.visitJumpInsn(IF_ICMPLE, target);
                    break;
                case LT:
                    asm.visitJumpInsn(IF_ICMPLT, target);
                    break;
                }
            }

            break;
        case Type.ARRAY:
        case Type.OBJECT:
            // IF_ACMPx
            // IF[non]null

            if ((v1.vt == VT.CONSTANT && ((Constant) v1).value.equals(Constant.Null))
                    || (v2.vt == VT.CONSTANT && ((Constant) v2).value.equals(Constant.Null))) { // IF[non]null
                if ((v2.vt == VT.CONSTANT && ((Constant) v2).value.equals(Constant.Null))) {// v2 is null
                    accept(v1, asm);
                } else {
                    accept(v2, asm);
                }
                asm.visitJumpInsn(v.vt == VT.EQ ? IFNULL : IFNONNULL, target);
            } else {
                accept(v1, asm);
                accept(v2, asm);
                asm.visitJumpInsn(v.vt == VT.EQ ? IF_ACMPEQ : IF_ACMPNE, target);
            }
            break;
        }
    }

    private static void accept(Value value, MethodVisitor asm) {

        switch (value.et) {
        case E0:
            switch (value.vt) {
            case LOCAL:
                asm.visitVarInsn(LocalType.typeOf(value).getOpcode(ILOAD), ((Local) value)._ls_index);
                break;
            case CONSTANT:
                Constant cst = (Constant) value;
                if (cst.value.equals(Constant.Null)) {
                    asm.visitInsn(ACONST_NULL);
                } else {
                    asm.visitLdcInsn(cst.value);
                }
                break;
            case NEW:
                asm.visitTypeInsn(NEW, ((NewExpr) value).type.getInternalName());
                break;
            }
            break;
        case E1:
            reBuildE1Expression((E1Expr) value, asm);
            break;
        case E2:
            reBuildE2Expression((E2Expr) value, asm);
            break;
        case En:
            reBuildEnExpression((EnExpr) value, asm);
            break;
        }
    }

    private static void reBuildEnExpression(EnExpr value, MethodVisitor asm) {
        if (value.vt == VT.INVOKE_NEW) {
            asm.visitTypeInsn(NEW, ((InvokeExpr) value).methodOwnerType.getInternalName());
            asm.visitInsn(DUP);
        }

        for (ValueBox vb : value.ops) {
            accept(vb.value, asm);
        }
        switch (value.vt) {
        case NEW_MUTI_ARRAY:
            NewMutiArrayExpr nmae = (NewMutiArrayExpr) value;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nmae.dimension; i++) {
                sb.append('[');
            }
            sb.append(nmae.baseType.getDescriptor());
            asm.visitMultiANewArrayInsn(sb.toString(), nmae.dimension);
            break;
        case INVOKE_INTERFACE:
        case INVOKE_NEW:
        case INVOKE_SPECIAL:
        case INVOKE_STATIC:
        case INVOKE_VIRTUAL:
            InvokeExpr ie = (InvokeExpr) value;
            int opcode;
            switch (value.vt) {
            case INVOKE_VIRTUAL:
                opcode = INVOKEVIRTUAL;
                break;
            case INVOKE_INTERFACE:
                opcode = INVOKEINTERFACE;
                break;
            case INVOKE_NEW:
            case INVOKE_SPECIAL:
                opcode = INVOKESPECIAL;
                break;
            case INVOKE_STATIC:
                opcode = INVOKESTATIC;
                break;
            default:
                opcode = -1;
            }

            asm.visitMethodInsn(opcode, ie.methodOwnerType.getInternalName(), ie.methodName, Type.getMethodDescriptor(
                    ie.vt == VT.INVOKE_NEW ? Type.VOID_TYPE : ie.methodReturnType, ie.argmentTypes));
            break;
        }
    }

    private static void reBuildE1Expression(E1Expr e1, MethodVisitor asm) {
        if (e1.op != null) {// the op is null if GETSTATIC
            accept(e1.op.value, asm);
        }
        switch (e1.vt) {
        case FIELD:
            FieldExpr fe = (FieldExpr) e1;
            asm.visitFieldInsn(e1.op == null ? GETSTATIC : GETFIELD, fe.fieldOwnerType.getInternalName(), fe.fieldName,
                    fe.fieldType.getDescriptor());
            break;
        case NEW_ARRAY: {
            TypeExpr te = (TypeExpr) e1;
            switch (te.type.getSort()) {
            case Type.ARRAY:
            case Type.OBJECT:
                asm.visitTypeInsn(ANEWARRAY, te.type.getInternalName());
                break;
            default:
                int operand;
                switch (te.type.getSort()) {
                case Type.BOOLEAN:
                    operand = T_BOOLEAN;
                    break;
                case Type.BYTE:
                    operand = T_BYTE;
                    break;
                case Type.SHORT:
                    operand = T_SHORT;
                    break;
                case Type.CHAR:
                    operand = T_CHAR;
                    break;
                case Type.INT:
                    operand = T_INT;
                    break;
                case Type.FLOAT:
                    operand = T_FLOAT;
                    break;
                case Type.LONG:
                    operand = T_LONG;
                    break;
                case Type.DOUBLE:
                    operand = T_DOUBLE;
                    break;
                default:
                    operand = -1;
                }
                asm.visitIntInsn(NEWARRAY, operand);
                break;
            }
        }
            break;
        case CHECK_CAST:
        case INSTANCE_OF: {
            TypeExpr te = (TypeExpr) e1;
            asm.visitTypeInsn(e1.vt == VT.CHECK_CAST ? CHECKCAST : INSTANCEOF, te.type.getInternalName());
        }
            break;
        case CAST: {
            CastExpr te = (CastExpr) e1;
            cast2(LocalType.typeOf(e1.op.value), te.to, asm);
        }
            break;
        case LENGTH:
            asm.visitInsn(ARRAYLENGTH);
            break;
        case NEG:
            asm.visitInsn(LocalType.typeOf(e1).getOpcode(INEG));
            break;
        }
    }

    private static void reBuildE2Expression(E2Expr e2, MethodVisitor asm) {
        Type type = LocalType.typeOf(e2.op2.value);
        accept(e2.op1.value, asm);
        if ((e2.vt == VT.ADD || e2.vt == VT.SUB) && e2.op2.value.vt == VT.CONSTANT) {
            // [x + (-1)] to [x - 1]
            // [x - (-1)] to [x + 1]
            Constant constant = (Constant) e2.op2.value;
            Type t = LocalType.typeOf(constant);
            switch (t.getSort()) {
            case Type.SHORT:
            case Type.BYTE:
            case Type.INT: {
                int s = (Integer) constant.value;
                if (s < 0) {
                    asm.visitLdcInsn((int) -s);
                    asm.visitInsn(type.getOpcode(e2.vt == VT.ADD ? ISUB : IADD));
                    return;
                }
            }
                break;
            case Type.FLOAT: {
                float s = (Float) constant.value;
                if (s < 0) {
                    asm.visitLdcInsn((float) -s);
                    asm.visitInsn(type.getOpcode(e2.vt == VT.ADD ? ISUB : IADD));
                    return;
                }
            }
                break;
            case Type.LONG: {
                long s = (Long) constant.value;
                if (s < 0) {
                    asm.visitLdcInsn((long) -s);
                    asm.visitInsn(type.getOpcode(e2.vt == VT.ADD ? ISUB : IADD));
                    return;
                }
            }
                break;
            case Type.DOUBLE: {
                double s = (Double) constant.value;
                if (s < 0) {
                    asm.visitLdcInsn((double) -s);
                    asm.visitInsn(type.getOpcode(e2.vt == VT.ADD ? ISUB : IADD));
                    return;
                }
            }
                break;
            }
        }

        accept(e2.op2.value, asm);

        Type tp1 = LocalType.typeOf(e2.op1.value);
        switch (e2.vt) {
        case ARRAY:
            Type tp2 = LocalType.typeOf(e2);
            if (tp1.getSort() == Type.ARRAY) {
                asm.visitInsn(Type.getType(tp1.getDescriptor().substring(1)).getOpcode(IALOAD));
            } else {
                asm.visitInsn(tp2.getOpcode(IALOAD));
            }
            break;
        case ADD:
            asm.visitInsn(type.getOpcode(IADD));
            break;
        case SUB:
            asm.visitInsn(type.getOpcode(ISUB));
            break;
        case DIV:
            asm.visitInsn(type.getOpcode(IDIV));
            break;
        case MUL:
            asm.visitInsn(type.getOpcode(IMUL));
            break;
        case REM:
            asm.visitInsn(type.getOpcode(IREM));
            break;
        case AND:
            asm.visitInsn(type.getOpcode(IAND));
            break;
        case OR:
            asm.visitInsn(type.getOpcode(IOR));
            break;
        case XOR:
            asm.visitInsn(type.getOpcode(IXOR));
            break;

        case SHL:
            asm.visitInsn(tp1.getOpcode(ISHL));
            break;
        case SHR:
            asm.visitInsn(tp1.getOpcode(ISHR));
            break;
        case USHR:
            asm.visitInsn(tp1.getOpcode(IUSHR));
            break;
        case LCMP:
            asm.visitInsn(LCMP);
            break;
        case FCMPG:
            asm.visitInsn(FCMPG);
            break;
        case DCMPG:
            asm.visitInsn(DCMPG);
            break;
        case FCMPL:
            asm.visitInsn(FCMPL);
            break;
        case DCMPL:
            asm.visitInsn(DCMPL);
            break;
        }
    }

    private static void cast2(Type t1, Type t2, MethodVisitor asm) {
        if (t1.equals(t2)) {
            return;
        }

        // char 2
        // byte 3
        // short 4
        // int 5
        // float 6
        // long 7
        // double 8

        // int I2L = 133; // visitInsn
        // int I2F = 134; // -
        // int I2D = 135; // -
        // int L2I = 136; // -
        // int L2F = 137; // -
        // int L2D = 138; // -
        // int F2I = 139; // -
        // int F2L = 140; // -
        // int F2D = 141; // -
        // int D2I = 142; // -
        // int D2L = 143; // -
        // int D2F = 144; // -
        // int I2B = 145; // -
        // int I2C = 146; // -
        // int I2S = 147; // -

        switch (t1.getSort()) {
        case Type.BOOLEAN:
        case Type.BYTE:
        case Type.CHAR:
        case Type.SHORT:
            t1 = Type.INT_TYPE;
            break;
        }

        int opcode;
        switch (t1.getSort() * 10 + t2.getSort()) {
        case 56:
            opcode = I2F;
            break;
        case 57:
            opcode = I2L;
            break;
        case 58:
            opcode = I2D;
            break;
        case 52:
            opcode = I2C;
            break;
        case 53:
            opcode = I2B;
            break;
        case 54:
            opcode = I2S;
            break;
        case 75:
            opcode = L2I;
            break;
        case 76:
            opcode = L2F;
            break;
        case 78:
            opcode = L2D;
            break;

        case 65:
            opcode = F2I;
            break;
        case 67:
            opcode = F2L;
            break;
        case 68:
            opcode = F2D;
            break;
        case 85:
            opcode = D2I;
            break;
        case 86:
            opcode = D2F;
            break;
        case 87:
            opcode = D2L;
            break;
        default:
            opcode = -1;
            break;
        }
        if (opcode == -1) {
            throw new DexException("can't cast %s to %s", t1, t2);
        }
        asm.visitInsn(opcode);
    }
}