package org.deeplearning4j.topicmodeling;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.deeplearning4j.berkeley.Counter;
import org.deeplearning4j.word2vec.inputsanitation.InputHomogenization;
import org.jblas.DoubleMatrix;


public class DocumentMatrix {

	private Counter<String> words = new Counter<String>();
	private boolean initializedWithCounter = false;
	private Counter<String> docWords = new Counter<String>();

	public DocumentMatrix(File file,Counter<String> words) {
		if(file.isDirectory())
			throw new IllegalArgumentException("File must not be a directory");
		this.words = words;
		initializedWithCounter = true;
		convert(file);

	}


	public DocumentMatrix(File file) {
		if(file.isDirectory())
			throw new IllegalArgumentException("File must not be a directory");
		convert(file);
	}

	private void convert(File file) {
		if(initializedWithCounter) {

			try {
				LineIterator iter = FileUtils.lineIterator(file);
				while(iter.hasNext()) {
					StringTokenizer t = new StringTokenizer(new InputHomogenization(iter.nextLine()).transform());
					while(t.hasMoreTokens()) {
						docWords.incrementCount(t.nextToken(), 1.0);
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException("Unable to read file",e);
			}
		}
		else {

			try {
				LineIterator iter = FileUtils.lineIterator(file);
				while(iter.hasNext()) {
					StringTokenizer t = new StringTokenizer(new InputHomogenization(iter.nextLine()).transform());
					while(t.hasMoreTokens()) {
						words.incrementCount(t.nextToken(), 1.0);
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException("Unable to read file",e);
			}
		}

	}

	public Counter<String> toCounter() {
		if(initializedWithCounter)
			return docWords;
		return words;
	}

	public DoubleMatrix toNormalized() {
		if(initializedWithCounter) {
			DoubleMatrix ret = new DoubleMatrix(words.size());
			int count = 0;
			for(String s : words.keySet())
				ret.put(count++,docWords.getProbability(s));
			return ret;

		}
		else {
			DoubleMatrix ret = new DoubleMatrix(words.size());
			int count = 0;
			for(String s : words.keySet())
				ret.put(count++,words.getProbability(s));
			return ret;

		}

	}


	public DoubleMatrix toMatrix() {
		if(initializedWithCounter) {
			DoubleMatrix ret = new DoubleMatrix(words.size());
			int count = 0;
			for(String s : words.keySet())
				ret.put(count++,docWords.getCount(s));
			return ret;

		}
		else {
			DoubleMatrix ret = new DoubleMatrix(words.size());
			int count = 0;
			for(String s : words.keySet())
				ret.put(count++,words.getCount(s));
			return ret;

		}
	}




}