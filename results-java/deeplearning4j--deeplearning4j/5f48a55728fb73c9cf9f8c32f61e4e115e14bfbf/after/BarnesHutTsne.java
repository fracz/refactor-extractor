package org.deeplearning4j.plot;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.math3.random.MersenneTwister;
import org.deeplearning4j.berkeley.Counter;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.clustering.quadtree.QuadTree;
import org.deeplearning4j.clustering.vptree.VpTreeNode;
import org.deeplearning4j.clustering.vptree.VpTreePointINDArray;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.gradient.DefaultGradient;
import org.deeplearning4j.nn.gradient.Gradient;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Conditions;
import org.nd4j.linalg.indexing.functions.Value;


import java.util.List;

import static org.nd4j.linalg.factory.Nd4j.ones;
import static org.nd4j.linalg.factory.Nd4j.randn;
import static org.nd4j.linalg.factory.Nd4j.zeros;
import static org.nd4j.linalg.ops.transforms.Transforms.*;


/**
 * Barnes hut algorithm for TSNE, uses a dual tree approximation approach.
 * Work based on:
 * http://lvdmaaten.github.io/tsne/
 * @author Adam Gibson
 */
public class BarnesHutTsne extends Tsne implements Model {
    private int n;
    private int d;
    private double perplexity;
    private double theta;
    private INDArray rows;
    private INDArray cols;
    private INDArray vals;
    private INDArray p;
    private INDArray x;
    private int numDimensions = 0;
    public final static String Y_GRAD = "yIncs";

    public BarnesHutTsne(INDArray x,
                         int n,
                         int d,
                         INDArray y,
                         int numDimensions,
                         double perplexity,
                         double theta,
                         int maxIter,
                         int stopLyingIteration,
                         int momentumSwitchIteration,
                         double momentum,
                         double finalMomentum,
                         double learningRate) {
        super();
        this.n = n;
        this.d = d;
        this.y = y;
        this. x = x;
        this.numDimensions = numDimensions;
        this.perplexity = perplexity;
        this.theta = theta;
        this.maxIter = maxIter;
        this.stopLyingIteration = stopLyingIteration;
        this.momentum = momentum;
        this.finalMomentum = finalMomentum;
        this.learningRate = learningRate;
        this.switchMomentumIteration = momentumSwitchIteration;
    }



    /**
     * Convert data to probability
     * co-occurrences (aka calculating the kernel)
     * @param d the data to convert
     * @param u the perplexity of the model
     * @return the probabilities of co-occurrence
     */
    @Override
    public INDArray computeGaussianPerplexity(final INDArray d,  double u) {
        int N = d.rows();
        final int k = (int) (3 * u);

        rows = zeros(N + 1);
        cols = zeros(N,k);
        vals = zeros(N,k);

        for(int n = 1; n < N; n++)
            rows.putScalar(n,rows.getDouble(n - 1) + k);


        final INDArray beta =  ones(N, 1);

        final double logU =  Math.log(u);

        final List<VpTreePointINDArray> list = VpTreePointINDArray.dataPoints(d);
        final VpTreeNode<VpTreePointINDArray> tree = VpTreeNode.buildVpTree(list);

        log.info("Calculating probabilities of data similarities...");
        for(int i = 0; i < N; i++) {
            if(i % 500 == 0)
                log.info("Handled " + i + " records");
            final int j = i;

            double betaMin = Double.NEGATIVE_INFINITY;
            double betaMax = Double.POSITIVE_INFINITY;
            Counter<VpTreePointINDArray> c = tree.findNearByPointsWithDistancesK(list.get(j),k + 1);

            INDArray cArr = toNDArray(c);
            Pair<INDArray,Double> pair =  computeGaussianKernel(cArr, beta.getDouble(j));
            INDArray currP = pair.getFirst();
            double hDiff =  pair.getSecond() - logU;
            int tries = 0;
            boolean found = false;

            while(!found && tries < 50) {
                if(hDiff < tolerance && -hDiff < tolerance)
                    found = true;
                else {
                    if(hDiff > 0) {
                        if(Double.isInfinite(betaMax))
                            beta.putScalar(j,beta.getDouble(j) * 2.0);
                        else
                            beta.putScalar(j,(beta.getDouble(j) + betaMax) / 2.0);
                        betaMin = beta.getDouble(j);
                    }
                    else {
                        if(Double.isInfinite(betaMin))
                            beta.putScalar(j,beta.getDouble(j) / 2.0);
                        else
                            beta.putScalar(j,(beta.getDouble(j) + betaMin) / 2.0);
                        betaMax = beta.getDouble(j);
                    }

                    pair = computeGaussianKernel(toNDArray(c), beta.getDouble(j));
                    hDiff = pair.getSecond() - logU;
                    tries++;
                }

            }

            INDArray currPAssign = currP.div(currP.sum(Integer.MAX_VALUE));
            INDArray indices = toIndex(c);


            for(int l = 0; l < indices.length(); l++) {
                cols.putScalar(new int[]{rows.getInt(n),l},indices.getDouble(l));
                vals.putScalar(new int[]{rows.getInt(n),l},currPAssign.getDouble(l));
            }

            cols.slice(j).put(new NDArrayIndex[]{NDArrayIndex.interval(0,indices.length())},indices);


        }
        return vals;

    }

    @Override
    public INDArray input() {
        return x;
    }

    @Override
    public void validateInput() {

    }

    /* compute the gradient given the current solution, the probabilities and the constant */
    @Override
    protected Pair<Double,INDArray> gradient(INDArray p) {
        this.p = p;
        return new Pair<>(score(),y);
    }


    public INDArray getYGradient(int n,INDArray PQ,INDArray qu) {
        INDArray yGrads = Nd4j.create(y.shape());
        for(int i = 0; i < n; i++) {
            INDArray sum1 = Nd4j.tile(PQ.getRow(i).mul(qu.getRow(i)), new int[]{y.columns(), 1})
                    .transpose().mul(y.getRow(i).broadcast(y.shape()).sub(y)).sum(0);
            yGrads.putRow(i, sum1);
        }

        return yGrads;
    }

    private INDArray toIndex(Counter<VpTreePointINDArray> counter) {
        INDArray ret = Nd4j.create(counter.size());
        List<VpTreePointINDArray> list = counter.getSortedKeys();
        for(int i = 0; i < list.size(); i++) {
            ret.putScalar(i,list.get(i).getIndex());
        }
        return ret;
    }

    private INDArray toNDArray(Counter<VpTreePointINDArray> counter) {
        INDArray ret = Nd4j.create(counter.size());
        List<VpTreePointINDArray> list = counter.getSortedKeys();
        for(int i = 0; i < list.size(); i++) {
            ret.putScalar(i,counter.getCount(list.get(i)));
        }
        return ret;
    }


    /**
     * Computes a gaussian kernel
     * given a vector of squared euclidean distances
     *
     * @param beta
     * @return
     */
    public Pair<INDArray,Double> computeGaussianKernel(INDArray distances, double beta) {
        INDArray distancesTimesBeta = exp(distances.mul(-beta));
        double sum = distancesTimesBeta.sum(Integer.MAX_VALUE).getDouble(0);
        double h = distancesTimesBeta.mul(distances).muli(beta).sum(Integer.MAX_VALUE).getDouble(0) / sum + Math.log(sum);
        return new Pair<>(distancesTimesBeta,h);
    }



    @Override
    public void fit() {
        boolean exact = theta == 0.0;
        if(exact)
            y = super.calculate(x,numDimensions,perplexity);


        else {
            //output
            if(y == null)
                y = randn(x.rows(),numDimensions,new MersenneTwister(123)).muli(1e-3f);


            INDArray p = computeGaussianPerplexity(x,perplexity);

            for(int i = 0; i < maxIter; i++) {
                step(p,i);

                if(i == switchMomentumIteration)
                    momentum = finalMomentum;
                if(i == stopLyingIteration)
                    p.divi(4);

                if(iterationListener != null)
                    iterationListener.iterationDone(i);


            }

        }
    }






    @Override
    public void update(Gradient gradient) {

    }

    @Override
    public double score() {
        // Get estimate of normalization term
        int QT_NO_DIMS = 2;
        QuadTree tree = new QuadTree(y);
        INDArray buff = Nd4j.create(QT_NO_DIMS);
        AtomicDouble sum_Q = new AtomicDouble(0.0);
        for(int n = 0; n < y.rows(); n++)
            tree.computeNonEdgeForces(n, theta, buff, sum_Q);

        // Loop over all edges to compute t-SNE error
        double C = .0, Q;
        for(int n = 0; n < y.rows(); n++) {
            int begin = rows.getInt(n);
            int end = rows.getInt(n + 1);
            for(int i = begin; i < end; i++) {
                buff.assign(y.slice(n));
                buff.subi(y.slice(cols.getInt(i)));
                Q = Nd4j.getBlasWrapper().dot(buff,buff);
                Q = (1.0 / (1.0 + Q)) / sum_Q.doubleValue();
                double val = vals.getDouble(i,0);
                C += val * Math.log((val + Double.MIN_VALUE) / (Q + Double.MAX_VALUE));
            }
        }

        return C;
    }

    @Override
    public INDArray transform(INDArray data) {
        return null;
    }

    @Override
    public INDArray params() {
        return null;
    }

    @Override
    public int numParams() {
        return 0;
    }

    @Override
    public void setParams(INDArray params) {

    }

    @Override
    public void fit(INDArray data) {
        this.x  = data;
        fit();
    }

    @Override
    public void iterate(INDArray input) {

    }

    @Override
    public Gradient getGradient() {
        if(yIncs == null)
            yIncs =  zeros(y.shape());
        if(gains == null)
            gains = ones(y.shape());

        AtomicDouble sum_Q = new AtomicDouble(0);
        INDArray pos_f = Nd4j.create(p.rows(), p.columns());
        INDArray neg_f = Nd4j.create(p.rows() ,p.columns());

        QuadTree quad = new QuadTree(y);
        quad.computeEdgeForces(rows,cols,p,p.rows(),pos_f);

        for(int n = 0; n < p.rows(); n++) {
            quad.computeNonEdgeForces(n,theta,neg_f,sum_Q);
        }

        INDArray dC = pos_f.subi(neg_f.divi(sum_Q));

        INDArray yGrads = dC;

        gains = gains.add(.2)
                .muli(yGrads.cond(Conditions.greaterThan(0)).neqi(yIncs.cond(Conditions.greaterThan(0))))
                .addi(gains.mul(0.8).muli(yGrads.cond(Conditions.greaterThan(0)).eqi(yIncs.cond(Conditions.greaterThan(0)))));

        BooleanIndexing.applyWhere(
                gains,
                Conditions.lessThan(minGain),
                new Value(minGain));


        INDArray gradChange = gains.mul(yGrads);

        if(useAdaGrad)
            gradChange = adaGrad.getGradient(gradChange);
        else
            gradChange.muli(learningRate);


        yIncs.muli(momentum).subi(gradChange);


        Gradient ret = new DefaultGradient();
        ret.gradientLookupTable().put("yIncs",yIncs);
        return ret;
    }

    @Override
    public Pair<Gradient, Double> gradientAndScore() {
        return new Pair<>(getGradient(),score());
    }

    @Override
    public int batchSize() {
        return 0;
    }

    @Override
    public NeuralNetConfiguration conf() {
        return null;
    }

    @Override
    public void setConf(NeuralNetConfiguration conf) {

    }


    public static class Builder extends  Tsne.Builder {
        private double theta = 0.0;

        public Builder theta(double theta) {
            this.theta = theta;
            return this;
        }

        @Override
        public Builder minGain(double minGain) {
            super.minGain(minGain);
            return this;
        }

        @Override
        public Builder perplexity(double perplexity) {
            super.perplexity(perplexity);
            return this;
        }

        @Override
        public Builder useAdaGrad(boolean useAdaGrad) {
            super.useAdaGrad(useAdaGrad);
            return this;
        }

        @Override
        public Builder learningRate(double learningRate) {
            super.learningRate(learningRate);
            return this;
        }

        @Override
        public Builder tolerance(double tolerance) {
            super.tolerance(tolerance);
            return this;
        }

        @Override
        public Builder stopLyingIteration(int stopLyingIteration) {
            super.stopLyingIteration(stopLyingIteration);
            return this;
        }

        @Override
        public Builder usePca(boolean usePca) {
            super.usePca(usePca);
            return this;
        }

        @Override
        public Builder normalize(boolean normalize) {
            super.normalize(normalize);
            return this;
        }

        @Override
        public Builder setMaxIter(int maxIter) {
            super.setMaxIter(maxIter);
            return this;
        }

        @Override
        public Builder setRealMin(double realMin) {
            super.setRealMin(realMin);
            return this;
        }

        @Override
        public Builder setInitialMomentum(double initialMomentum) {
            super.setInitialMomentum(initialMomentum);
            return this;
        }

        @Override
        public Builder setFinalMomentum(double finalMomentum) {
            super.setFinalMomentum(finalMomentum);
            return this;
        }

        @Override
        public Builder setMomentum(double momentum) {
            super.setMomentum(momentum);
            return this;
        }

        @Override
        public Builder setSwitchMomentumIteration(int switchMomentumIteration) {
            super.setSwitchMomentumIteration(switchMomentumIteration);
            return this;
        }

        @Override
        public BarnesHutTsne build() {
            return new BarnesHutTsne(null,0,0,null,2,perplexity,theta,maxIter,this.stopLyingIteration,this.switchMomentumIteration,this.momentum,this.finalMomentum,this.learningRate);
        }
    }
}