package org.deeplearning4j.text.sentenceiterator.labelaware;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.text.corpora.breaker.CorpusBreaker;
import org.deeplearning4j.text.sentenceiterator.UimaSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.uima.UimaResource;

import java.io.File;
import java.lang.reflect.Field;

/**
 *
 * Uima sentence iterator that is aware of the current file
 * @author Adam Gibson
 */
public class LabelAwareUimaSentenceIterator extends UimaSentenceIterator implements LabelAwareSentenceIterator {

    public LabelAwareUimaSentenceIterator(SentencePreProcessor preProcessor, String path,UimaResource resource,CorpusBreaker breaker) {
        super(preProcessor, path, resource,breaker);
    }

    public LabelAwareUimaSentenceIterator(String path, AnalysisEngine engine) throws ResourceInitializationException {
        super(path, new UimaResource(engine));
    }


    /**
     * Returns the current label for nextSentence()
     *
     * @return the label for nextSentence()
     */
    @Override
    public String currentLabel() {

        try {
            /**
             * Little bit hacky, but most concise way to do it.
             * Get the parent collection reader's current file.
             * The collection reader is basically a wrapper for a file iterator.
             * We can use this to ge the current file for the iterator.
             */
            Field f = reader.getClass().getDeclaredField("currentFile");
            f.setAccessible(true);
            File file = (File) f.get(reader);
            return file.getParentFile().getName();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }

    }
}