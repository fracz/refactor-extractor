package org.deeplearning4j.word2vec.iterator;

import org.deeplearning4j.datasets.DataSet;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.util.MatrixUtil;
import org.deeplearning4j.word2vec.Word2Vec;
import org.deeplearning4j.word2vec.inputsanitation.InputHomogenization;
import org.deeplearning4j.word2vec.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.word2vec.sentenceiterator.labelaware.LabelAwareSentenceIterator;
import org.deeplearning4j.word2vec.util.Window;
import org.deeplearning4j.word2vec.util.WindowConverter;
import org.deeplearning4j.word2vec.util.Windows;
import org.jblas.DoubleMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Iterates over a sentence with moving window to produce a data applyTransformToDestination
 * for word windows based on a pretrained word2vec.
 *
 * @author Adam Gibson
 */
public class Word2VecDataSetIterator implements DataSetIterator {
    private Word2Vec vec;
    private LabelAwareSentenceIterator iter;
    private List<Window> cachedWindow;
    private List<String> labels;
    private boolean homogenization = true;
    private boolean addLabels = true;
    private int batch = 10;
    private DataSet curr;

    /**
     * Allows for customization of all of the params of the iterator
     * @param vec the word2vec model to use
     * @param iter the sentence iterator to use
     * @param labels the possible labels
     * @param batch the batch size
     * @param homogenization whether to homogenize the sentences or not
     * @param addLabels whether to add labels for windows
     */
    public Word2VecDataSetIterator(Word2Vec vec,LabelAwareSentenceIterator iter,List<String> labels,int batch,boolean homogenization,boolean addLabels) {
        this.vec = vec;
        this.iter = iter;
        this.labels = labels;
        this.batch = batch;
        cachedWindow = new CopyOnWriteArrayList<>();
        this.addLabels = addLabels;
        this.homogenization = homogenization;

        if(addLabels && homogenization)
            iter.setPreProcessor(new SentencePreProcessor() {
                @Override
                public String preProcess(String sentence) {
                    String label = Word2VecDataSetIterator.this.iter.currentLabel();
                    String ret = "<" + label + "> " + new InputHomogenization(sentence).transform() + " </" + label + ">";
                    return ret;
                }
            });

        else if(addLabels)
            iter.setPreProcessor(new SentencePreProcessor() {
                @Override
                public String preProcess(String sentence) {
                    String label = Word2VecDataSetIterator.this.iter.currentLabel();
                    String ret = "<" + label + ">" + sentence + "</" + label + ">";
                    return ret;
                }
            });

        else if(homogenization)
            iter.setPreProcessor(new SentencePreProcessor() {
                @Override
                public String preProcess(String sentence) {
                    String ret = new InputHomogenization(sentence).transform();
                    return ret;
                }
            });

    }

    /**
     * Initializes this iterator with homogenization and adding labels
     * and a batch size of 10
     * @param vec the vector model to use
     * @param iter the sentence iterator to use
     * @param labels the possible labels
     */
    public Word2VecDataSetIterator(Word2Vec vec,LabelAwareSentenceIterator iter,List<String> labels ) {
        this(vec,iter,labels,10);
    }
    /**
     * Initializes this iterator with homogenization and adding labels
     * @param vec the vector model to use
     * @param iter the sentence iterator to use
     * @param labels the possible labels
     * @param batch the batch size
     */
    public Word2VecDataSetIterator(Word2Vec vec,LabelAwareSentenceIterator iter,List<String> labels,int batch) {
        this(vec,iter,labels,batch,true,true);


    }
    /**
     * Like the standard next method but allows a
     * customizable number of examples returned
     *
     * @param num the number of examples
     * @return the next data applyTransformToDestination
     */
    @Override
    public DataSet next(int num) {
        if(num <= cachedWindow.size())
            return fromCached(num);
            //no more sentences, return the left over
        else if(num >= cachedWindow.size() && !iter.hasNext())
            return fromCached(cachedWindow.size());

            //need the next sentence
        else {
            while(cachedWindow.size() < num && iter.hasNext()) {
                String sentence = iter.nextSentence();
                if(sentence.isEmpty())
                    continue;
                List<Window> windows = Windows.windows(sentence,vec.getTokenizerFactory(),vec.getWindow());
                if(windows.isEmpty() && !sentence.isEmpty())
                    throw new IllegalStateException("Empty window on sentence");
                for(Window w : windows)
                    w.setLabel(iter.currentLabel());
                cachedWindow.addAll(windows);
            }

            return fromCached(num);
        }

    }

    private DataSet fromCached(int num) {
        if(cachedWindow.isEmpty()) {
            while(cachedWindow.size() < num && iter.hasNext()) {
                String sentence = iter.nextSentence();
                if(sentence.isEmpty())
                    continue;
                List<Window> windows = Windows.windows(sentence,vec.getTokenizerFactory(),vec.getWindow());
                for(Window w : windows)
                    w.setLabel(iter.currentLabel());
                cachedWindow.addAll(windows);
            }
        }


        List<Window> windows = new ArrayList<>(num);

        for(int i = 0; i < num; i++) {
            if(cachedWindow.isEmpty())
                break;
            windows.add(cachedWindow.remove(0));
        }

        if(windows.isEmpty())
            return null;

        DoubleMatrix inputs = new DoubleMatrix(num,inputColumns());
        for(int i = 0; i < inputs.rows; i++) {
            inputs.putRow(i, WindowConverter.asExampleMatrix(windows.get(i),vec));
        }

        DoubleMatrix labelOutput = new DoubleMatrix(num,labels.size());
        for(int i = 0; i < labelOutput.rows; i++) {
            String label = windows.get(i).getLabel();
            labelOutput.putRow(i, MatrixUtil.toOutcomeVector(labels.indexOf(label),labels.size()));
        }

        return new DataSet(inputs,labelOutput);

    }


    @Override
    public int totalExamples() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int inputColumns() {
        return vec.getLayerSize() * vec.getWindow();
    }

    @Override
    public int totalOutcomes() {
        return labels.size();
    }

    @Override
    public void reset() {
        iter.reset();
        cachedWindow.clear();
    }

    @Override
    public int batch() {
        return batch;
    }

    @Override
    public int cursor() {
        return 0;
    }

    @Override
    public int numExamples() {
        return 0;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return iter.hasNext() || !cachedWindow.isEmpty();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     */
    @Override
    public DataSet next() {
        return next(batch);
    }

    /**
     * Removes from the underlying collection the last element returned
     * by this iterator (optional operation).  This method can be called
     * only once per call to {@link #next}.  The behavior of an iterator
     * is unspecified if the underlying collection is modified while the
     * iteration is in progress in any way other than by calling this
     * method.
     *
     * @throws UnsupportedOperationException if the {@code remove}
     *                                       operation is not supported by this iterator
     * @throws IllegalStateException         if the {@code next} method has not
     *                                       yet been called, or the {@code remove} method has already
     *                                       been called after the last call to the {@code next}
     *                                       method
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}