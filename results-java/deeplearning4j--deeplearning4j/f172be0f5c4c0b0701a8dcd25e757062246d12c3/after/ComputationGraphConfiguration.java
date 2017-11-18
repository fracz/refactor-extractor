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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.graph.nodes.GraphNode;

import java.io.Serializable;
import java.util.*;

/** ComputationGraphConfiguration is a configuration object for neural networks with arbitrary connection structure.
 * It is analogous to {@link MultiLayerConfiguration}, but allows considerably greater flexibility for the network
 * architecture
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ComputationGraphConfiguration implements Serializable, Cloneable {


    /** Map between layer numbers, and layer names */
    protected Map<Integer,String> layerNamesMap;
    protected Map<String,Integer> layerNumbersMap;

    protected Map<String,NeuralNetConfiguration> layers = new HashMap<>();
    protected Map<String,GraphNode> graphNodes = new HashMap<>();

    protected Map<String,String[]> layerInputs = new HashMap<>();
    protected Map<String,String[]> graphNodeInputs = new HashMap<>();

    protected List<String> networkInputs;
    protected List<String> networkOutputs;


    protected boolean pretrain = true;
    protected boolean backprop = false;
    protected Map<Integer,InputPreProcessor> inputPreProcessors = new HashMap<>();
    protected BackpropType backpropType = BackpropType.Standard;
    protected int tbpttFwdLength = 20;
    protected int tbpttBackLength = 20;
    //whether to redistribute params or not
    protected boolean redistributeParams = false;


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
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Create a computation graph configuration from json
     * @param json the neural net configuration from json
     * @return {@link org.deeplearning4j.nn.conf.MultiLayerConfiguration}
     */
    public static MultiLayerConfiguration fromJson(String json) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public ComputationGraphConfiguration clone(){
        throw new UnsupportedOperationException("Not implemnted");
    }


    /** Check the configuration, make sure it is valid */
    public boolean validate(){

        //Check uniqueness of names for inputs, layers, GraphNodes

        //Check that all input keys/names for each layer actually exist

        //Check: at least one input, at least one output

        //Check: each layer has at least one input

        //Check: no graph cycles

        //Check preprocessors

        System.out.println("WARNING: ComputationGraphConfiguration.validate() NOT YET IMPLEMENTED");
        return true;
    }

    @Data
    public static class GraphBuilder {
        /** Map between layer numbers, and layer names */
        protected Map<Integer,String> layerNamesMap;
        protected Map<String,Integer> layerNumbersMap;

        protected Map<String,NeuralNetConfiguration.Builder> layers = new HashMap<>();
        protected Map<String,GraphNode> graphNodes = new HashMap<>();

        /** Key: layer. Values: inputs to that layer */
        protected Map<String,String[]> layerInputs = new HashMap<>();

        /** Key: graph node. Values: input to that node */
        protected Map<String,String[]> graphNodeInputs = new HashMap<>();

        protected List<String> networkInputs = new ArrayList<>();
        protected List<String> networkOutputs = new ArrayList<>();

        protected boolean pretrain = true;
        protected boolean backprop = false;
        protected BackpropType backpropType = BackpropType.Standard;
        protected int tbpttFwdLength = 20;
        protected int tbpttBackLength = 20;

        protected Map<String,InputPreProcessor> inputPreProcessors = new HashMap<>();
        //whether to redistribute params or not
        protected boolean redistributeParams = false;

        protected NeuralNetConfiguration.Builder globalConfiguration;

        public GraphBuilder(NeuralNetConfiguration.Builder globalConfiguration){
            this.globalConfiguration = globalConfiguration;
        }

        /**
         * Whether to redistribute parameters as a view or not
         * @param redistributeParams whether to redistribute parameters
         *                           as a view or not
         * @return
         */
        public GraphBuilder redistributeParams(boolean redistributeParams) {
            this.redistributeParams = redistributeParams;
            return this;
        }

        /**
         * Specify the processors.
         * These are used at each layer for doing things like normalization and
         * shaping of input.
         * @param processor what to use to preProcess the data.
         */
        public GraphBuilder inputPreProcessor(String layer, InputPreProcessor processor) {
            inputPreProcessors.put(layer,processor);
            return this;
        }

        /**
         * Whether to do back prop or not
         * @param backprop whether to do back prop or not
         * @return
         */
        public GraphBuilder backprop(boolean backprop) {
            this.backprop = backprop;
            return this;
        }

        /**Whether to do pre train or not
         * @param pretrain whether to do pre train or not
         * @return builder pattern
         */
        public GraphBuilder pretrain(boolean pretrain) {
            this.pretrain = pretrain;
            return this;
        }

        /**The type of backprop. Default setting is used for most networks (MLP, CNN etc),
         * but optionally truncated BPTT can be used for training recurrent neural networks.
         * If using TruncatedBPTT make sure you set both tBPTTForwardLength() and tBPTTBackwardLength()
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

        //TODO: code duplication? Layer has a name field...
        public GraphBuilder addLayer(String layerName, Layer layer, String... layerInputs ){
            NeuralNetConfiguration.Builder builder = globalConfiguration.clone();
            builder.layer(layer);
            layers.put(layerName,builder);
            this.layerInputs.put(layerName,layerInputs);
            return this;
        }

        public GraphBuilder addInputs( String... inputNames ){
            Collections.addAll(networkInputs,inputNames);
            return this;
        }

        public GraphBuilder setOutputs( String... outputNames ){
            Collections.addAll(networkOutputs,outputNames);
            return this;
        }

        public GraphBuilder addNode(String nodeName, GraphNode node, String... nodeInputs ){
            graphNodes.put(nodeName,node);
            this.graphNodeInputs.put(nodeName, nodeInputs);
            return this;
        }


        public ComputationGraphConfiguration build(){

            ComputationGraphConfiguration conf = new ComputationGraphConfiguration();
            conf.backprop = backprop;
            conf.pretrain = pretrain;
            conf.backpropType = backpropType;
            conf.tbpttBackLength = tbpttBackLength;
            conf.tbpttFwdLength = tbpttFwdLength;


            conf.layerNamesMap = layerNamesMap;
            conf.layerNumbersMap = layerNumbersMap;
            conf.layerInputs = layerInputs;

            conf.networkInputs = networkInputs;
            conf.networkOutputs = networkOutputs;

            Map<String,NeuralNetConfiguration> layers = new HashMap<>();
            for( Map.Entry<String,NeuralNetConfiguration.Builder> entry : this.layers.entrySet() ){
                layers.put(entry.getKey(), entry.getValue().build());
            }

            conf.layers = layers;

            conf.graphNodes = this.graphNodes;
            conf.graphNodeInputs = this.graphNodeInputs;



            if(!conf.validate()){
                //TODO: provide details
                throw new IllegalStateException("Invalid configuration");
            }
            return conf;
        }
    }

}