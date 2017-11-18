/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package com.intellij.formatting;

import com.intellij.formatting.engine.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.formatting.InitialInfoBuilder.prepareToBuildBlocksSequentially;

public class FormatProcessor {
  private static final Logger LOG = Logger.getInstance("#com.intellij.formatting.FormatProcessor");
  private Set<Alignment> myAlignmentsInsideRangesToModify = null;
  private boolean myReformatContext;

  private LeafBlockWrapper myCurrentBlock;

  private Map<AbstractBlockWrapper, Block> myInfos;
  private CompositeBlockWrapper myRootBlockWrapper;

  private BlockMapperHelper myBlockMapperHelper;
  private DependentSpacingEngine myDependentSpacingEngine;
  private AlignmentHelper myAlignmentHelper;

  private final BlockIndentOptions myBlockIndentOptions;
  private final CommonCodeStyleSettings.IndentOptions myDefaultIndentOption;
  private final CodeStyleSettings mySettings;
  private final Document myDocument;

  private LeafBlockWrapper myWrapCandidate           = null;
  private LeafBlockWrapper myFirstWrappedBlockOnLine = null;

  private LeafBlockWrapper myFirstTokenBlock;
  private Ref<LeafBlockWrapper> myFirstTokenBlockRef = Ref.create();
  private LeafBlockWrapper myLastTokenBlock;


  private final HashSet<WhiteSpace> myAlignAgain = new HashSet<WhiteSpace>();
  @NotNull
  private final FormattingProgressCallback myProgressCallback;

  private WhiteSpace                      myLastWhiteSpace;
  private boolean                         myDisposed;
  private final int myRightMargin;

  @NotNull
  private StateProcessor myStateProcessor;
  private MultiMap<ExpandableIndent, AbstractBlockWrapper> myExpandableIndents;

  public FormatProcessor(final FormattingDocumentModel docModel,
                         Block rootBlock,
                         CodeStyleSettings settings,
                         CommonCodeStyleSettings.IndentOptions indentOptions,
                         @Nullable FormatTextRanges affectedRanges,
                         @NotNull FormattingProgressCallback progressCallback)
  {
    this(docModel, rootBlock, new FormatOptions(settings, indentOptions, affectedRanges, false), progressCallback);
  }

  public FormatProcessor(final FormattingDocumentModel model,
                         Block block,
                         FormatOptions options,
                         @NotNull FormattingProgressCallback callback)
  {
    myProgressCallback = callback;
    myDefaultIndentOption = options.myIndentOptions;
    mySettings = options.mySettings;
    myBlockIndentOptions = new BlockIndentOptions(mySettings, myDefaultIndentOption);
    myDocument = model.getDocument();
    myReformatContext = options.myReformatContext;
    myRightMargin = getRightMargin(block);

    final InitialInfoBuilder builder = prepareToBuildBlocksSequentially(block, model, options, mySettings, myDefaultIndentOption, myProgressCallback);
    final WrapBlocksState wrapState = new WrapBlocksState(builder);
    wrapState.setOnDone(new Runnable() {
      @Override
      public void run() {
        myInfos = builder.getBlockToInfoMap();
        myRootBlockWrapper = builder.getRootBlockWrapper();
        myFirstTokenBlock = builder.getFirstTokenBlock();
        myFirstTokenBlockRef.set(myFirstTokenBlock);
        myLastTokenBlock = builder.getLastTokenBlock();
        myCurrentBlock = myFirstTokenBlock;
        int lastBlockOffset = myLastTokenBlock.getEndOffset();
        myLastWhiteSpace = new WhiteSpace(lastBlockOffset, false);
        myLastWhiteSpace.append(Math.max(lastBlockOffset, builder.getEndOffset()), model, myDefaultIndentOption);
        myBlockMapperHelper = new BlockMapperHelper(myFirstTokenBlock, myLastTokenBlock);
        myDependentSpacingEngine = new DependentSpacingEngine(myBlockMapperHelper);
        myAlignmentsInsideRangesToModify = builder.getAlignmentsInsideRangeToModify();
        myAlignmentHelper = new AlignmentHelper(myDocument, builder.getBlocksToAlign(), myBlockIndentOptions);
        myExpandableIndents = builder.getExpandableIndentsBlocks();
      }
    });
    myStateProcessor = new StateProcessor(wrapState);
  }

  public BlockMapperHelper getBlockMapperHelper() {
    return myBlockMapperHelper;
  }

  private int getRightMargin(Block rootBlock) {
    Language language = null;
    if (rootBlock instanceof ASTBlock) {
      ASTNode node = ((ASTBlock)rootBlock).getNode();
      if (node != null) {
        PsiElement psiElement = node.getPsi();
        if (psiElement.isValid()) {
          PsiFile psiFile = psiElement.getContainingFile();
          if (psiFile != null) {
            language = psiFile.getViewProvider().getBaseLanguage();
          }
        }
      }
    }
    return mySettings.getRightMargin(language);
  }

  public void format(FormattingModel model) {
    format(model, false);
  }

  /**
   * Asks current processor to perform formatting.
   * <p/>
   * There are two processing approaches at the moment:
   * <pre>
   * <ul>
   *   <li>perform formatting during the current method call;</li>
   *   <li>
   *     split the whole formatting process to the set of fine-grained tasks and execute them sequentially during
   *     subsequent {@link #iteration()} calls;
   *   </li>
   * </ul>
   * </pre>
   * <p/>
   * Here is rationale for the second approach - formatting may introduce changes to the underlying document and IntelliJ IDEA
   * is designed in a way that write access is allowed from EDT only. That means that every time we execute particular action
   * from EDT we have no chance of performing any other actions from EDT simultaneously (e.g. we may want to show progress bar
   * that reflects current formatting state but the progress bar can' bet updated if formatting is performed during a single long
   * method call). So, we can interleave formatting iterations with GUI state updates.
   *
   * @param model         target formatting model
   * @param sequentially  flag that indicates what kind of processing should be used
   */
  public void format(FormattingModel model, boolean sequentially) {
    if (sequentially) {
      myStateProcessor.setNextState(new AdjustWhiteSpacesState());
      myStateProcessor.setNextState(new ExpandChildrenIndent());
      myStateProcessor.setNextState(new ApplyChangesState(myFirstTokenBlockRef, model, myBlockIndentOptions, myProgressCallback));
    }
    else {
      formatWithoutRealModifications(false);
      performModifications(model, false);
    }
  }

  /**
   * Asks current processor to perform processing iteration
   *
   * @return    <code>true</code> if the processing is finished; <code>false</code> otherwise
   * @see #format(FormattingModel, boolean)
   */
  public boolean iteration() {
    if (myStateProcessor.isDone()) {
      return true;
    }
    myStateProcessor.iteration();
    return myStateProcessor.isDone();
  }

  /**
   * Asks current processor to stop any active sequential processing if any.
   */
  public void stopSequentialProcessing() {
    myStateProcessor.stop();
  }

  public void formatWithoutRealModifications() {
    formatWithoutRealModifications(false);
  }

  @SuppressWarnings({"WhileLoopSpinsOnField"})
  public void formatWithoutRealModifications(boolean sequentially) {
    myStateProcessor.setNextState(new AdjustWhiteSpacesState());
    myStateProcessor.setNextState(new ExpandChildrenIndent());
    if (sequentially) {
      return;
    }
    doIterationsSynchronously();
  }

  private void reset() {
    myAlignmentHelper.reset();
    myDependentSpacingEngine.clear();
    myWrapCandidate = null;
    if (myRootBlockWrapper != null) {
      myRootBlockWrapper.reset();
    }
  }

  public void performModifications(FormattingModel model) {
    performModifications(model, false);
  }

  public void performModifications(FormattingModel model, boolean sequentially) {
    assert !myDisposed;
    myStateProcessor.setNextState(new ApplyChangesState(myFirstTokenBlockRef, model, myBlockIndentOptions, myProgressCallback));

    if (sequentially) {
      return;
    }

    doIterationsSynchronously();
  }

  private void doIterationsSynchronously() {
    while (!myStateProcessor.isDone()) {
      myStateProcessor.iteration();
    }
  }

  private void processToken() {
    final SpacingImpl spaceProperty = myCurrentBlock.getSpaceProperty();
    final WhiteSpace whiteSpace = myCurrentBlock.getWhiteSpace();

    if (isReformatSelectedRangesContext()) {
      if (isCurrentBlockAlignmentUsedInRangesToModify() && whiteSpace.isReadOnly() && spaceProperty != null && !spaceProperty.isReadOnly()) {
        whiteSpace.setReadOnly(false);
        whiteSpace.setLineFeedsAreReadOnly(true);
      }
    }

    whiteSpace.arrangeLineFeeds(spaceProperty, this);

    if (!whiteSpace.containsLineFeeds()) {
      whiteSpace.arrangeSpaces(spaceProperty);
    }

    try {
      if (processWrap()) {
        return;
      }
    }
    finally {
      if (whiteSpace.containsLineFeeds()) {
        onCurrentLineChanged();
      }
    }

    if (!adjustIndentAndContinue()) {
      return;
    }

    defineAlignOffset(myCurrentBlock);

    if (myCurrentBlock.containsLineFeeds()) {
      onCurrentLineChanged();
    }


    final List<TextRange> ranges = getDependentRegionRangesAfterCurrentWhiteSpace(spaceProperty, whiteSpace);
    if (!ranges.isEmpty()) {
      myDependentSpacingEngine.registerUnresolvedDependentSpacingRanges(spaceProperty, ranges);
    }

    if (!whiteSpace.isIsReadOnly() && myDependentSpacingEngine.shouldReformatPreviouslyLocatedDependentSpacing(whiteSpace)) {
      myAlignAgain.add(whiteSpace);
    }
    else if (!myAlignAgain.isEmpty()) {
      myAlignAgain.remove(whiteSpace);
    }

    myCurrentBlock = myCurrentBlock.getNextBlock();
  }

  private void onCurrentLineChanged() {
    myWrapCandidate = null;
  }

  private boolean isReformatSelectedRangesContext() {
    return myReformatContext && !ContainerUtil.isEmpty(myAlignmentsInsideRangesToModify);
  }

  private boolean isCurrentBlockAlignmentUsedInRangesToModify() {
    AbstractBlockWrapper block = myCurrentBlock;
    AlignmentImpl alignment = myCurrentBlock.getAlignment();

    while (alignment == null) {
      block = block.getParent();
      if (block == null || block.getStartOffset() != myCurrentBlock.getStartOffset()) {
        return false;
      }
      alignment = block.getAlignment();
    }

    return myAlignmentsInsideRangesToModify.contains(alignment);
  }

  private static List<TextRange> getDependentRegionRangesAfterCurrentWhiteSpace(final SpacingImpl spaceProperty,
                                                                                final WhiteSpace whiteSpace)
  {
    if (!(spaceProperty instanceof DependantSpacingImpl)) return ContainerUtil.emptyList();

    if (whiteSpace.isReadOnly() || whiteSpace.isLineFeedsAreReadOnly()) return ContainerUtil.emptyList();

    DependantSpacingImpl spacing = (DependantSpacingImpl)spaceProperty;
    return ContainerUtil.filter(spacing.getDependentRegionRanges(), new Condition<TextRange>() {
      @Override
      public boolean value(TextRange dependencyRange) {
        return whiteSpace.getStartOffset() < dependencyRange.getEndOffset();
      }
    });
  }

  /**
   * Processes the wrap of the current block.
   *
   * @return true if we have changed myCurrentBlock and need to restart its processing; false if myCurrentBlock is unchanged and we can
   * continue processing
   */
  private boolean processWrap() {
    final SpacingImpl spacing = myCurrentBlock.getSpaceProperty();
    final WhiteSpace whiteSpace = myCurrentBlock.getWhiteSpace();

    final boolean wrapWasPresent = whiteSpace.containsLineFeeds();

    if (wrapWasPresent) {
      myFirstWrappedBlockOnLine = null;

      if (!whiteSpace.containsLineFeedsInitially()) {
        whiteSpace.removeLineFeeds(spacing, this);
      }
    }

    final boolean wrapIsPresent = whiteSpace.containsLineFeeds();

    final ArrayList<WrapImpl> wraps = myCurrentBlock.getWraps();
    for (WrapImpl wrap : wraps) {
      wrap.setWrapOffset(myCurrentBlock.getStartOffset());
    }

    final WrapImpl wrap = getWrapToBeUsed(wraps);

    if (wrap != null || wrapIsPresent) {
      if (!wrapIsPresent && !canReplaceWrapCandidate(wrap)) {
        myCurrentBlock = myWrapCandidate;
        return true;
      }
      if (wrap != null && wrap.getChopStartBlock() != null) {
        // getWrapToBeUsed() returns the block only if it actually exceeds the right margin. In this case, we need to go back to the
        // first block that has the CHOP_IF_NEEDED wrap type and start wrapping from there.
        myCurrentBlock = wrap.getChopStartBlock();
        wrap.setActive();
        return true;
      }
      if (wrap != null && isChopNeeded(wrap)) {
        wrap.setActive();
      }

      if (!wrapIsPresent) {
        whiteSpace.ensureLineFeed();
        if (!wrapWasPresent) {
          if (myFirstWrappedBlockOnLine != null && wrap.isChildOf(myFirstWrappedBlockOnLine.getWrap(), myCurrentBlock)) {
            wrap.ignoreParentWrap(myFirstWrappedBlockOnLine.getWrap(), myCurrentBlock);
            myCurrentBlock = myFirstWrappedBlockOnLine;
            return true;
          }
          else {
            myFirstWrappedBlockOnLine = myCurrentBlock;
          }
        }
      }

      myWrapCandidate = null;
    }
    else {
      for (final WrapImpl wrap1 : wraps) {
        if (isCandidateToBeWrapped(wrap1) && canReplaceWrapCandidate(wrap1)) {
          myWrapCandidate = myCurrentBlock;
        }
        if (isChopNeeded(wrap1)) {
          wrap1.saveChopBlock(myCurrentBlock);
        }
      }
    }

    if (!whiteSpace.containsLineFeeds() && myWrapCandidate != null && !whiteSpace.isReadOnly() && lineOver()) {
      myCurrentBlock = myWrapCandidate;
      return true;
    }

    return false;
  }

  /**
   * Allows to answer if wrap of the {@link #myWrapCandidate} object (if any) may be replaced by the given wrap.
   *
   * @param wrap wrap candidate to check
   * @return <code>true</code> if wrap of the {@link #myWrapCandidate} object (if any) may be replaced by the given wrap;
   *         <code>false</code> otherwise
   */
  private boolean canReplaceWrapCandidate(WrapImpl wrap) {
    if (myWrapCandidate == null) return true;
    WrapImpl.Type type = wrap.getType();
    if (wrap.isActive() && (type == WrapImpl.Type.CHOP_IF_NEEDED || type == WrapImpl.Type.WRAP_ALWAYS)) return true;
    final WrapImpl currentWrap = myWrapCandidate.getWrap();
    return wrap == currentWrap || !wrap.isChildOf(currentWrap, myCurrentBlock);
  }

  private boolean isCandidateToBeWrapped(final WrapImpl wrap) {
    return isSuitableInTheCurrentPosition(wrap) &&
           (wrap.getType() == WrapImpl.Type.WRAP_AS_NEEDED || wrap.getType() == WrapImpl.Type.CHOP_IF_NEEDED) &&
           !myCurrentBlock.getWhiteSpace().isReadOnly();
  }

  /**
   * Adjusts indent of the current block.
   *
   * @return <code>true</code> if current formatting iteration should be continued;
   *         <code>false</code> otherwise (e.g. if previously processed block is shifted inside this method for example
   *         because of specified alignment options)
   */
  private boolean adjustIndentAndContinue() {
    AlignmentImpl alignment = CoreFormatterUtil.getAlignment(myCurrentBlock);
    WhiteSpace whiteSpace = myCurrentBlock.getWhiteSpace();

    if (alignment == null || myAlignmentHelper.shouldSkip(alignment)) {
      if (whiteSpace.containsLineFeeds()) {
        adjustSpacingByIndentOffset();
      }
      else {
        whiteSpace.arrangeSpaces(myCurrentBlock.getSpaceProperty());
      }
      return true;
    }

    LeafBlockWrapper newCurrentBlock = myAlignmentHelper.applyAlignmentAndContinueFormatting(alignment, myCurrentBlock);
    if (newCurrentBlock != myCurrentBlock) {
      myCurrentBlock = newCurrentBlock;
      onCurrentLineChanged();
      return false;
    }
    return true;
  }

  /**
   * Applies indent to the white space of {@link #myCurrentBlock currently processed wrapped block}. Both indentation
   * and alignment options are took into consideration here.
   */
  private void adjustLineIndent() {
    IndentData alignOffset = getAlignOffset();

    if (alignOffset == null) {
      adjustSpacingByIndentOffset();
    }
    else {
      myCurrentBlock.getWhiteSpace().setSpaces(alignOffset.getSpaces(), alignOffset.getIndentSpaces());
    }
  }

  private void adjustSpacingByIndentOffset() {
    IndentData offset = myCurrentBlock.calculateOffset(myBlockIndentOptions.getIndentOptions(myCurrentBlock));
    myCurrentBlock.getWhiteSpace().setSpaces(offset.getSpaces(), offset.getIndentSpaces());
  }

  private boolean isChopNeeded(final WrapImpl wrap) {
    return wrap != null && wrap.getType() == WrapImpl.Type.CHOP_IF_NEEDED && isSuitableInTheCurrentPosition(wrap);
  }

  private boolean isSuitableInTheCurrentPosition(final WrapImpl wrap) {
    if (wrap.getWrapOffset() < myCurrentBlock.getStartOffset()) {
      return true;
    }

    if (wrap.isWrapFirstElement()) {
      return true;
    }

    if (wrap.getType() == WrapImpl.Type.WRAP_AS_NEEDED) {
      return positionAfterWrappingIsSuitable();
    }

    return wrap.getType() == WrapImpl.Type.CHOP_IF_NEEDED && lineOver() && positionAfterWrappingIsSuitable();
  }

  /**
   * Ensures that offset of the {@link #myCurrentBlock currently processed block} is not increased if we make a wrap on it.
   *
   * @return <code>true</code> if it's ok to wrap at the currently processed block; <code>false</code> otherwise
   */
  private boolean positionAfterWrappingIsSuitable() {
    final WhiteSpace whiteSpace = myCurrentBlock.getWhiteSpace();
    if (whiteSpace.containsLineFeeds()) return true;
    final int spaces = whiteSpace.getSpaces();
    int indentSpaces = whiteSpace.getIndentSpaces();
    try {
      final int startColumnNow = CoreFormatterUtil.getStartColumn(myCurrentBlock);
      whiteSpace.ensureLineFeed();
      adjustLineIndent();
      final int startColumnAfterWrap = CoreFormatterUtil.getStartColumn(myCurrentBlock);
      return startColumnNow > startColumnAfterWrap;
    }
    finally {
      whiteSpace.removeLineFeeds(myCurrentBlock.getSpaceProperty(), this);
      whiteSpace.setSpaces(spaces, indentSpaces);
    }
  }

  @Nullable
  private WrapImpl getWrapToBeUsed(final ArrayList<WrapImpl> wraps) {
    if (wraps.isEmpty()) {
      return null;
    }
    if (myWrapCandidate == myCurrentBlock) return wraps.get(0);

    for (final WrapImpl wrap : wraps) {
      if (!isSuitableInTheCurrentPosition(wrap)) continue;
      if (wrap.isActive()) return wrap;

      final WrapImpl.Type type = wrap.getType();
      if (type == WrapImpl.Type.WRAP_ALWAYS) return wrap;
      if (type == WrapImpl.Type.WRAP_AS_NEEDED || type == WrapImpl.Type.CHOP_IF_NEEDED) {
        if (lineOver()) {
          return wrap;
        }
      }
    }
    return null;
  }

  /**
   * @return <code>true</code> if {@link #myCurrentBlock currently processed wrapped block} doesn't contain line feeds and
   *         exceeds right margin; <code>false</code> otherwise
   */
  private boolean lineOver() {
    return !myCurrentBlock.containsLineFeeds() &&
           CoreFormatterUtil.getStartColumn(myCurrentBlock) + myCurrentBlock.getLength() > myRightMargin;
  }

  private void defineAlignOffset(final LeafBlockWrapper block) {
    AbstractBlockWrapper current = myCurrentBlock;
    while (true) {
      final AlignmentImpl alignment = current.getAlignment();
      if (alignment != null) {
        alignment.setOffsetRespBlock(block);
      }
      current = current.getParent();
      if (current == null) return;
      if (current.getStartOffset() != myCurrentBlock.getStartOffset()) return;

    }
  }

  /**
   * Tries to get align-implied indent of the current block.
   *
   * @return indent of the current block if any; <code>null</code> otherwise
   */
  @Nullable
  private IndentData getAlignOffset() {
    AbstractBlockWrapper current = myCurrentBlock;
    while (true) {
      final AlignmentImpl alignment = current.getAlignment();
      LeafBlockWrapper offsetResponsibleBlock;
      if (alignment != null && (offsetResponsibleBlock = alignment.getOffsetRespBlockBefore(myCurrentBlock)) != null) {
        final WhiteSpace whiteSpace = offsetResponsibleBlock.getWhiteSpace();
        if (whiteSpace.containsLineFeeds()) {
          return new IndentData(whiteSpace.getIndentSpaces(), whiteSpace.getSpaces());
        }
        else {
          final int offsetBeforeBlock = CoreFormatterUtil.getStartColumn(offsetResponsibleBlock);
          final AbstractBlockWrapper indentedParentBlock = CoreFormatterUtil.getIndentedParentBlock(myCurrentBlock);
          if (indentedParentBlock == null) {
            return new IndentData(0, offsetBeforeBlock);
          }
          else {
            final int parentIndent = indentedParentBlock.getWhiteSpace().getIndentOffset();
            if (parentIndent > offsetBeforeBlock) {
              return new IndentData(0, offsetBeforeBlock);
            }
            else {
              return new IndentData(parentIndent, offsetBeforeBlock - parentIndent);
            }
          }
        }
      }
      else {
        current = current.getParent();
        if (current == null || current.getStartOffset() != myCurrentBlock.getStartOffset()) return null;
      }
    }
  }

  public void setAllWhiteSpacesAreReadOnly() {
    LeafBlockWrapper current = myFirstTokenBlock;
    while (current != null) {
      current.getWhiteSpace().setReadOnly(true);
      current = current.getNextBlock();
    }
  }

  static class ChildAttributesInfo {
    public final AbstractBlockWrapper parent;
    final        ChildAttributes      attributes;
    final        int                  index;

    public ChildAttributesInfo(final AbstractBlockWrapper parent, final ChildAttributes attributes, final int index) {
      this.parent = parent;
      this.attributes = attributes;
      this.index = index;
    }
  }

  public IndentInfo getIndentAt(final int offset) {
    processBlocksBefore(offset);
    AbstractBlockWrapper parent = getParentFor(offset, myCurrentBlock);
    if (parent == null) {
      final LeafBlockWrapper previousBlock = myCurrentBlock.getPreviousBlock();
      if (previousBlock != null) parent = getParentFor(offset, previousBlock);
      if (parent == null) return new IndentInfo(0, 0, 0);
    }
    int index = getNewChildPosition(parent, offset);
    final Block block = myInfos.get(parent);

    if (block == null) {
      return new IndentInfo(0, 0, 0);
    }

    ChildAttributesInfo info = getChildAttributesInfo(block, index, parent);
    if (info == null) {
      return new IndentInfo(0, 0, 0);
    }

    return adjustLineIndent(info.parent, info.attributes, info.index);
  }

  @Nullable
  private static ChildAttributesInfo getChildAttributesInfo(@NotNull final Block block,
                                                            final int index,
                                                            @Nullable AbstractBlockWrapper parent) {
    if (parent == null) {
      return null;
    }
    ChildAttributes childAttributes = block.getChildAttributes(index);

    if (childAttributes == ChildAttributes.DELEGATE_TO_PREV_CHILD) {
      final Block newBlock = block.getSubBlocks().get(index - 1);
      AbstractBlockWrapper prevWrappedBlock;
      if (parent instanceof CompositeBlockWrapper) {
        prevWrappedBlock = ((CompositeBlockWrapper)parent).getChildren().get(index - 1);
      }
      else {
        prevWrappedBlock = parent.getPreviousBlock();
      }
      return getChildAttributesInfo(newBlock, newBlock.getSubBlocks().size(), prevWrappedBlock);
    }

    else if (childAttributes == ChildAttributes.DELEGATE_TO_NEXT_CHILD) {
      AbstractBlockWrapper nextWrappedBlock;
      if (parent instanceof CompositeBlockWrapper) {
        List<AbstractBlockWrapper> children = ((CompositeBlockWrapper)parent).getChildren();
        if (children != null && index < children.size()) {
          nextWrappedBlock = children.get(index);
        }
        else {
          return null;
        }
      }
      else {
        nextWrappedBlock = ((LeafBlockWrapper)parent).getNextBlock();
      }
      return getChildAttributesInfo(block.getSubBlocks().get(index), 0, nextWrappedBlock);
    }

    else {
      return new ChildAttributesInfo(parent, childAttributes, index);
    }
  }

  private IndentInfo adjustLineIndent(final AbstractBlockWrapper parent, final ChildAttributes childAttributes, final int index) {
    int alignOffset = getAlignOffsetBefore(childAttributes.getAlignment(), null);
    if (alignOffset == -1) {
      return parent.calculateChildOffset(myBlockIndentOptions.getIndentOptions(parent), childAttributes, index).createIndentInfo();
    }
    else {
      AbstractBlockWrapper indentedParentBlock = CoreFormatterUtil.getIndentedParentBlock(myCurrentBlock);
      if (indentedParentBlock == null) {
        return new IndentInfo(0, 0, alignOffset);
      }
      else {
        int indentOffset = indentedParentBlock.getWhiteSpace().getIndentOffset();
        if (indentOffset > alignOffset) {
          return new IndentInfo(0, 0, alignOffset);
        }
        else {
          return new IndentInfo(0, indentOffset, alignOffset - indentOffset);
        }
      }
    }
  }

  private static int getAlignOffsetBefore(@Nullable final Alignment alignment, @Nullable final LeafBlockWrapper blockAfter) {
    if (alignment == null) return -1;
    final LeafBlockWrapper alignRespBlock = ((AlignmentImpl)alignment).getOffsetRespBlockBefore(blockAfter);
    if (alignRespBlock != null) {
      return CoreFormatterUtil.getStartColumn(alignRespBlock);
    }
    else {
      return -1;
    }
  }

  private static int getNewChildPosition(final AbstractBlockWrapper parent, final int offset) {
    AbstractBlockWrapper parentBlockToUse = getLastNestedCompositeBlockForSameRange(parent);
    if (!(parentBlockToUse instanceof CompositeBlockWrapper)) return 0;
    final List<AbstractBlockWrapper> subBlocks = ((CompositeBlockWrapper)parentBlockToUse).getChildren();
    //noinspection ConstantConditions
    if (subBlocks != null) {
      for (int i = 0; i < subBlocks.size(); i++) {
        AbstractBlockWrapper block = subBlocks.get(i);
        if (block.getStartOffset() >= offset) return i;
      }
      return subBlocks.size();
    }
    else {
      return 0;
    }
  }

  @Nullable
  private static AbstractBlockWrapper getParentFor(final int offset, AbstractBlockWrapper block) {
    AbstractBlockWrapper current = block;
    while (current != null) {
      if (current.getStartOffset() < offset && current.getEndOffset() >= offset) {
        return current;
      }
      current = current.getParent();
    }
    return null;
  }

  @Nullable
  private AbstractBlockWrapper getParentFor(final int offset, LeafBlockWrapper block) {
    AbstractBlockWrapper previous = getPreviousIncompleteBlock(block, offset);
    if (previous != null) {
      return getLastNestedCompositeBlockForSameRange(previous);
    }
    else {
      return getParentFor(offset, (AbstractBlockWrapper)block);
    }
  }

  @Nullable
  private AbstractBlockWrapper getPreviousIncompleteBlock(final LeafBlockWrapper block, final int offset) {
    if (block == null) {
      if (myLastTokenBlock.isIncomplete()) {
        return myLastTokenBlock;
      }
      else {
        return null;
      }
    }

    AbstractBlockWrapper current = block;
    while (current.getParent() != null && current.getParent().getStartOffset() > offset) {
      current = current.getParent();
    }

    if (current.getParent() == null) return null;

    if (current.getEndOffset() <= offset) {
      while (!current.isIncomplete() &&
             current.getParent() != null &&
             current.getParent().getEndOffset() <= offset) {
        current = current.getParent();
      }
      if (current.isIncomplete()) return current;
    }

    if (current.getParent() == null) return null;

    final List<AbstractBlockWrapper> subBlocks = current.getParent().getChildren();
    final int index = subBlocks.indexOf(current);
    if (index < 0) {
      LOG.assertTrue(false);
    }
    if (index == 0) return null;

    AbstractBlockWrapper currentResult = subBlocks.get(index - 1);
    if (!currentResult.isIncomplete()) return null;

    AbstractBlockWrapper lastChild = getLastChildOf(currentResult);
    while (lastChild != null && lastChild.isIncomplete()) {
      currentResult = lastChild;
      lastChild = getLastChildOf(currentResult);
    }
    return currentResult;
  }

  @Nullable
  private static AbstractBlockWrapper getLastChildOf(final AbstractBlockWrapper currentResult) {
    AbstractBlockWrapper parentBlockToUse = getLastNestedCompositeBlockForSameRange(currentResult);
    if (!(parentBlockToUse instanceof CompositeBlockWrapper)) return null;
    final List<AbstractBlockWrapper> subBlocks = ((CompositeBlockWrapper)parentBlockToUse).getChildren();
    if (subBlocks.isEmpty()) return null;
    return subBlocks.get(subBlocks.size() - 1);
  }

  /**
   * There is a possible case that particular block is a composite block that contains number of nested composite blocks
   * that all target the same text range. This method allows to derive the most nested block that shares the same range (if any).
   *
   * @param block   block to check
   * @return        the most nested block of the given one that shares the same text range if any; given block otherwise
   */
  @NotNull
  private static AbstractBlockWrapper getLastNestedCompositeBlockForSameRange(@NotNull final AbstractBlockWrapper block) {
    if (!(block instanceof CompositeBlockWrapper)) {
      return block;
    }

    AbstractBlockWrapper result = block;
    AbstractBlockWrapper candidate = block;
    while (true) {
      List<AbstractBlockWrapper> subBlocks = ((CompositeBlockWrapper)candidate).getChildren();
      if (subBlocks == null || subBlocks.size() != 1) {
        break;
      }

      candidate = subBlocks.get(0);
      if (candidate.getStartOffset() == block.getStartOffset() && candidate.getEndOffset() == block.getEndOffset()
          && candidate instanceof CompositeBlockWrapper)
      {
        result = candidate;
      }
      else {
        break;
      }
    }
    return result;
  }

  private void processBlocksBefore(final int offset) {
    while (true) {
      myAlignAgain.clear();
      myCurrentBlock = myFirstTokenBlock;
      while (myCurrentBlock != null && myCurrentBlock.getStartOffset() < offset) {
        processToken();
        if (myCurrentBlock == null) {
          myCurrentBlock = myLastTokenBlock;
          if (myCurrentBlock != null) {
            myProgressCallback.afterProcessingBlock(myCurrentBlock);
          }
          break;
        }
      }
      if (myAlignAgain.isEmpty()) return;
      reset();
    }
  }

  public LeafBlockWrapper getFirstTokenBlock() {
    return myFirstTokenBlock;
  }

  public WhiteSpace getLastWhiteSpace() {
    return myLastWhiteSpace;
  }

  private class AdjustWhiteSpacesState extends State {

    @Override
    protected void doIteration() {
      LeafBlockWrapper blockToProcess = myCurrentBlock;
      processToken();
      if (blockToProcess != null) {
        myProgressCallback.afterProcessingBlock(blockToProcess);
      }

      if (myCurrentBlock != null) {
        return;
      }

      if (myAlignAgain.isEmpty()) {
        setDone(true);
      }
      else {
        myAlignAgain.clear();
        myDependentSpacingEngine.clear();
        myCurrentBlock = myFirstTokenBlock;
      }
    }
  }

  public static class FormatOptions {
    public CodeStyleSettings mySettings;
    public CommonCodeStyleSettings.IndentOptions myIndentOptions;

    public FormatTextRanges myAffectedRanges;
    public boolean myReformatContext;

    public int myInterestingOffset;

    public FormatOptions(CodeStyleSettings settings,
                         CommonCodeStyleSettings.IndentOptions options,
                         FormatTextRanges ranges,
                         boolean reformatContext) {
      this(settings, options, ranges, reformatContext, -1);
    }

    public FormatOptions(CodeStyleSettings settings,
                         CommonCodeStyleSettings.IndentOptions options,
                         FormatTextRanges ranges,
                         boolean reformatContext,
                         int interestingOffset) {
      mySettings = settings;
      myIndentOptions = options;
      myAffectedRanges = ranges;
      myReformatContext = reformatContext;
      myInterestingOffset = interestingOffset;
    }
  }

  private class ExpandChildrenIndent extends State {
    private Iterator<ExpandableIndent> myIterator;
    private MultiMap<Alignment, LeafBlockWrapper> myBlocksToRealign = new MultiMap<Alignment, LeafBlockWrapper>();

    @Override
    protected void doIteration() {
      if (myIterator == null) {
        myIterator = myExpandableIndents.keySet().iterator();
      }
      if (!myIterator.hasNext()) {
        setDone(true);
        return;
      }

      final ExpandableIndent indent = myIterator.next();
      Collection<AbstractBlockWrapper> blocksToExpandIndent = myExpandableIndents.get(indent);
      if (shouldExpand(blocksToExpandIndent)) {
        for (AbstractBlockWrapper block : blocksToExpandIndent) {
          indent.setEnforceIndent(true);
          reindentNewLineChildren(block);
          indent.setEnforceIndent(false);
        }
      }

      restoreAlignments(myBlocksToRealign);
      myBlocksToRealign.clear();
    }

    private void restoreAlignments(MultiMap<Alignment, LeafBlockWrapper> blocks) {
      for (Alignment alignment : blocks.keySet()) {
        AlignmentImpl alignmentImpl = (AlignmentImpl)alignment;
        if (!alignmentImpl.isAllowBackwardShift()) continue;

        Set<LeafBlockWrapper> toRealign = alignmentImpl.getOffsetResponsibleBlocks();
        arrangeSpaces(toRealign);

        LeafBlockWrapper rightMostBlock = getRightMostBlock(toRealign);
        int maxSpacesBeforeBlock = rightMostBlock.getNumberOfSymbolsBeforeBlock().getTotalSpaces();
        int rightMostBlockLine = myDocument.getLineNumber(rightMostBlock.getStartOffset());

        for (LeafBlockWrapper block : toRealign) {
          int currentBlockLine = myDocument.getLineNumber(block.getStartOffset());
          if (currentBlockLine == rightMostBlockLine) continue;

          int blockIndent = block.getNumberOfSymbolsBeforeBlock().getTotalSpaces();
          int delta = maxSpacesBeforeBlock - blockIndent;
          if (delta > 0) {
            int newSpaces = block.getWhiteSpace().getTotalSpaces() + delta;
            adjustSpacingToKeepAligned(block, newSpaces);
          }
        }
      }
    }

    private void adjustSpacingToKeepAligned(LeafBlockWrapper block, int newSpaces) {
      WhiteSpace space = block.getWhiteSpace();
      SpacingImpl property = block.getSpaceProperty();
      if (property == null) return;
      space.arrangeSpaces(new SpacingImpl(newSpaces, newSpaces,
                                          property.getMinLineFeeds(),
                                          property.isReadOnly(),
                                          property.isSafe(),
                                          property.shouldKeepLineFeeds(),
                                          property.getKeepBlankLines(),
                                          property.shouldKeepFirstColumn(),
                                          property.getPrefLineFeeds()));
    }

    private LeafBlockWrapper getRightMostBlock(Collection<LeafBlockWrapper> toRealign) {
      int maxSpacesBeforeBlock = -1;
      LeafBlockWrapper rightMostBlock = null;

      for (LeafBlockWrapper block : toRealign) {
        int spaces = block.getNumberOfSymbolsBeforeBlock().getTotalSpaces();
        if (spaces > maxSpacesBeforeBlock) {
          maxSpacesBeforeBlock = spaces;
          rightMostBlock = block;
        }
      }

      return rightMostBlock;
    }

    private void arrangeSpaces(Collection<LeafBlockWrapper> toRealign) {
      for (LeafBlockWrapper block : toRealign) {
        WhiteSpace whiteSpace = block.getWhiteSpace();
        SpacingImpl spacing = block.getSpaceProperty();
        whiteSpace.arrangeSpaces(spacing);
      }
    }

    private boolean shouldExpand(Collection<AbstractBlockWrapper> blocksToExpandIndent) {
      AbstractBlockWrapper last = null;
      for (AbstractBlockWrapper block : blocksToExpandIndent) {
        if (block.getWhiteSpace().containsLineFeeds()) {
          return true;
        }
        last = block;
      }

      if (last != null) {
        AbstractBlockWrapper next = getNextBlock(last);
        if (next != null && next.getWhiteSpace().containsLineFeeds()) {
          int nextNewLineBlockIndent = next.getNumberOfSymbolsBeforeBlock().getTotalSpaces();
          if (nextNewLineBlockIndent >= finMinNewLineIndent(blocksToExpandIndent)) {
            return true;
          }
        }
      }

      return false;
    }

    private int finMinNewLineIndent(@NotNull Collection<AbstractBlockWrapper> wrappers) {
      int totalMinimum = Integer.MAX_VALUE;
      for (AbstractBlockWrapper wrapper : wrappers) {
        int minNewLineIndent = findMinNewLineIndent(wrapper);
        if (minNewLineIndent < totalMinimum) {
          totalMinimum = minNewLineIndent;
        }
      }
      return totalMinimum;
    }

    private int findMinNewLineIndent(@NotNull AbstractBlockWrapper block) {
      if (block instanceof LeafBlockWrapper && block.getWhiteSpace().containsLineFeeds()) {
        return block.getNumberOfSymbolsBeforeBlock().getTotalSpaces();
      }
      else if (block instanceof CompositeBlockWrapper) {
        List<AbstractBlockWrapper> children = ((CompositeBlockWrapper)block).getChildren();
        int currentMin = Integer.MAX_VALUE;
        for (AbstractBlockWrapper child : children) {
          int childIndent = findMinNewLineIndent(child);
          if (childIndent < currentMin) {
            currentMin = childIndent;
          }
        }
        return currentMin;
      }
      return Integer.MAX_VALUE;
    }

    private AbstractBlockWrapper getNextBlock(AbstractBlockWrapper block) {
      List<AbstractBlockWrapper> children = block.getParent().getChildren();
      int nextBlockIndex = children.indexOf(block) + 1;
      if (nextBlockIndex < children.size()) {
        return children.get(nextBlockIndex);
      }
      return null;
    }

    private void reindentNewLineChildren(final @NotNull AbstractBlockWrapper block) {
      if (block instanceof LeafBlockWrapper) {
        WhiteSpace space = block.getWhiteSpace();

        if (space.containsLineFeeds()) {
          myCurrentBlock = (LeafBlockWrapper)block;
          adjustIndentAndContinue();
          storeAlignmentsAfterCurrentBlock();
        }
      }
      else if (block instanceof CompositeBlockWrapper) {
        List<AbstractBlockWrapper> children = ((CompositeBlockWrapper)block).getChildren();
        for (AbstractBlockWrapper childBlock : children) {
          reindentNewLineChildren(childBlock);
        }
      }
    }

    private void storeAlignmentsAfterCurrentBlock() {
      LeafBlockWrapper current = myCurrentBlock.getNextBlock();
      while (current != null && !current.getWhiteSpace().containsLineFeeds()) {
        if (current.getAlignment() != null) {
          myBlocksToRealign.putValue(current.getAlignment(), current);
        }
        current = current.getNextBlock();
      }
    }
  }
}