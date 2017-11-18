/*
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
package com.facebook.presto.operator.aggregation;

import com.facebook.presto.byteCode.Block;
import com.facebook.presto.byteCode.ClassDefinition;
import com.facebook.presto.byteCode.ClassInfoLoader;
import com.facebook.presto.byteCode.CompilerContext;
import com.facebook.presto.byteCode.DumpByteCodeVisitor;
import com.facebook.presto.byteCode.DynamicClassLoader;
import com.facebook.presto.byteCode.FieldDefinition;
import com.facebook.presto.byteCode.LocalVariableDefinition;
import com.facebook.presto.byteCode.MethodDefinition;
import com.facebook.presto.byteCode.NamedParameterDefinition;
import com.facebook.presto.byteCode.SmartClassWriter;
import com.facebook.presto.byteCode.control.ForLoop;
import com.facebook.presto.operator.GroupByIdBlock;
import com.facebook.presto.operator.Page;
import com.facebook.presto.operator.aggregation.state.AccumulatorStateFactory;
import com.facebook.presto.operator.aggregation.state.AccumulatorStateSerializer;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.block.BlockBuilderStatus;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.sql.gen.CompilerOperations;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.airlift.slice.Slice;
import org.objectweb.asm.ClassWriter;

import javax.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.facebook.presto.byteCode.Access.FINAL;
import static com.facebook.presto.byteCode.Access.PRIVATE;
import static com.facebook.presto.byteCode.Access.PUBLIC;
import static com.facebook.presto.byteCode.Access.a;
import static com.facebook.presto.byteCode.NamedParameterDefinition.arg;
import static com.facebook.presto.byteCode.OpCodes.NOP;
import static com.facebook.presto.byteCode.ParameterizedType.type;
import static com.facebook.presto.byteCode.ParameterizedType.typeFromPathName;
import static com.facebook.presto.byteCode.control.IfStatement.IfStatementBuilder;
import static com.facebook.presto.byteCode.control.IfStatement.ifStatementBuilder;
import static com.facebook.presto.operator.aggregation.AggregationMetadata.ParameterMetadata;
import static com.facebook.presto.operator.aggregation.AggregationMetadata.ParameterMetadata.ParameterType.BLOCK_INDEX;
import static com.facebook.presto.operator.aggregation.AggregationMetadata.countInputChannels;
import static com.google.common.base.Preconditions.checkArgument;

public class AccumulatorCompiler
{
    private static final boolean DUMP_BYTE_CODE_TREE = false;

    private static final AtomicLong CLASS_ID = new AtomicLong();

    public AccumulatorFactory generateAccumulatorFactory(AggregationMetadata metadata, DynamicClassLoader classLoader)
    {
        Class<? extends Accumulator> accumulatorClass = generateAccumulatorClass(
                Accumulator.class,
                metadata,
                classLoader);

        Class<? extends GroupedAccumulator> groupedAccumulatorClass = generateAccumulatorClass(
                GroupedAccumulator.class,
                metadata,
                classLoader);

        return new GenericAccumulatorFactory(
                metadata.getStateSerializer(),
                metadata.getStateFactory(),
                accumulatorClass,
                groupedAccumulatorClass,
                metadata.isApproximate());
    }

    private static <T> Class<? extends T> generateAccumulatorClass(
            Class<T> accumulatorInterface,
            AggregationMetadata metadata,
            DynamicClassLoader classLoader)
    {
        boolean grouped = accumulatorInterface == GroupedAccumulator.class;

        ClassDefinition definition = new ClassDefinition(new CompilerContext(null),
                a(PUBLIC, FINAL),
                typeFromPathName(metadata.getName() + accumulatorInterface.getSimpleName() + "_" + CLASS_ID.incrementAndGet()),
                type(Object.class),
                type(accumulatorInterface));

        AccumulatorStateSerializer<?> stateSerializer = metadata.getStateSerializer();
        AccumulatorStateFactory<?> stateFactory = metadata.getStateFactory();

        FieldDefinition stateSerializerField = definition.declareField(a(PRIVATE, FINAL), "stateSerializer", stateSerializer.getClass());
        FieldDefinition stateFactoryField = definition.declareField(a(PRIVATE, FINAL), "stateFactory", stateFactory.getClass());
        FieldDefinition inputChannelsField = definition.declareField(a(PRIVATE, FINAL), "inputChannels", type(List.class, Integer.class));
        FieldDefinition maskChannelField = definition.declareField(a(PRIVATE, FINAL), "maskChannel", type(Optional.class, Integer.class));
        FieldDefinition sampleWeightChannelField = definition.declareField(a(PRIVATE, FINAL), "sampleWeightChannel", type(Optional.class, Integer.class));
        FieldDefinition confidenceField = definition.declareField(a(PRIVATE, FINAL), "confidence", double.class);
        FieldDefinition stateField = definition.declareField(a(PRIVATE, FINAL), "state", grouped ? stateFactory.getGroupedStateClass() : stateFactory.getSingleStateClass());

        // Generate constructor
        generateConstructor(
                definition,
                stateSerializerField,
                stateFactoryField,
                inputChannelsField,
                maskChannelField,
                sampleWeightChannelField,
                confidenceField,
                stateField,
                grouped);

        // Generate methods
        generateAddInput(definition, stateField, inputChannelsField, maskChannelField, sampleWeightChannelField, metadata.getInputMetadata(), metadata.getInputFunction(), grouped);
        generateGetEstimatedSize(definition, stateField);
        MethodDefinition getIntermediateType = generateGetIntermediateType(definition, stateSerializer.getSerializedType());
        MethodDefinition getFinalType = generateGetFinalType(definition, metadata.getOutputType());

        if (metadata.getIntermediateInputFunction() == null) {
            generateAddIntermediateAsCombine(definition, stateField, stateSerializerField, stateFactoryField, metadata.getCombineFunction(), stateFactory.getSingleStateClass(), grouped);
        }
        else {
            generateAddIntermediateAsIntermediateInput(definition, stateField, metadata.getIntermediateInputMetadata(), metadata.getIntermediateInputFunction(), grouped);
        }

        if (grouped) {
            generateGroupedEvaluateIntermediate(definition, stateSerializerField, stateField);
        }
        else {
            generateEvaluateIntermediate(definition, getIntermediateType, stateSerializerField, stateField);
        }

        if (grouped) {
            generateGroupedEvaluateFinal(definition, confidenceField, stateSerializerField, stateField, metadata.getOutputFunction(), metadata.isApproximate());
        }
        else {
            generateEvaluateFinal(definition, getFinalType, confidenceField, stateSerializerField, stateField, metadata.getOutputFunction(), metadata.isApproximate());
        }

        return defineClass(definition, accumulatorInterface, classLoader);
    }

    private static MethodDefinition generateGetIntermediateType(ClassDefinition definition, Type type)
    {
        MethodDefinition methodDefinition = definition.declareMethod(new CompilerContext(null), a(PUBLIC), "getIntermediateType", type(Type.class));

        methodDefinition.getBody()
                .push(type.getClass())
                .invokeStatic(type.getClass(), "getInstance", type.getClass())
                .retObject();

        return methodDefinition;
    }

    private static MethodDefinition generateGetFinalType(ClassDefinition definition, Type type)
    {
        MethodDefinition methodDefinition = definition.declareMethod(new CompilerContext(null), a(PUBLIC), "getFinalType", type(Type.class));

        methodDefinition.getBody()
                .push(type.getClass())
                .invokeStatic(type.getClass(), "getInstance", type.getClass())
                .retObject();

        return methodDefinition;
    }

    private static void generateGetEstimatedSize(ClassDefinition definition, FieldDefinition stateField)
    {
        definition.declareMethod(new CompilerContext(null), a(PUBLIC), "getEstimatedSize", type(long.class))
                .getBody()
                .pushThis()
                .getField(stateField)
                .invokeVirtual(stateField.getType(), "getEstimatedSize", type(long.class))
                .retLong();
    }

    private static void generateAddInput(
            ClassDefinition definition,
            FieldDefinition stateField,
            FieldDefinition inputChannelsField,
            FieldDefinition maskChannelField,
            FieldDefinition sampleWeightChannelField,
            List<ParameterMetadata> parameterMetadatas,
            Method inputFunction,
            boolean grouped)
    {
        CompilerContext context = new CompilerContext(null);

        ImmutableList.Builder<NamedParameterDefinition> parameters = ImmutableList.builder();
        if (grouped) {
            parameters.add(arg("groupIdsBlock", GroupByIdBlock.class));
        }
        parameters.add(arg("page", Page.class));

        Block body = definition.declareMethod(context, a(PUBLIC), "addInput", type(void.class), parameters.build())
                .getBody();

        if (grouped) {
            generateEnsureCapacity(stateField, body);
        }

        List<LocalVariableDefinition> parameterVariables = new ArrayList<>();
        for (int i = 0; i < countInputChannels(parameterMetadatas); i++) {
            parameterVariables.add(context.declareVariable(com.facebook.presto.spi.block.Block.class, "block" + i));
        }
        LocalVariableDefinition masksBlock = context.declareVariable(com.facebook.presto.spi.block.Block.class, "masksBlock");
        LocalVariableDefinition sampleWeightsBlock = context.declareVariable(com.facebook.presto.spi.block.Block.class, "sampleWeightsBlock");

        body.comment("masksBlock = maskChannel.transform(page.blockGetter()).orNull();")
                .pushThis()
                .getField(maskChannelField)
                .getVariable("page")
                .invokeVirtual(type(Page.class), "blockGetter", type(Function.class, Integer.class, com.facebook.presto.spi.block.Block.class))
                .invokeVirtual(Optional.class, "transform", Optional.class, Function.class)
                .invokeVirtual(Optional.class, "orNull", Object.class)
                .checkCast(com.facebook.presto.spi.block.Block.class)
                .putVariable(masksBlock);

        body.comment("sampleWeightsBlock = sampleWeightChannel.transform(page.blockGetter()).orNull();")
                .pushThis()
                .getField(sampleWeightChannelField)
                .getVariable("page")
                .invokeVirtual(type(Page.class), "blockGetter", type(Function.class, Integer.class, com.facebook.presto.spi.block.Block.class))
                .invokeVirtual(Optional.class, "transform", Optional.class, Function.class)
                .invokeVirtual(Optional.class, "orNull", Object.class)
                .checkCast(com.facebook.presto.spi.block.Block.class)
                .putVariable(sampleWeightsBlock);

        // Get all parameter blocks
        for (int i = 0; i < countInputChannels(parameterMetadatas); i++) {
            body.comment("%s = page.getBlock(inputChannels.get(%d));", parameterVariables.get(i).getName(), i)
                    .getVariable("page")
                    .pushThis()
                    .getField(inputChannelsField)
                    .push(i)
                    .invokeInterface(List.class, "get", Object.class, int.class)
                    .checkCast(Integer.class)
                    .invokeVirtual(Integer.class, "intValue", int.class)
                    .invokeVirtual(Page.class, "getBlock", com.facebook.presto.spi.block.Block.class, int.class)
                    .putVariable(parameterVariables.get(i));
        }
        Block block = generateInputForLoop(stateField, parameterMetadatas, inputFunction, context, parameterVariables, masksBlock, sampleWeightsBlock, grouped);

        body.append(block)
                .ret();
    }

    private static Block generateInputForLoop(
            FieldDefinition stateField,
            List<ParameterMetadata> parameterMetadatas,
            Method inputFunction,
            CompilerContext context,
            List<LocalVariableDefinition> parameterVariables,
            LocalVariableDefinition masksBlock,
            LocalVariableDefinition sampleWeightsBlock,
            boolean grouped)
    {
        // For-loop over rows
        LocalVariableDefinition positionVariable = context.declareVariable(int.class, "position");
        LocalVariableDefinition sampleWeightVariable = context.declareVariable(long.class, "sampleWeight");
        LocalVariableDefinition rowsVariable = context.declareVariable(int.class, "rows");

        Block block = new Block(context)
                .getVariable("page")
                .invokeVirtual(Page.class, "getPositionCount", int.class)
                .putVariable(rowsVariable)
                .initializeVariable(positionVariable)
                .initializeVariable(sampleWeightVariable);

        Block loopBody = generateInvokeInputFunction(context, stateField, positionVariable, sampleWeightVariable, parameterVariables, parameterMetadatas, inputFunction, grouped);

        //  Wrap with null checks
        for (LocalVariableDefinition variable : parameterVariables) {
            IfStatementBuilder builder = ifStatementBuilder(context);
            builder.comment("if(!%s.isNull(position))", variable.getName())
                    .condition(new Block(context)
                            .getVariable(variable)
                            .getVariable(positionVariable)
                            .invokeInterface(com.facebook.presto.spi.block.Block.class, "isNull", boolean.class, int.class))
                    .ifTrue(NOP)
                    .ifFalse(loopBody);
            loopBody = new Block(context).append(builder.build());
        }

        // Check that sample weight is > 0 (also checks the mask)
        loopBody = generateComputeSampleWeightAndCheckGreaterThanZero(context, loopBody, sampleWeightVariable, masksBlock, sampleWeightsBlock, positionVariable);

        block.append(new ForLoop.ForLoopBuilder(context)
                .initialize(new Block(context).putVariable(positionVariable, 0))
                .condition(new Block(context)
                        .getVariable(positionVariable)
                        .getVariable(rowsVariable)
                        .invokeStatic(CompilerOperations.class, "lessThan", boolean.class, int.class, int.class))
                .update(new Block(context).incrementVariable(positionVariable, (byte) 1))
                .body(loopBody)
                .build());

        return block;
    }

    private static Block generateComputeSampleWeightAndCheckGreaterThanZero(CompilerContext context, Block body, LocalVariableDefinition sampleWeight, LocalVariableDefinition masks, LocalVariableDefinition sampleWeights, LocalVariableDefinition position)
    {
        Block block = new Block(context)
                .comment("sampleWeight = computeSampleWeight(masks, sampleWeights, position);")
                .getVariable(masks)
                .getVariable(sampleWeights)
                .getVariable(position)
                .invokeStatic(ApproximateUtils.class, "computeSampleWeight", long.class, com.facebook.presto.spi.block.Block.class, com.facebook.presto.spi.block.Block.class, int.class)
                .putVariable(sampleWeight);

        IfStatementBuilder builder = ifStatementBuilder(context);
        builder.comment("if(sampleWeight > 0)")
                .condition(new Block(context)
                        .getVariable(sampleWeight)
                        .invokeStatic(CompilerOperations.class, "longGreaterThanZero", boolean.class, long.class))
                .ifTrue(body)
                .ifFalse(NOP);

        return block.append(builder.build());
    }

    private static Block generateInvokeInputFunction(
            CompilerContext context,
            FieldDefinition stateField,
            LocalVariableDefinition position,
            LocalVariableDefinition sampleWeight,
            List<LocalVariableDefinition> parameterVariables,
            List<ParameterMetadata> parameterMetadatas,
            Method inputFunction,
            boolean grouped)
    {
        Block block = new Block(context);

        if (grouped) {
            generateSetGroupIdFromGroupIdsBlock(stateField, position, block);
        }

        block.comment("Call input function with unpacked Block arguments");

        Class<?>[] parameters = inputFunction.getParameterTypes();
        int inputChannel = 0;
        for (int i = 0; i < parameters.length; i++) {
            ParameterMetadata parameterMetadata = parameterMetadatas.get(i);
            switch (parameterMetadata.getParameterType()) {
                case STATE:
                    block.pushThis().getField(stateField);
                    break;
                case BLOCK_INDEX:
                    block.getVariable(position);
                    break;
                case SAMPLE_WEIGHT:
                    block.getVariable(sampleWeight);
                    break;
                case INPUT_CHANNEL:
                    Block getBlockByteCode = new Block(context)
                            .getVariable(parameterVariables.get(inputChannel));
                    pushStackType(block, parameterMetadata.getSqlType(), getBlockByteCode, parameters[i]);
                    inputChannel++;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported parameter type: " + parameterMetadata.getParameterType());
            }
        }

        block.invokeStatic(inputFunction);
        return block;
    }

    // Assumes that there is a variable named 'position' in the block, which is the current index
    private static void pushStackType(Block block, Class<? extends Type> sqlType, Block getBlockByteCode, Class<?> parameter)
    {
        if (parameter == com.facebook.presto.spi.block.Block.class) {
            block.append(getBlockByteCode);
        }
        else if (parameter == long.class) {
            block.comment("%s.getLong(block, position)", sqlType.getSimpleName())
                    .invokeStatic(sqlType, "getInstance", sqlType)
                    .append(getBlockByteCode)
                    .getVariable("position")
                    .invokeVirtual(sqlType, "getLong", long.class, com.facebook.presto.spi.block.Block.class, int.class);
        }
        else if (parameter == double.class) {
            block.comment("%s.getDouble(block, position)", sqlType.getSimpleName())
                    .invokeStatic(sqlType, "getInstance", sqlType)
                    .append(getBlockByteCode)
                    .getVariable("position")
                    .invokeVirtual(sqlType, "getDouble", double.class, com.facebook.presto.spi.block.Block.class, int.class);
        }
        else if (parameter == boolean.class) {
            block.comment("%s.getBoolean(block, position)", sqlType.getSimpleName())
                    .invokeStatic(sqlType, "getInstance", sqlType)
                    .append(getBlockByteCode)
                    .getVariable("position")
                    .invokeVirtual(sqlType, "getBoolean", boolean.class, com.facebook.presto.spi.block.Block.class, int.class);
        }
        else if (parameter == Slice.class) {
            block.comment("%s.getBoolean(block, position)", sqlType.getSimpleName())
                    .invokeStatic(sqlType, "getInstance", sqlType)
                    .append(getBlockByteCode)
                    .getVariable("position")
                    .invokeVirtual(sqlType, "getSlice", Slice.class, com.facebook.presto.spi.block.Block.class, int.class);
        }
        else {
            throw new IllegalArgumentException("Unsupported parameter type: " + parameter.getSimpleName());
        }
    }

    private static void generateAddIntermediateAsCombine(
            ClassDefinition definition,
            FieldDefinition stateField,
            FieldDefinition stateSerializerField,
            FieldDefinition stateFactoryField,
            Method combineFunction,
            Class<?> singleStateClass,
            boolean grouped)
    {
        CompilerContext context = new CompilerContext(null);

        Block body = declareAddIntermediate(definition, grouped, context);

        LocalVariableDefinition scratchStateVariable = context.declareVariable(singleStateClass, "scratchState");
        LocalVariableDefinition positionVariable = context.declareVariable(int.class, "position");

        body.comment("scratchState = stateFactory.createSingleState();")
                .pushThis()
                .getField(stateFactoryField)
                .invokeInterface(AccumulatorStateFactory.class, "createSingleState", Object.class)
                .checkCast(scratchStateVariable.getType())
                .putVariable(scratchStateVariable);

        if (grouped) {
            generateEnsureCapacity(stateField, body);
        }

        Block loopBody = new Block(context);

        if (grouped) {
            generateSetGroupIdFromGroupIdsBlock(stateField, positionVariable, loopBody);
        }

        loopBody.comment("stateSerializer.deserialize(block, position, scratchState)")
                .pushThis()
                .getField(stateSerializerField)
                .getVariable("block")
                .getVariable(positionVariable)
                .getVariable(scratchStateVariable)
                .invokeInterface(AccumulatorStateSerializer.class, "deserialize", void.class, com.facebook.presto.spi.block.Block.class, int.class, Object.class);

        loopBody.comment("combine(state, scratchState)")
                .pushThis()
                .getField(stateField)
                .getVariable("scratchState")
                .invokeStatic(combineFunction);

        body.append(generateBlockNonNullPositionForLoop(context, positionVariable, loopBody))
                .ret();
    }

    private static void generateSetGroupIdFromGroupIdsBlock(FieldDefinition stateField, LocalVariableDefinition positionVariable, Block block)
    {
        block.comment("state.setGroupId(groupIdsBlock.getGroupId(position))")
                .pushThis()
                .getField(stateField)
                .getVariable("groupIdsBlock")
                .getVariable(positionVariable)
                .invokeVirtual(GroupByIdBlock.class, "getGroupId", long.class, int.class)
                .invokeVirtual(stateField.getType(), "setGroupId", type(void.class), type(long.class));
    }

    private static void generateEnsureCapacity(FieldDefinition stateField, Block block)
    {
        block.comment("state.ensureCapacity(groupIdsBlock.getGroupCount())")
                .pushThis()
                .getField(stateField)
                .getVariable("groupIdsBlock")
                .invokeVirtual(GroupByIdBlock.class, "getGroupCount", long.class)
                .invokeVirtual(stateField.getType(), "ensureCapacity", type(void.class), type(long.class));
    }

    private static Block declareAddIntermediate(ClassDefinition definition, boolean grouped, CompilerContext context)
    {
        ImmutableList.Builder<NamedParameterDefinition> parameters = ImmutableList.builder();
        if (grouped) {
            parameters.add(arg("groupIdsBlock", GroupByIdBlock.class));
        }
        parameters.add(arg("block", com.facebook.presto.spi.block.Block.class));

        return definition.declareMethod(
                context,
                a(PUBLIC),
                "addIntermediate",
                type(void.class),
                parameters.build())
                .getBody();
    }

    private static void generateAddIntermediateAsIntermediateInput(
            ClassDefinition definition,
            FieldDefinition stateField,
            List<ParameterMetadata> parameterMetadatas,
            Method intermediateInputFunction,
            boolean grouped)
    {
        CompilerContext context = new CompilerContext(null);

        Block body = declareAddIntermediate(definition, grouped, context);

        if (grouped) {
            generateEnsureCapacity(stateField, body);
        }

        LocalVariableDefinition positionVariable = context.declareVariable(int.class, "position");

        Block loopBody = new Block(context)
                .comment("<intermediate>(state, ...)")
                .pushThis()
                .getField(stateField);

        if (grouped) {
            generateSetGroupIdFromGroupIdsBlock(stateField, positionVariable, loopBody);
        }

        Class<?>[] parameters = intermediateInputFunction.getParameterTypes();
        // Parameter 0 is the state
        for (int i = 1; i < parameters.length; i++) {
            ParameterMetadata parameterMetadata = parameterMetadatas.get(i);
            if (parameterMetadata.getParameterType() == BLOCK_INDEX) {
                loopBody.getVariable("position");
            }
            else  {
                Block getBlockByteCode = new Block(context)
                        .getVariable("block");
                pushStackType(loopBody, parameterMetadata.getSqlType(), getBlockByteCode, parameters[i]);
            }
        }
        loopBody.invokeStatic(intermediateInputFunction);

        body.append(generateBlockNonNullPositionForLoop(context, positionVariable, loopBody))
                .ret();
    }

    // Generates a for-loop with a local variable named "position" defined, with the current position in the block,
    // loopBody will only be executed for non-null positions in the Block
    private static Block generateBlockNonNullPositionForLoop(CompilerContext context, LocalVariableDefinition positionVariable, Block loopBody)
    {
        LocalVariableDefinition rowsVariable = context.declareVariable(int.class, "rows");

        Block block = new Block(context)
                .getVariable("block")
                .invokeInterface(com.facebook.presto.spi.block.Block.class, "getPositionCount", int.class)
                .putVariable(rowsVariable);

        IfStatementBuilder builder = ifStatementBuilder(context);
        builder.comment("if(!block.isNull(position))")
                .condition(new Block(context)
                        .getVariable("block")
                        .getVariable(positionVariable)
                        .invokeInterface(com.facebook.presto.spi.block.Block.class, "isNull", boolean.class, int.class))
                .ifTrue(NOP)
                .ifFalse(loopBody);

        block.append(new ForLoop.ForLoopBuilder(context)
                .initialize(new Block(context).putVariable(positionVariable, 0))
                .condition(new Block(context)
                        .getVariable(positionVariable)
                        .getVariable(rowsVariable)
                        .invokeStatic(CompilerOperations.class, "lessThan", boolean.class, int.class, int.class))
                .update(new Block(context).incrementVariable(positionVariable, (byte) 1))
                .body(builder.build())
                .build());

        return block;
    }

    private static void generateGroupedEvaluateIntermediate(ClassDefinition definition, FieldDefinition stateSerializerField, FieldDefinition stateField)
    {
        definition.declareMethod(
                new CompilerContext(null),
                a(PUBLIC),
                "evaluateIntermediate",
                type(void.class),
                arg("groupId", int.class),
                arg("out", BlockBuilder.class))
                .getBody()
                .comment("state.setGroupId(groupId)")
                .pushThis()
                .getField(stateField)
                .getVariable("groupId")
                .intToLong()
                .invokeVirtual(stateField.getType(), "setGroupId", type(void.class), type(long.class))

                .comment("stateSerializer.serialize(state, out)")
                .pushThis()
                .getField(stateSerializerField)
                .pushThis()
                .getField(stateField)
                .getVariable("out")
                .invokeInterface(AccumulatorStateSerializer.class, "serialize", void.class, Object.class, BlockBuilder.class)
                .ret();
    }

    private static void generateEvaluateIntermediate(ClassDefinition definition, MethodDefinition getIntermediateType, FieldDefinition stateSerializerField, FieldDefinition stateField)
    {
        CompilerContext context = new CompilerContext(null);
        Block body = definition.declareMethod(
                context,
                a(PUBLIC),
                "evaluateIntermediate",
                type(com.facebook.presto.spi.block.Block.class))
                .getBody();

        context.declareVariable(BlockBuilder.class, "out");

        body.comment("out = getIntermediateType().createBlockBuilder(new BlockBuilderStatus());")
                .pushThis()
                .invokeVirtual(getIntermediateType)
                .newObject(BlockBuilderStatus.class)
                .dup()
                .invokeConstructor(BlockBuilderStatus.class)
                .invokeInterface(Type.class, "createBlockBuilder", BlockBuilder.class, BlockBuilderStatus.class)
                .putVariable("out");

        body.comment("stateSerializer.serialize(state, out)")
                .pushThis()
                .getField(stateSerializerField)
                .pushThis()
                .getField(stateField)
                .getVariable("out")
                .invokeInterface(AccumulatorStateSerializer.class, "serialize", void.class, Object.class, BlockBuilder.class);

        body.comment("return out.build();")
                .getVariable("out")
                .invokeInterface(BlockBuilder.class, "build", com.facebook.presto.spi.block.Block.class)
                .retObject();
    }

    private static void generateGroupedEvaluateFinal(
            ClassDefinition definition,
            FieldDefinition confidenceField,
            FieldDefinition stateSerializerField,
            FieldDefinition stateField,
            @Nullable Method outputFunction,
            boolean approximate)
    {
        Block body = definition.declareMethod(
                new CompilerContext(null),
                a(PUBLIC),
                "evaluateFinal",
                type(void.class),
                arg("groupId", int.class),
                arg("out", BlockBuilder.class))
                .getBody()
                .comment("state.setGroupId(groupId)")
                .pushThis()
                .getField(stateField)
                .getVariable("groupId")
                .intToLong()
                .invokeVirtual(stateField.getType(), "setGroupId", type(void.class), type(long.class));

        if (outputFunction != null) {
            body.comment("output(state, out)")
                    .pushThis()
                    .getField(stateField);
            if (approximate) {
                body.pushThis().getField(confidenceField);
            }
            body.getVariable("out")
                    .invokeStatic(outputFunction);
        }
        else {
            checkArgument(!approximate, "Approximate aggregations must specify an output function");
            body.comment("stateSerializer.serialize(state, out)")
                    .pushThis()
                    .getField(stateSerializerField)
                    .pushThis()
                    .getField(stateField)
                    .getVariable("out")
                    .invokeInterface(AccumulatorStateSerializer.class, "serialize", void.class, Object.class, BlockBuilder.class);
        }
        body.ret();
    }

    private static void generateEvaluateFinal(
            ClassDefinition definition,
            MethodDefinition getFinalType,
            FieldDefinition confidenceField,
            FieldDefinition stateSerializerField,
            FieldDefinition stateField,
            @Nullable
            Method outputFunction,
            boolean approximate)
    {
        CompilerContext context = new CompilerContext(null);
        Block body = definition.declareMethod(
                context,
                a(PUBLIC),
                "evaluateFinal",
                type(com.facebook.presto.spi.block.Block.class))
                .getBody();

        context.declareVariable(BlockBuilder.class, "out");

        body.pushThis()
                .invokeVirtual(getFinalType)
                .newObject(BlockBuilderStatus.class)
                .dup()
                .invokeConstructor(BlockBuilderStatus.class)
                .invokeInterface(Type.class, "createBlockBuilder", BlockBuilder.class, BlockBuilderStatus.class)
                .putVariable("out");

        if (outputFunction != null) {
            body.comment("output(state, out)")
                    .pushThis()
                    .getField(stateField);
            if (approximate) {
                body.pushThis().getField(confidenceField);
            }
            body.getVariable("out")
                    .invokeStatic(outputFunction);
        }
        else {
            checkArgument(!approximate, "Approximate aggregations must specify an output function");
            body.comment("stateSerializer.serialize(state, out)")
                    .pushThis()
                    .getField(stateSerializerField)
                    .pushThis()
                    .getField(stateField)
                    .getVariable("out")
                    .invokeInterface(AccumulatorStateSerializer.class, "serialize", void.class, Object.class, BlockBuilder.class);
        }

        body.getVariable("out")
                .invokeInterface(BlockBuilder.class, "build", com.facebook.presto.spi.block.Block.class)
                .retObject();
    }

    private static void generateConstructor(
            ClassDefinition definition,
            FieldDefinition stateSerializerField,
            FieldDefinition stateFactoryField,
            FieldDefinition inputChannelsField,
            FieldDefinition maskChannelField,
            FieldDefinition sampleWeightChannelField,
            FieldDefinition confidenceField,
            FieldDefinition stateField,
            boolean grouped)
    {
        Block body = definition.declareConstructor(
                new CompilerContext(null),
                a(PUBLIC),
                arg("stateSerializer", AccumulatorStateSerializer.class),
                arg("stateFactory", AccumulatorStateFactory.class),
                arg("inputChannels", type(List.class, Integer.class)),
                arg("maskChannel", type(Optional.class, Integer.class)),
                arg("sampleWeightChannel", type(Optional.class, Integer.class)),
                arg("confidence", double.class))
                .getBody()
                .comment("super();")
                .pushThis()
                .invokeConstructor(Object.class);

        generateCastCheckNotNullAndAssign(body, stateSerializerField, "stateSerializer");
        generateCastCheckNotNullAndAssign(body, stateFactoryField, "stateFactory");
        generateCastCheckNotNullAndAssign(body, inputChannelsField, "inputChannels");
        generateCastCheckNotNullAndAssign(body, maskChannelField, "maskChannel");
        generateCastCheckNotNullAndAssign(body, sampleWeightChannelField, "sampleWeightChannel");

        String createState;
        if (grouped) {
            createState = "createGroupedState";
        }
        else {
            createState = "createSingleState";
        }

        body.comment("this.confidence = confidence")
                .pushThis()
                .getVariable("confidence")
                .putField(confidenceField)
                .comment("this.state = stateFactory.%s()", createState)
                .pushThis()
                .getVariable("stateFactory")
                .invokeInterface(AccumulatorStateFactory.class, createState, Object.class)
                .checkCast(stateField.getType())
                .putField(stateField)
                .ret();
    }

    private static void generateCastCheckNotNullAndAssign(Block block, FieldDefinition field, String variableName)
    {
        block.comment("this.%s = checkNotNull(%s, \"%s is null\"", field.getName(), variableName, variableName)
                .pushThis()
                .getVariable(variableName)
                .checkCast(field.getType())
                .push(variableName + " is null")
                .invokeStatic(Preconditions.class, "checkNotNull", Object.class, Object.class, Object.class)
                .checkCast(field.getType())
                .putField(field);
    }

    private static Map<String, Class<?>> defineClasses(List<ClassDefinition> classDefinitions, DynamicClassLoader classLoader)
    {
        ClassInfoLoader classInfoLoader = ClassInfoLoader.createClassInfoLoader(classDefinitions, classLoader);

        if (DUMP_BYTE_CODE_TREE) {
            DumpByteCodeVisitor dumpByteCode = new DumpByteCodeVisitor(System.out);
            for (ClassDefinition classDefinition : classDefinitions) {
                dumpByteCode.visitClass(classDefinition);
            }
        }

        Map<String, byte[]> byteCodes = new LinkedHashMap<>();
        for (ClassDefinition classDefinition : classDefinitions) {
            ClassWriter cw = new SmartClassWriter(classInfoLoader);
            classDefinition.visit(cw);
            byte[] byteCode = cw.toByteArray();
            byteCodes.put(classDefinition.getType().getJavaClassName(), byteCode);
        }

        return classLoader.defineClasses(byteCodes);
    }

    private static <T> Class<? extends T> defineClass(ClassDefinition classDefinition, Class<T> superType, DynamicClassLoader classLoader)
    {
        Class<?> clazz = defineClasses(ImmutableList.of(classDefinition), classLoader).values().iterator().next();
        return clazz.asSubclass(superType);
    }
}