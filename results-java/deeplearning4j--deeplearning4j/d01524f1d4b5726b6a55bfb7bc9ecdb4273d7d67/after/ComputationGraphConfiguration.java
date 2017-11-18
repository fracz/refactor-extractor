/*
 *
 *  * Copyright 2016 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
package org.deeplearning4j.nn.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import lombok.*;
import org.apache.commons.lang3.ClassUtils;
import org.deeplearning4j.nn.conf.graph.GraphVertex;
import org.deeplearning4j.nn.conf.graph.LayerVertex;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/** ComputationGraphConfiguration is a configuration object for neural networks with arbitrary connection structure.
 * It is analogous to {@link MultiLayerConfiguration}, but allows considerably greater flexibility for the network
 * architecture.<br>
 * Specifically, the network architecture is a directed acyclic graph, where each vertex in the graph is either a Layer,
 * or a {@link GraphVertex} object that defines arbitrary forward and backward pass functionality.<br>
 * Note that the ComputationGraph may have an arbitrary number of inputs (multiple independent inputs, possibly of different
 * types), and an arbitrary number of outputs (for example, multiple {@link org.deeplearning4j.nn.conf.layers.OutputLayer} instances.
 * Typical usage:<br>
 * {@code ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()....graphBuilder()...build();}
 *
 * @author Alex Black
 */
@Data @EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ComputationGraphConfiguration implements Serializable, Cloneable {

    protected Map<String,GraphVertex> vertices = new HashMap<>();
    protected Map<String,List<String>> vertexInputs = new HashMap<>();

    /**List of inputs to the network, by name */
    protected List<String> networkInputs;

    /**List of network outputs, by name */
    protected List<String> networkOutputs;

    protected boolean pretrain = true;
    protected boolean backprop = false;
    protected BackpropType backpropType = BackpropType.Standard;
    protected int tbpttFwdLength = 20;
    protected int tbpttBackLength = 20;
    //whether to redistribute params or not
    protected boolean redistributeParams = false;

    protected NeuralNetConfiguration defaultConfiguration;


    /**
     * @return  JSON representation of configuration
     */
    public String toYaml() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Create a neural net configuration from json
     * @param json the neural net configuration from json
     * @return {@link org.deeplearning4j.nn.conf.ComputationGraphConfiguration}
     */
    public static ComputationGraphConfiguration fromYaml(String json) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @return  JSON representation of computation graph configuration
     */
    public String toJson() {
        //As per MultiLayerConfiguration.toJson()
        ObjectMapper mapper = NeuralNetConfiguration.mapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a computation graph configuration from json
     * @param json the neural net configuration from json
     * @return {@link org.deeplearning4j.nn.conf.MultiLayerConfiguration}
     */
    public static ComputationGraphConfiguration fromJson(String json) {
        //As per MultiLayerConfiguration.fromJson()
        ObjectMapper mapper = NeuralNetConfiguration.mapper();
        try {
            return mapper.readValue(json, ComputationGraphConfiguration.class);
        } catch (IOException e) {
            //No op - try again after adding new subtypes
        }

        //Try: programmatically registering JSON subtypes for GraphVertex classes. This allows users to to add custom GraphVertex
        // implementations without needing to manually register subtypes
            //First: get all registered subtypes
        AnnotatedClass ac = AnnotatedClass.construct(GraphVertex.class,mapper.getSerializationConfig().getAnnotationIntrospector(),null);
        Collection<NamedType> types = mapper.getSubtypeResolver().collectAndResolveSubtypes(ac, mapper.getSerializationConfig(), mapper.getSerializationConfig().getAnnotationIntrospector());
        Set<Class<?>> registeredSubtypes = new HashSet<>();
        for(NamedType nt : types){
            registeredSubtypes.add(nt.getType());
        }

            //Second: get all subtypes of GraphVertex using reflection
        Reflections reflections = new Reflections();
        Set<Class<? extends GraphVertex>> subTypes = reflections.getSubTypesOf(GraphVertex.class);

            //Third: register all subtypes that are not already registered
        List<NamedType> toRegister = new ArrayList<>();
        for(Class<? extends GraphVertex> c : subTypes){
            if(!registeredSubtypes.contains(c)){
                String name;
                if(ClassUtils.isInnerClass(c)){
                    Class<?> c2 = c.getDeclaringClass();
                    name = c2.getSimpleName() + "$" + c.getSimpleName();
                } else {
                    name = c.getSimpleName();
                }
                toRegister.add(new NamedType(c, name));
            }
        }
        mapper = NeuralNetConfiguration.reinitMapperWithSubtypes(toRegister);


        try {
            return mapper.readValue(json, ComputationGraphConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public ComputationGraphConfiguration clone(){
        ComputationGraphConfiguration conf = new ComputationGraphConfiguration();

        conf.vertices = new HashMap<>();
        for(Map.Entry<String,GraphVertex> entry : this.vertices.entrySet()){
            conf.vertices.put(entry.getKey(),entry.getValue().clone());
        }

        conf.vertexInputs = new HashMap<>();
        for( Map.Entry<String,List<String>> entry : this.vertexInputs.entrySet() ){
            conf.vertexInputs.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        conf.networkInputs = new ArrayList<>(this.networkInputs);
        conf.networkOutputs = new ArrayList<>(this.networkOutputs);

        conf.pretrain = pretrain;
        conf.backprop = backprop;
        conf.backpropType = backpropType;
        conf.tbpttFwdLength = tbpttFwdLength;
        conf.tbpttBackLength = tbpttBackLength;
        conf.redistributeParams = redistributeParams;

        return conf;
    }


    /** Check the configuration, make sure it is valid
     * @throws IllegalStateException if configuration is not valid
     * */
    public void validate(){
        if(networkInputs == null || networkInputs.size() < 1){
            throw new IllegalStateException("Invalid configuration: network has no inputs");
        }
        if(networkOutputs == null || networkOutputs.size() < 1){
            throw new IllegalStateException("Invalid configuration: network has no outputs");
        }

        //Check uniqueness of names for inputs, layers, GraphNodes
        for(String s : networkInputs){
            if(vertices.containsKey(s)){
                throw new IllegalStateException("Invalid configuration: name \"" + s + "\" is present in both network inputs and graph vertices/layers");
            }
        }

        //Check: each layer & node has at least one input
        for(Map.Entry<String,List<String>> e : vertexInputs.entrySet() ){
            String nodeName = e.getKey();
            if(e.getValue() == null || e.getValue().size() == 0){
                throw new IllegalStateException("Invalid configuration: vertex \"" + nodeName + "\" has no inputs");
            }
            for(String inputName : e.getValue()) {
                if (!vertices.containsKey(inputName) && !networkInputs.contains(inputName)) {
                    throw new IllegalStateException("Invalid configuration: Vertex \"" + nodeName + "\" has input \"" +
                            inputName + "\" that does not exist");
                }
            }
        }

        //Check output names:
        for(String s : networkOutputs){
            if(!vertices.containsKey(s)){
                throw new IllegalStateException("Invalid configuration: Output name \"" + s + "\" is not a valid vertex");
            }
        }

        //Check for no graph cycles: done in ComputationGraph.init()
    }

    /** Add FeedForward/RNN preprocessors automatically. */
    public void addPreProcessors(){

        System.out.println("addPreProcessors() functionality not yet implemented");
    }


    @Data
    public static class GraphBuilder {
        protected Map<String,GraphVertex> vertices = new HashMap<>();

        /** Key: graph node. Values: input to that node */
        protected Map<String,List<String>> vertexInputs = new HashMap<>();

        protected List<String> networkInputs = new ArrayList<>();
        protected List<String> networkOutputs = new ArrayList<>();

        protected boolean pretrain = false;
        protected boolean backprop = true;
        protected BackpropType backpropType = BackpropType.Standard;
        protected int tbpttFwdLength = 20;
        protected int tbpttBackLength = 20;

        protected Map<String,InputPreProcessor> inputPreProcessors = new HashMap<>();
        //whether to redistribute params or not
        protected boolean redistributeParams = false;

        //Whether to auto-add preprocessors - FF->RNN and RNN->FF etc
        protected boolean addPreProcessors = true;

        protected NeuralNetConfiguration.Builder globalConfiguration;


        public GraphBuilder(NeuralNetConfiguration.Builder globalConfiguration){
            this.globalConfiguration = globalConfiguration;
        }

        /**
         * Whether to redistribute parameters as a view or not
         * @param redistributeParams whether to redistribute parameters as a view or not
         */
        public GraphBuilder redistributeParams(boolean redistributeParams) {
            this.redistributeParams = redistributeParams;
            return this;
        }

        /**
         * Specify the processors for a given layer
         * These are used at each layer for doing things like normalization and shaping of input.<br>
         * <b>Note</b>: preprocessors can also be defined using the {@link #addLayer(String, Layer, InputPreProcessor, String...)} method.
         * @param layer the name of the layer that this preprocessor will be used with
         * @param processor the preprocessor to use for the specified layer
         */
        public GraphBuilder inputPreProcessor(String layer, InputPreProcessor processor) {
            inputPreProcessors.put(layer,processor);
            return this;
        }

        /**
         * Whether to do back prop (standard supervised learning) or not
         * @param backprop whether to do back prop or not
         */
        public GraphBuilder backprop(boolean backprop) {
            this.backprop = backprop;
            return this;
        }

        /**Whether to do layerwise pre training or not
         * @param pretrain whether to do pre train or not
         */
        public GraphBuilder pretrain(boolean pretrain) {
            this.pretrain = pretrain;
            return this;
        }

        /**The type of backprop. Default setting is used for most networks (MLP, CNN etc),
         * but optionally truncated BPTT can be used for training recurrent neural networks.
         * If using TruncatedBPTT make sure you set both tBPTTForwardLength() and tBPTTBackwardLength()
         * @param type Type of backprop. Default: BackpropType.Standard
         */
        public GraphBuilder backpropType(BackpropType type){
            this.backpropType = type;
            return this;
        }

        /**When doing truncated BPTT: how many steps of forward pass should we do
         * before doing (truncated) backprop?<br>
         * Only applicable when doing backpropType(BackpropType.TruncatedBPTT)<br>
         * Typically tBPTTForwardLength parameter is same as the the tBPTTBackwardLength parameter,
         * but may be larger than it in some circumstances (but never smaller)<br>
         * Ideally your training data time series length should be divisible by this
         * This is the k1 parameter on pg23 of
         * http://www.cs.utoronto.ca/~ilya/pubs/ilya_sutskever_phd_thesis.pdf
         * @param forwardLength Forward length > 0, >= backwardLength
         */
        public GraphBuilder tBPTTForwardLength(int forwardLength){
            this.tbpttFwdLength = forwardLength;
            return this;
        }

        /**When doing truncated BPTT: how many steps of backward should we do?<br>
         * Only applicable when doing backpropType(BackpropType.TruncatedBPTT)<br>
         * This is the k2 parameter on pg23 of
         * http://www.cs.utoronto.ca/~ilya/pubs/ilya_sutskever_phd_thesis.pdf
         * @param backwardLength <= forwardLength
         */
        public GraphBuilder tBPTTBackwardLength(int backwardLength){
            this.tbpttBackLength = backwardLength;
            return this;
        }

        /** Add a layer, with no {@link InputPreProcessor}, with the specified name and specified inputs.
         * @param layerName Name/label of the the layer to add
         * @param layer The layer configuration
         * @param layerInputs Inputs to this layer (must be 1 or more). Inputs may be other layers, GraphVertex objects,
         *                    on a combination of the two.
         * @see #addLayer(String, Layer, InputPreProcessor, String...)
         */
        public GraphBuilder addLayer(String layerName, Layer layer, String... layerInputs ) {
            return addLayer(layerName,layer,null,layerInputs);
        }

        /** Add a layer and an {@link InputPreProcessor}, with the specified name and specified inputs.
         * @param layerName Name/label of the the layer to add
         * @param layer The layer configuration
         * @param preProcessor The InputPreProcessor to use with this layer.
         * @param layerInputs Inputs to this layer (must be 1 or more). Inputs may be other layers, GraphVertex objects,
         *                    on a combination of the two.
         */
        public GraphBuilder addLayer(String layerName, Layer layer, InputPreProcessor preProcessor, String... layerInputs ){
            NeuralNetConfiguration.Builder builder = globalConfiguration.clone();
            builder.layer(layer);
            vertices.put(layerName,new LayerVertex(builder.build(),preProcessor));

            //Automatically insert a MergeNode if layerInputs.length > 1
            //Layers can only have 1 input
            if(layerInputs != null && layerInputs.length > 1 ){
                String mergeName = layerName+"-merge";
                addVertex(mergeName, new MergeVertex(), layerInputs);
                this.vertexInputs.put(layerName,Collections.singletonList(mergeName));
            } else if(layerInputs != null) {
                this.vertexInputs.put(layerName,Arrays.asList(layerInputs));
                layer.setLayerName(layerName);
            }
            return this;
        }

        /** Specify the inputs to the network, and their associated labels.
         * @param inputNames The names of the inputs. This also defines their order
         */
        public GraphBuilder addInputs( String... inputNames ){
            Collections.addAll(networkInputs,inputNames);
            return this;
        }

        /** Set the network output labels. These should be the names of the OutputLayer instances in the network
         * @param outputNames The names of the output layers. This also defines their order.
         */
        public GraphBuilder setOutputs( String... outputNames ){
            Collections.addAll(networkOutputs,outputNames);
            return this;
        }

        /** Add a {@link GraphVertex} to the network configuration. A GraphVertex defines forward and backward pass methods,
         * and can contain a {@link LayerVertex}, a {@link org.deeplearning4j.nn.conf.graph.ElementWiseVertex} to do element-wise
         * addition/subtraction, a {@link MergeVertex} to combine/concatenate the activations out of multiple layers or vertices,
         * a {@link org.deeplearning4j.nn.conf.graph.SubsetVertex} to select a subset of the activations out of another layer/GraphVertex.<br>
         * Custom GraphVertex objects (that extend the abstract {@link GraphVertex} class) may also be used.
         * @param vertexName The name of the GraphVertex to add
         * @param vertex The GraphVertex to add
         * @param vertexInputs The inputs/activations to this GraphVertex
         */
        public GraphBuilder addVertex(String vertexName, GraphVertex vertex, String... vertexInputs ){
            vertices.put(vertexName, vertex);
            this.vertexInputs.put(vertexName, Arrays.asList(vertexInputs));
            return this;
        }

        /** Whether to automatically add preprocessors, between feed forward and RNN layers.<br>
         * This is enabled by default. The assumption here is that with RNN layers, the data input is a time series,
         * and thus RNN->FF preprocesors may need to be added on the input (if input feeds into for example a DenseLayer).
         * Any FF->RNN transitions will automatically add a {@link org.deeplearning4j.nn.conf.preprocessor.FeedForwardToRnnPreProcessor}<br>
         * <b>NOTE:</b> the assumptions made above may not for every network.
         * @param addPreProcessors If true: add RNN/FF preprocessors if applicable. False: add no preprocessors.
         * @return
         */
        public GraphBuilder addPreProcessors(boolean addPreProcessors){
            this.addPreProcessors = addPreProcessors;
            return this;
        }

        /** Create the ComputationGraphConfiguration from the Builder pattern */
        public ComputationGraphConfiguration build(){

            ComputationGraphConfiguration conf = new ComputationGraphConfiguration();
            conf.backprop = backprop;
            conf.pretrain = pretrain;
            conf.backpropType = backpropType;
            conf.tbpttBackLength = tbpttBackLength;
            conf.tbpttFwdLength = tbpttFwdLength;

            conf.networkInputs = networkInputs;
            conf.networkOutputs = networkOutputs;

            conf.vertices = this.vertices;
            conf.vertexInputs = this.vertexInputs;

            conf.defaultConfiguration = globalConfiguration.build();

            //Add preprocessors that were defined separately to the Layers to which they belong
            for(Map.Entry<String,InputPreProcessor> entry : inputPreProcessors.entrySet()){
                GraphVertex gv = vertices.get(entry.getKey());
                if(gv instanceof LayerVertex){
                    LayerVertex lv = (LayerVertex)gv;
                    lv.setPreProcessor(entry.getValue());
                } else {
                    throw new IllegalStateException("Invalid configuration: InputPreProcessor defined for GraphVertex \"" + entry.getKey()
                        + "\", but this vertex is not a LayerVertex");
                }
            }

            conf.validate();    //throws exception for invalid configuration

            if(addPreProcessors) conf.addPreProcessors();

            return conf;
        }
    }

}