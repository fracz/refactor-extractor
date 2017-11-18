package org.deeplearning4j.datasets.fetchers;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.base.LFWLoader;
import org.deeplearning4j.datasets.DataSet;


/**
 * Data fetcher for the LFW faces dataset
 * @author Adam Gibson
 *
 */
public class LFWDataFetcher extends BaseDataFetcher {

	/**
	 *
	 */
	private static final long serialVersionUID = -7473748140401804666L;
	private LFWLoader loader;
	public final static int NUM_IMAGES = 13233;


	public LFWDataFetcher(int imageWidth,int imageHeight) {
		try {
			loader = new LFWLoader(imageWidth,imageHeight);
			loader.getIfNotExists();
			inputColumns = loader.getNumPixelColumns();
			numOutcomes = loader.getNumNames();
			totalExamples = NUM_IMAGES;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to fetch images",e);
		}
	}


	public LFWDataFetcher() {
		this(200,200);
	}




	@Override
	public void fetch(int numExamples) {
		if(!hasMore())
			throw new IllegalStateException("Unable to get more; there are no more images");



		//we need to ensure that we don't overshoot the number of examples total
		List<DataSet> toConvert = new ArrayList<>();

		for(int i = 0; i < numExamples; i++,cursor++) {
			if(!hasMore())
				break;
			toConvert.add(loader.getDataFor(cursor));
		}

		initializeCurrFromList(toConvert);
	}




	@Override
	public DataSet next() {
		DataSet next = super.next();
		next.normalizeZeroMeanZeroUnitVariance();
		return next;
	}



}