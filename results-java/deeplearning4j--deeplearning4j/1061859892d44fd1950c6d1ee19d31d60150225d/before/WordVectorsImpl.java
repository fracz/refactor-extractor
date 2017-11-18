package org.deeplearning4j.models.embeddings.wordvectors;

import org.deeplearning4j.berkeley.Counter;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.text.stopwords.StopWords;
import org.deeplearning4j.util.MathUtils;
import org.deeplearning4j.util.SetUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.*;

/**
 * Common word vector operations
 * @author Adam Gibson
 */
public class WordVectorsImpl implements WordVectors {


    protected WeightLookupTable lookupTable;
    protected VocabCache vocab;
    public final static String UNK = "UNK";
    protected int layerSize = 100;
    protected List<String> stopWords = StopWords.getStopWords();
    /**
     * Returns true if the model has this word in the vocab
     * @param word the word to test for
     * @return true if the model has the word in the vocab
     */
    public boolean hasWord(String word) {
        return vocab().indexOf(word) >= 0;
    }
    /**
     * Words nearest based on positive and negative words
     * @param positive the positive words
     * @param negative the negative words
     * @param top the top n words
     * @return the words nearest the mean of the words
     */
    public Collection<String> wordsNearestSum(List<String> positive,List<String> negative,int top) {
        INDArray words = Nd4j.create(layerSize);
        Set<String> union = SetUtils.union(new HashSet<>(positive), new HashSet<>(negative));
        for(String s : positive)
            words.addi(lookupTable.vector(s));


        for(String s : negative)
            words.addi(lookupTable.vector(s).mul(-1));


        if(lookupTable instanceof InMemoryLookupTable) {
            InMemoryLookupTable l = (InMemoryLookupTable) lookupTable;
            INDArray syn0 = l.getSyn0();
            INDArray weights = syn0.norm2(0).rdivi(1).muli(words);
            INDArray distances = syn0.mulRowVector(weights).sum(1);
            INDArray[] sorted = Nd4j.sortWithIndices(distances,0,false);
            INDArray sort = sorted[0];
            List<String> ret = new ArrayList<>();
            if(top > sort.length())
                top = sort.length();
            //there will be a redundant word
            int end = top + 1;
            for(int i = 0; i < end; i++) {
                String word = vocab.wordAtIndex(sort.getInt(i));
                if(union.contains(word)) {
                    end++;
                    if(end >= sort.length())
                        break;
                    continue;
                }
                ret.add(vocab.wordAtIndex(sort.getInt(i)));
            }


            return ret;
        }

        Counter<String> distances = new Counter<>();

        for(String s : vocab.words()) {
            INDArray otherVec = getWordVectorMatrix(s);
            double sim = Transforms.cosineSim(words, otherVec);
            distances.incrementCount(s, sim);
        }


        distances.keepTopNKeys(top);
        return distances.keySet();


    }


    /**
     * Get the top n words most similar to the given word
     * @param word the word to compare
     * @param n the n to get
     * @return the top n words
     */
    public Collection<String> wordsNearestSum(String word,int n) {
        INDArray vec = Transforms.unitVec(this.getWordVectorMatrix(word));


        if(lookupTable instanceof InMemoryLookupTable) {
            InMemoryLookupTable l = (InMemoryLookupTable) lookupTable();
            INDArray syn0 = l.getSyn0();
            INDArray weights = syn0.norm2(0).rdivi(1).muli(vec);
            INDArray distances = syn0.mulRowVector(weights).sum(1);
            INDArray[] sorted = Nd4j.sortWithIndices(distances,0,false);
            INDArray sort = sorted[0];
            List<String> ret = new ArrayList<>();
            VocabWord word2 = vocab().wordFor(word);
            if(n > sort.length())
                n = sort.length();
            //there will be a redundant word
            for(int i = 0; i < n + 1; i++) {
                if(sort.getInt(i) == word2.getIndex())
                    continue;
                ret.add(vocab().wordAtIndex(sort.getInt(i)));
            }


            return ret;
        }

        if(vec == null)
            return new ArrayList<>();
        Counter<String> distances = new Counter<>();

        for(String s : vocab().words()) {
            if(s.equals(word))
                continue;
            INDArray otherVec = getWordVectorMatrix(s);
            double sim = Transforms.cosineSim(vec,otherVec);
            distances.incrementCount(s, sim);
        }


        distances.keepTopNKeys(n);
        return distances.keySet();

    }


    /** Accuracy based on questions which are a space separated list of strings
    * where the first word is the query word, the next 2 words are negative,
            * and the last word is the predicted word to be nearest
    * @param questions the questions to ask
    * @return the accuracy based on these questions
    */
    public Map<String,Double> accuracy(List<String> questions) {
        Map<String,Double> accuracy = new HashMap<>();
        Counter<String> right = new Counter<>();
        for(String s : questions) {
            if(s.startsWith(":")) {
                double correct = right.getCount("correct");
                double wrong = right.getCount("wrong");
                double accuracyRet = 100.0 * correct / (correct / wrong);
                accuracy.put(s,accuracyRet);
                right.clear();
            }
            else {
                String[] split = s.split(" ");
                String word = split[0];
                List<String> positive = Arrays.asList(word);
                List<String> negative = Arrays.asList(split[1],split[2]);
                String predicted = split[3];
                String w = wordsNearest(positive,negative,1).iterator().next();
                if(predicted.equals(w))
                    right.incrementCount("right",1.0);
                else
                    right.incrementCount("wrong",1.0);

            }
        }

        return accuracy;
    }

    @Override
    public int indexOf(String word) {
        return vocab().indexOf(word);
    }


    /**
     * Find all words with a similar characters
     * in the vocab
     * @param word the word to compare
     * @param accuracy the accuracy: 0 to 1
     * @return the list of words that are similar in the vocab
     */
    public List<String> similarWordsInVocabTo(String word,double accuracy) {
        List<String> ret = new ArrayList<>();
        for(String s : vocab.words()) {
            if(MathUtils.stringSimilarity(word, s) >= accuracy)
                ret.add(s);
        }
        return ret;
    }

    /**
     * Get the word vector for a given matrix
     * @param word the word to get the matrix for
     * @return the ndarray for this word
     */
    public double[] getWordVector(String word) {
        int i = vocab().indexOf(word);
        if(i < 0)
            return lookupTable.vector(UNK).ravel().data().asDouble();
        return lookupTable.vector(word).ravel().data().asDouble();
    }

    /**
     * Returns the word vector divided by the norm2 of the array
     * @param word the word to get the matrix for
     * @return the looked up matrix
     */
    public INDArray getWordVectorMatrixNormalized(String word) {
        int i = vocab().indexOf(word);

        if(i < 0)
            return lookupTable.vector(UNK);
        INDArray r =  lookupTable.vector(word);
        return r.div(Nd4j.getBlasWrapper().nrm2(r));
    }

    @Override
    public INDArray getWordVectorMatrix(String word) {
        return null;
    }


    /**
     * Words nearest based on positive and negative words
     * @param positive the positive words
     * @param negative the negative words
     * @param top the top n words
     * @return the words nearest the mean of the words
     */
    public Collection<String> wordsNearest(List<String> positive,List<String> negative,int top) {
        for(String p : SetUtils.union(new HashSet<>(positive),new HashSet<>(negative))) {
            if(!vocab.containsWord(p)) {
                return new ArrayList<>();
            }
        }


        INDArray words = Nd4j.create(positive.size() + negative.size(), layerSize);
        int row = 0;
        Set<String> union = SetUtils.union(new HashSet<>(positive),new HashSet<>(negative));
        for(String s : positive) {
            words.putRow(row++,lookupTable.vector(s));
        }

        for(String s : negative) {
            words.putRow(row++, lookupTable.vector(s).mul(-1));
        }

        INDArray mean = words.isMatrix() ? words.mean(0) : words;
        if(lookupTable instanceof  InMemoryLookupTable) {
            InMemoryLookupTable l = (InMemoryLookupTable) lookupTable();
            INDArray syn0 = l.getSyn0();
            INDArray weights = mean;
            INDArray distances = syn0.mmul(weights.transpose());
            distances.diviRowVector(distances.norm2(1));
            INDArray[] sorted = Nd4j.sortWithIndices(distances,0,false);
            INDArray sort = sorted[0];
            List<String> ret = new ArrayList<>();
            if(top > sort.length())
                top = sort.length();
            //there will be a redundant word
            int end = top + 1;
            for(int i = 0; i < end; i++) {
                String word = vocab().wordAtIndex(sort.getInt(i));
                if(union.contains(word)) {
                    end++;
                    if(end >= sort.length())
                        break;
                    continue;
                }
                ret.add(vocab().wordAtIndex(sort.getInt(i)));
            }


            return ret;
        }

        Counter<String> distances = new Counter<>();

        for(String s : vocab.words()) {
            INDArray otherVec = getWordVectorMatrix(s);
            double sim = Transforms.cosineSim(mean,otherVec);
            distances.incrementCount(s, sim);
        }


        distances.keepTopNKeys(top);
        return distances.keySet();


    }


    /**
     * Get the top n words most similar to the given word
     * @param word the word to compare
     * @param n the n to get
     * @return the top n words
     */
    public Collection<String> wordsNearest(String word,int n) {
        return wordsNearest(Arrays.asList(word),new ArrayList<String>(),n);

    }


    /**
     * Returns the similarity of 2 words
     * @param word the first word
     * @param word2 the second word
     * @return a normalized similarity (cosine similarity)
     */
    public double similarity(String word,String word2) {
        if(word.equals(word2))
            return 1.0;

        INDArray vector = Transforms.unitVec(getWordVectorMatrix(word));
        INDArray vector2 = Transforms.unitVec(getWordVectorMatrix(word2));
        if(vector == null || vector2 == null)
            return -1;
        return  Nd4j.getBlasWrapper().dot(vector, vector2);
    }

    @Override
    public VocabCache vocab() {
        return vocab;
    }

    @Override
    public WeightLookupTable lookupTable() {
        return lookupTable;
    }

    public void setLookupTable(WeightLookupTable lookupTable) {
        this.lookupTable = lookupTable;
    }

    public void setVocab(VocabCache vocab) {
        this.vocab = vocab;
    }

    public void setLayerSize(int layerSize) {
        this.layerSize = layerSize;
    }
}