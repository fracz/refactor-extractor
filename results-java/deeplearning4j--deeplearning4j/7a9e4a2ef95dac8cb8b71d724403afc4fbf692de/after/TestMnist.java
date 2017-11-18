package org.deeplearning4j.example.mnist;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.deeplearning4j.datasets.DataSet;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.dbn.DBN;
import org.deeplearning4j.eval.Evaluation;
import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMnist {

	private static Logger log = LoggerFactory.getLogger(TestMnist.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DBN dbn = new DBN.Builder().buildEmpty();
		InputStream is = new FileInputStream(new File(args[0]));
		dbn.load(is);

		//batches of 10, 60000 examples total
		DataSetIterator iter = new MnistDataSetIterator(10,60000);

		Evaluation eval = new Evaluation();

		while(iter.hasNext()) {
			DataSet next = iter.next();
			DoubleMatrix predict = dbn.predict(next.getFirst());
			DoubleMatrix labels = next.getSecond();
			eval.eval(labels, predict);
			log.info("Current stats " + eval.stats());
		}

		log.info("Prediciton f scores and accuracy");
		log.info(eval.stats());

	}

}