package jadx.core.dex.nodes;

import jadx.core.dex.attributes.AttributeFlag;
import jadx.core.dex.attributes.JumpAttribute;
import jadx.core.dex.attributes.LineAttrNode;
import jadx.core.dex.attributes.LoopAttr;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.info.AccessInfo.AFType;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.GotoNode;
import jadx.core.dex.instructions.IfNode;
import jadx.core.dex.instructions.InsnDecoder;
import jadx.core.dex.instructions.SwitchNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.parser.DebugInfoParser;
import jadx.core.dex.nodes.parser.SignatureParser;
import jadx.core.dex.regions.Region;
import jadx.core.dex.trycatch.ExcHandlerAttr;
import jadx.core.dex.trycatch.ExceptionHandler;
import jadx.core.dex.trycatch.TryCatchBlock;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.DecodeException;
import jadx.core.utils.exceptions.JadxRuntimeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.android.dx.io.ClassData.Method;
import com.android.dx.io.Code;
import com.android.dx.io.Code.CatchHandler;
import com.android.dx.io.Code.Try;

public class MethodNode extends LineAttrNode implements ILoadable {
	private static final Logger LOG = LoggerFactory.getLogger(MethodNode.class);

	private final MethodInfo mthInfo;
	private final ClassNode parentClass;
	private final AccessInfo accFlags;

	private final Method methodData;
	private int regsCount;
	private List<InsnNode> instructions;
	private boolean noCode;

	private ArgType retType;
	private RegisterArg thisArg;
	private List<RegisterArg> argsList;
	private Map<ArgType, List<ArgType>> genericMap;

	private List<BlockNode> blocks;
	private BlockNode enterBlock;
	private List<BlockNode> exitBlocks;

	private Region region;
	private List<ExceptionHandler> exceptionHandlers = Collections.emptyList();
	private List<LoopAttr> loops = Collections.emptyList();

	public MethodNode(ClassNode classNode, Method mthData) {
		this.mthInfo = MethodInfo.fromDex(classNode.dex(), mthData.getMethodIndex());
		this.parentClass = classNode;
		this.accFlags = new AccessInfo(mthData.getAccessFlags(), AFType.METHOD);
		this.noCode = (mthData.getCodeOffset() == 0);
		this.methodData = (noCode ? null : mthData);
	}

	@Override
	public void load() throws DecodeException {
		try {
			if (noCode) {
				regsCount = 0;
				initMethodTypes();
				return;
			}

			DexNode dex = parentClass.dex();
			Code mthCode = dex.readCode(methodData);
			regsCount = mthCode.getRegistersSize();
			initMethodTypes();

			InsnDecoder decoder = new InsnDecoder(this, mthCode);
			InsnNode[] insnByOffset = decoder.run();
			instructions = new ArrayList<InsnNode>();
			for (InsnNode insn : insnByOffset) {
				if (insn != null) {
					instructions.add(insn);
				}
			}
			((ArrayList<InsnNode>) instructions).trimToSize();

			initTryCatches(mthCode, insnByOffset);
			initJumps(insnByOffset);

			int debugInfoOffset = mthCode.getDebugInfoOffset();
			if (debugInfoOffset > 0) {
				DebugInfoParser debugInfoParser = new DebugInfoParser(this, debugInfoOffset, insnByOffset);
				debugInfoParser.process();

				if (instructions.size() != 0) {
					int line = instructions.get(0).getSourceLine();
					if (line != 0) {
						this.setSourceLine(line - 1);
					}
				}
			}
		} catch (Exception e) {
			throw new DecodeException(this, "Load method exception", e);
		}
	}

	private void initMethodTypes() {
		if (!parseSignature()) {
			retType = mthInfo.getReturnType();
			initArguments(mthInfo.getArgumentsTypes());
		}
	}

	@Override
	public void unload() {
		if (noCode) {
			return;
		}
		if (instructions != null) {
			instructions.clear();
		}
		blocks = null;
		exitBlocks = null;
		exceptionHandlers.clear();
		noCode = true;
	}

	private boolean parseSignature() {
		SignatureParser sp = SignatureParser.fromNode(this);
		if (sp == null) {
			return false;
		}
		try {
			genericMap = sp.consumeGenericMap();
			List<ArgType> argsTypes = sp.consumeMethodArgs();
			retType = sp.consumeType();

			List<ArgType> mthArgs = mthInfo.getArgumentsTypes();
			if (argsTypes.size() != mthArgs.size()) {
				if (argsTypes.isEmpty()) {
					return false;
				}
				if (!mthInfo.isConstructor()) {
					LOG.warn("Wrong signature parse result: " + sp + " -> " + argsTypes
							+ ", not generic version: " + mthArgs);
					return false;
				} else if (getParentClass().getAccessFlags().isEnum()) {
					// TODO:
					argsTypes.add(0, mthArgs.get(0));
					argsTypes.add(1, mthArgs.get(1));
				} else {
					// add synthetic arg for outer class
					argsTypes.add(0, mthArgs.get(0));
				}
				if (argsTypes.size() != mthArgs.size()) {
					return false;
				}
			}
			initArguments(argsTypes);
		} catch (JadxRuntimeException e) {
			LOG.error("Method signature parse error: " + this, e);
			return false;
		}
		return true;
	}

	private void initArguments(List<ArgType> args) {
		int pos;
		if (noCode) {
			pos = 1;
		} else {
			pos = regsCount;
			for (ArgType arg : args) {
				pos -= arg.getRegCount();
			}
		}
		if (accFlags.isStatic()) {
			thisArg = null;
		} else {
			thisArg = InsnArg.immutableReg(pos - 1, parentClass.getClassInfo().getType());
			thisArg.getTypedVar().setName("this");
		}
		if (args.isEmpty()) {
			argsList = Collections.emptyList();
			return;
		}
		argsList = new ArrayList<RegisterArg>(args.size());
		for (ArgType arg : args) {
			argsList.add(InsnArg.immutableReg(pos, arg));
			pos += arg.getRegCount();
		}
	}

	public List<RegisterArg> getArguments(boolean includeThis) {
		if (includeThis && thisArg != null) {
			List<RegisterArg> list = new ArrayList<RegisterArg>(argsList.size() + 1);
			list.add(thisArg);
			list.addAll(argsList);
			return list;
		} else {
			return argsList;
		}
	}

	public RegisterArg removeFirstArgument() {
		this.getAttributes().add(AttributeFlag.SKIP_FIRST_ARG);
		return argsList.remove(0);
	}

	public RegisterArg getThisArg() {
		return thisArg;
	}

	public ArgType getReturnType() {
		return retType;
	}

	public Map<ArgType, List<ArgType>> getGenericMap() {
		return genericMap;
	}

	// TODO: move to external class
	private void initTryCatches(Code mthCode, InsnNode[] insnByOffset) {
		CatchHandler[] catchBlocks = mthCode.getCatchHandlers();
		Try[] tries = mthCode.getTries();

		// Bug in dx library already fixed (Try.getHandlerOffset() replaced by Try.getCatchHandlerIndex())
		// and we don't need this mapping anymore,
		// but in maven repository still old version
		Set<Integer> handlerSet = new HashSet<Integer>(tries.length);
		for (Try aTry : tries) {
			handlerSet.add(aTry.getHandlerOffset());
		}
		List<Integer> handlerList = new ArrayList<Integer>(catchBlocks.length);
		handlerList.addAll(handlerSet);
		Collections.sort(handlerList);
		handlerSet = null;
		// -------------------

		int hc = 0;
		Set<Integer> addrs = new HashSet<Integer>();
		List<TryCatchBlock> catches = new ArrayList<TryCatchBlock>(catchBlocks.length);

		for (CatchHandler handler : catchBlocks) {
			TryCatchBlock tcBlock = new TryCatchBlock();
			catches.add(tcBlock);
			for (int i = 0; i < handler.getAddresses().length; i++) {
				int addr = handler.getAddresses()[i];
				ClassInfo type = ClassInfo.fromDex(parentClass.dex(), handler.getTypeIndexes()[i]);
				tcBlock.addHandler(this, addr, type);
				addrs.add(addr);
				hc++;
			}
			int addr = handler.getCatchAllAddress();
			if (addr >= 0) {
				tcBlock.addHandler(this, addr, null);
				addrs.add(addr);
				hc++;
			}
		}

		if (hc > 0 && hc != addrs.size()) {
			// resolve nested try blocks:
			// inner block contains all handlers from outer block => remove these handlers from inner block
			// each handler must be only in one try/catch block
			for (TryCatchBlock ct1 : catches) {
				for (TryCatchBlock ct2 : catches) {
					if (ct1 != ct2 && ct2.getHandlers().containsAll(ct1.getHandlers())) {
						for (ExceptionHandler h : ct1.getHandlers()) {
							ct2.removeHandler(this, h);
						}
					}
				}
			}
		}

		// attach EXC_HANDLER attributes to instructions
		addrs.clear();
		for (TryCatchBlock ct : catches) {
			for (ExceptionHandler eh : ct.getHandlers()) {
				int addr = eh.getHandleOffset();
				// assert addrs.add(addr) : "Instruction already contains EXC_HANDLER attribute";
				ExcHandlerAttr ehAttr = new ExcHandlerAttr(ct, eh);
				insnByOffset[addr].getAttributes().add(ehAttr);
			}
		}

		// attach TRY_ENTER, TRY_LEAVE attributes to instructions
		for (Try aTry : tries) {
			int catchNum = handlerList.indexOf(aTry.getHandlerOffset());
			TryCatchBlock block = catches.get(catchNum);
			int offset = aTry.getStartAddress();
			int end = offset + aTry.getInstructionCount() - 1;

			insnByOffset[offset].getAttributes().add(AttributeFlag.TRY_ENTER);
			while (offset <= end && offset >= 0) {
				block.addInsn(insnByOffset[offset]);
				offset = InsnDecoder.getNextInsnOffset(insnByOffset, offset);
			}
			if (insnByOffset[end] != null) {
				insnByOffset[end].getAttributes().add(AttributeFlag.TRY_LEAVE);
			}
		}
	}

	private void initJumps(InsnNode[] insnByOffset) {
		for (InsnNode insn : getInstructions()) {
			int offset = insn.getOffset();
			switch (insn.getType()) {
				case SWITCH: {
					SwitchNode sw = (SwitchNode) insn;
					for (int target : sw.getTargets()) {
						addJump(insnByOffset, offset, target);
					}
					// default case
					int next = InsnDecoder.getNextInsnOffset(insnByOffset, offset);
					if (next != -1) {
						addJump(insnByOffset, offset, next);
					}
					break;
				}

				case IF:
					int next = InsnDecoder.getNextInsnOffset(insnByOffset, offset);
					if (next != -1) {
						addJump(insnByOffset, offset, next);
					}
					addJump(insnByOffset, offset, ((IfNode) insn).getTarget());
					break;

				case GOTO:
					addJump(insnByOffset, offset, ((GotoNode) insn).getTarget());
					break;

				default:
					break;
			}
		}
	}

	private static void addJump(InsnNode[] insnByOffset, int offset, int target) {
		insnByOffset[target].getAttributes().add(new JumpAttribute(offset, target));
	}

	public String getName() {
		String name = mthInfo.getName();
		if (name.equals(parentClass.getShortName())) {
			return name + "_";
		} else {
			return name;
		}
	}

	public ClassNode getParentClass() {
		return parentClass;
	}

	public boolean isNoCode() {
		return noCode;
	}

	public List<InsnNode> getInstructions() {
		return instructions;
	}

	public void initBasicBlocks() {
		blocks = new ArrayList<BlockNode>();
		exitBlocks = new ArrayList<BlockNode>(1);
	}

	public void finishBasicBlocks() {
		// after filling basic blocks we don't need instructions list anymore
		instructions.clear();
		instructions = null;

		((ArrayList<BlockNode>) blocks).trimToSize();
		((ArrayList<BlockNode>) exitBlocks).trimToSize();

		blocks = Collections.unmodifiableList(blocks);
		exitBlocks = Collections.unmodifiableList(exitBlocks);

		for (BlockNode block : blocks) {
			block.lock();
		}
	}

	public List<BlockNode> getBasicBlocks() {
		return blocks;
	}

	public BlockNode getEnterBlock() {
		return enterBlock;
	}

	public void setEnterBlock(BlockNode enterBlock) {
		this.enterBlock = enterBlock;
	}

	public List<BlockNode> getExitBlocks() {
		return exitBlocks;
	}

	public void addExitBlock(BlockNode exitBlock) {
		this.exitBlocks.add(exitBlock);
	}

	public void registerLoop(LoopAttr loop) {
		if (loops.isEmpty()) {
			loops = new ArrayList<LoopAttr>(5);
		}
		loops.add(loop);
	}

	public LoopAttr getLoopForBlock(BlockNode block) {
		for (LoopAttr loop : loops) {
			if (loop.getLoopBlocks().contains(block)) {
				return loop;
			}
		}
		return null;
	}

	public int getLoopsCount() {
		return loops.size();
	}

	public ExceptionHandler addExceptionHandler(ExceptionHandler handler) {
		if (exceptionHandlers.isEmpty()) {
			exceptionHandlers = new ArrayList<ExceptionHandler>(2);
		} else {
			for (ExceptionHandler h : exceptionHandlers) {
				if (h == handler || h.getHandleOffset() == handler.getHandleOffset()) {
					return h;
				}
			}
		}
		exceptionHandlers.add(handler);
		return handler;
	}

	public List<ExceptionHandler> getExceptionHandlers() {
		return Collections.unmodifiableList(exceptionHandlers);
	}

	/**
	 * Return true if exists method with same name and arguments count
	 */
	public boolean isArgsOverload() {
		int argsCount = mthInfo.getArgumentsTypes().size();
		if (argsCount == 0) {
			return false;
		}

		String name = getName();
		List<MethodNode> methods = parentClass.getMethods();
		for (MethodNode method : methods) {
			if (this != method
					&& method.getName().equals(name)
					&& method.mthInfo.getArgumentsTypes().size() == argsCount) {
				return true;
			}
		}
		return false;
	}

	public int getRegsCount() {
		return regsCount;
	}

	public AccessInfo getAccessFlags() {
		return accFlags;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public DexNode dex() {
		return parentClass.dex();
	}

	public MethodInfo getMethodInfo() {
		return mthInfo;
	}

	@Override
	public int hashCode() {
		return mthInfo.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		MethodNode other = (MethodNode) obj;
		return mthInfo.equals(other.mthInfo);
	}

	@Override
	public String toString() {
		return parentClass.getFullName() + "." + mthInfo.getName()
				+ "(" + Utils.listToString(mthInfo.getArgumentsTypes()) + "):"
				+ retType;
	}
}