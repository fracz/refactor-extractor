package org.deeplearning4j.scaleout.perform.models.word2vec;

import org.deeplearning4j.bagofwords.vectorizer.TextVectorizer;
import org.deeplearning4j.bagofwords.vectorizer.TfidfVectorizer;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.word2vec.Huffman;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache;
import org.deeplearning4j.scaleout.conf.Configuration;
import org.deeplearning4j.scaleout.job.JobIterator;
import org.deeplearning4j.scaleout.perform.models.word2vec.iterator.Word2VecJobIterator;
import org.deeplearning4j.scaleout.testsupport.BaseTestDistributed;
import org.deeplearning4j.text.invertedindex.InvertedIndex;
import org.deeplearning4j.text.invertedindex.LuceneInvertedIndex;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.UimaSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.UimaTokenizerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by agibsonccc on 11/29/14.
 */
public class DistributedWord2VecTest extends BaseTestDistributed {
    private  InMemoryLookupTable table;
    private TextVectorizer vectorizer;
    private InvertedIndex invertedIndex;
    private VocabCache cache;


    @Override
    public String workPerformFactoryClassName() {
        return Word2VecPerformerFactory.class.getName();
    }

    @Before
    public void before() throws  Exception {
        ClassPathResource resource = new ClassPathResource("/basic2/line2.txt");
        File file = resource.getFile().getParentFile();
        SentenceIterator iter = UimaSentenceIterator.createWithPath(file.getAbsolutePath());
        new File("cache.ser").delete();
        cache = new InMemoryLookupCache();


        TokenizerFactory t = new UimaTokenizerFactory();

        table =  (InMemoryLookupTable) new InMemoryLookupTable
                .Builder().vectorLength(100).useAdaGrad(false).cache(cache)
                .lr(0.025f).build();

        if(invertedIndex == null)
            invertedIndex = new LuceneInvertedIndex.Builder()
                    .cache(cache)
                    .build();

        vectorizer = new TfidfVectorizer.Builder().index(invertedIndex)
                .cache(cache).iterate(iter)
                .tokenize(t).build();

        vectorizer.fit();


        Huffman huffman = new Huffman(cache.vocabWords());
        huffman.build();


        init();
    }

    @After
    public void after() throws Exception {
        tearDown();
    }

    @Override
    public Configuration createConfiguration() {
        Configuration conf = super.createConfiguration();
        Word2VecPerformer.configure(table,invertedIndex,conf);
        return conf;
    }

    @Test
    public void testRunner() {
        distributed.train();

    }


    @Override
    public JobIterator createIterator() {
        return new Word2VecJobIterator(vectorizer,table,cache,stateTracker);
    }
}