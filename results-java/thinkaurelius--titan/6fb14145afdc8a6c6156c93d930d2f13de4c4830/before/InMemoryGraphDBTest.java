package com.thinkaurelius.titan.graphdb.test;

import com.thinkaurelius.titan.graphdb.transaction.InMemoryTitanGraph;
import org.junit.Test;


public class InMemoryGraphDBTest extends AbstractGraphDBTest {

	public InMemoryGraphDBTest() {
		super(null);
	}

	@Override
	public void open() {
		tx=new InMemoryTitanGraph();
	}

	@Override
	public void clopen() {
		//Do nothing!
	}

	@Override
	public void close() {
		//Do nothing
	}

    //Overwrite non-applicable test
    @Test
    public void basicTest() {}

	//Overwrite non-applicable test
	@Test
	public void neighborhoodTest() {}

	//Overwrite non-applicable test
	@Test
	public void primitiveCreateAndRetrieve() { }

	//Overwrite non-applicable test
	@Test
	public void multipleIndexRetrieval() { }

	//Overwrite non-applicable test
	@Test
	public void rangeRetrieval() { }

	//Overwrite non-applicable test
	@Test
	public void createDelete() { }
}