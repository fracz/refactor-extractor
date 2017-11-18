package com.thinkaurelius.titan.diskstorage.locking;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;

import com.thinkaurelius.titan.diskstorage.locking.consistentkey.ConsistentKeyLockTransaction;
import com.thinkaurelius.titan.diskstorage.util.KeyColumn;
import com.thinkaurelius.titan.diskstorage.locking.consistentkey.LocalLockMediator;
import org.junit.Test;

public class LocalLockMediatorTest {

	private static final String LOCK_NAMESPACE = "test";
	private static final ByteBuffer LOCK_ROW = ByteBuffer.wrap(new byte[] { 1 });
	private static final ByteBuffer LOCK_COL = ByteBuffer.wrap(new byte[] { 1 });
	private static final KeyColumn kc = new KeyColumn(LOCK_ROW, LOCK_COL);
//	private static final long LOCK_EXPIRATION_TIME_MS = 1;
//	private static final long SLEEP_MS = LOCK_EXPIRATION_TIME_MS * 1000;
	private static final ConsistentKeyLockTransaction mockTx1 = mock(ConsistentKeyLockTransaction.class);
	private static final ConsistentKeyLockTransaction mockTx2 = mock(ConsistentKeyLockTransaction.class);

	@Test
	public void testLockExpiration() throws InterruptedException {
		LocalLockMediator llm = new LocalLockMediator(LOCK_NAMESPACE);

		assertTrue(llm.lock(kc, mockTx1, 0));
		assertTrue(llm.lock(kc, mockTx2, Long.MAX_VALUE));

		llm = new LocalLockMediator(LOCK_NAMESPACE);

		assertTrue(llm.lock(kc, mockTx1, Long.MAX_VALUE));
		assertFalse(llm.lock(kc, mockTx2, Long.MAX_VALUE));
	}
}