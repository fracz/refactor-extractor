// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.common.hash;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import org.junit.Assert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

/**
 * Various utilities for testing {@link HashFunction}s.
 *
 * @author andreou@google.com (Dimitris Andreou)
 * @author kak@google.com (Kurt Alfred Kluever)
 */
final class HashTestUtils {
  private HashTestUtils() {}

  /**
   * Converts a string, which should contain only ascii-representable characters, to a byte[].
   */
  static byte[] ascii(String string) {
    byte[] bytes = new byte[string.length()];
    for (int i = 0; i < string.length(); i++) {
      bytes[i] = (byte) string.charAt(i);
    }
    return bytes;
  }

  /**
   * Returns a byte array representation for a sequence of longs, in big-endian order.
   */
  static byte[] toBytes(ByteOrder bo, long... longs) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[longs.length * 8]).order(bo);
    for (long x : longs) {
      bb.putLong(x);
    }
    return bb.array();
  }

  interface HashFn {
    byte[] hash(byte[] input, int seed);
  }

  static void verifyHashFunction(HashFn hashFunction, int hashbits, int expected) {
    int hashBytes = hashbits / 8;

    byte[] key = new byte[256];
    byte[] hashes = new byte[hashBytes * 256];

    // Hash keys of the form {}, {0}, {0,1}, {0,1,2}... up to N=255,using 256-N as the seed
    for (int i = 0; i < 256; i++) {
      key[i] = (byte) i;
      int seed = 256 - i;
      byte[] hash = hashFunction.hash(Arrays.copyOf(key, i), seed);
      System.arraycopy(hash, 0, hashes, i * hashBytes, hash.length);
    }

    // Then hash the result array
    byte[] result = hashFunction.hash(hashes, 0);

    // interpreted in little-endian order.
    int verification = Integer.reverseBytes(Ints.fromByteArray(result));

    if (expected != verification) {
      throw new AssertionError("Expected: " + Integer.toHexString(expected)
          + " got: " + Integer.toHexString(verification));
    }
  }

  static void assertEqualHashes(byte[] expectedHash, byte[] actualHash) {
    if (!Arrays.equals(expectedHash, actualHash)) {
      Assert.fail(String.format("Should be: %x, was %x", expectedHash, actualHash));
    }
  }

  static final Funnel<Object> BAD_FUNNEL = new Funnel<Object>() {
    @Override public void funnel(Object object, Sink byteSink) {
      byteSink.putInt(object.hashCode());
    }
  };

  static enum RandomHasherAction {
    PUT_BOOLEAN() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        boolean value = random.nextBoolean();
        for (Sink sink : sinks) {
          sink.putBoolean(value);
        }
      }
    },
    PUT_BYTE() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        int value = random.nextInt();
        for (Sink sink : sinks) {
          sink.putByte((byte) value);
        }
      }
    },
    PUT_SHORT() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        short value = (short) random.nextInt();
        for (Sink sink : sinks) {
          sink.putShort(value);
        }
      }
    },
    PUT_CHAR() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        char value = (char) random.nextInt();
        for (Sink sink : sinks) {
          sink.putChar(value);
        }
      }
    },
    PUT_INT() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        int value = random.nextInt();
        for (Sink sink : sinks) {
          sink.putInt(value);
        }
      }
    },
    PUT_LONG() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        long value = random.nextLong();
        for (Sink sink : sinks) {
          sink.putLong(value);
        }
      }
    },
    PUT_FLOAT() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        float value = random.nextFloat();
        for (Sink sink : sinks) {
          sink.putFloat(value);
        }
      }
    },
    PUT_DOUBLE() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        double value = random.nextDouble();
        for (Sink sink : sinks) {
          sink.putDouble(value);
        }
      }
    },
    PUT_BYTES() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        byte[] value = new byte[random.nextInt(128)];
        random.nextBytes(value);
        for (Sink sink : sinks) {
          sink.putBytes(value);
        }
      }
    },
    PUT_BYTES_INT_INT() {
      @Override void performAction(Random random, Iterable<? extends Sink> sinks) {
        byte[] value = new byte[random.nextInt(128)];
        random.nextBytes(value);
        int off = random.nextInt(value.length + 1);
        int len = random.nextInt(value.length - off + 1);
        for (Sink sink : sinks) {
          sink.putBytes(value);
        }
      }
    };

    abstract void performAction(Random random, Iterable<? extends Sink> sinks);

    private static final RandomHasherAction[] actions = values();

    static RandomHasherAction pickAtRandom(Random random) {
      return actions[random.nextInt(actions.length)];
    }
  }

  /**
   * Test that the hash function contains no funnels. A funnel is a situation where a set of input
   * (key) bits 'affects' a strictly smaller set of output bits. Funneling is bad because it can
   * result in more-than-ideal collisions for a non-uniformly distributed key space. In practice,
   * most key spaces are ANYTHING BUT uniformly distributed. A bit(i) in the input is said to
   * 'affect' a bit(j) in the output if two inputs, identical but for bit(i), will differ at output
   * bit(j) about half the time
   *
   * <p>Funneling is pretty simple to detect. The key idea is to find example keys which
   * unequivocably demonstrate that funneling cannot be occuring. This is done bit-by-bit. For
   * each input bit(i) and output bit(j), two pairs of keys must be found with all bits identical
   * except bit(i). One pair must differ in output bit(j), and one pair must not. This proves that
   * input bit(i) can alter output bit(j).
   */
  static void checkNoFunnels(HashFunction function) {
    Random rand = new Random(0);
    int keyBits = 32;
    int hashBits = function.bits();

    // output loop tests input bit
    for (int i = 0; i < keyBits; i++) {
      int same = 0x0; // bitset for output bits with same values
      int diff = 0x0; // bitset for output bits with different values
      int count = 0;
      int maxCount = (int) (2 * Math.log(2 * keyBits * hashBits) + 0.999);
      while (same != 0xffffffff || diff != 0xffffffff) {
        int key1 = rand.nextInt();
        // flip input bit for key2
        int key2 = key1 ^ (1 << i);
        // get hashes
        int hash1 = function.newHasher().putInt(key1).hash().asInt();
        int hash2 = function.newHasher().putInt(key2).hash().asInt();
        // test whether the hash values have same output bits
        same |= ~(hash1 ^ hash2);
        // test whether the hash values have different output bits
        diff |= (hash1 ^ hash2);

        count++;
        // check whether we've exceeded the probabilistically
        // likely number of trials to have proven no funneling
        if (count > maxCount) {
          Assert.fail("input bit(" + i + ") was found not to affect all " +
               hashBits + " output bits; The unaffected bits are " +
               "as follows: " + ~(same & diff) + ". This was " +
               "determined after " + count + " trials.");
        }
      }
    }
  }

  /**
   * Test for avalanche. Avalanche means that output bits differ with roughly 1/2 probability on
   * different input keys. This test verifies that each possible 1-bit key delta achieves avalanche.
   *
   * <p>For more information: http://burtleburtle.net/bob/hash/avalanche.html
   */
  static void checkAvalanche(HashFunction function, int trials, double epsilon) {
    Random rand = new Random(0);
    int keyBits = 32;
    int hashBits = function.bits();
    for (int i = 0; i < keyBits; i++) {
      int[] same = new int[hashBits];
      int[] diff = new int[hashBits];
      // go through trials to compute probability
      for (int j = 0; j < trials; j++) {
        int key1 = rand.nextInt();
        // flip input bit for key2
        int key2 = key1 ^ (1 << i);
        // compute hash values
        int hash1 = function.newHasher().putInt(key1).hash().asInt();
        int hash2 = function.newHasher().putInt(key2).hash().asInt();
        for (int k = 0; k < hashBits; k++) {
          if ((hash1 & (1 << k)) == (hash2 & (1 << k))) {
            same[k] += 1;
          } else {
            diff[k] += 1;
          }
        }
      }
      // measure probability and assert it's within margin of error
      for (int j = 0; j < hashBits; j++) {
        double prob = (double) diff[j] / (double) (diff[j] + same[j]);
        Assert.assertEquals(0.50d, prob, epsilon);
      }
    }
  }

  /**
   * Test for 2-bit characteristics. A characteristic is a delta in the input which is repeated in
   * the output. For example, if f() is a block cipher and c is a characteristic, then
   * f(x^c) = f(x)^c with greater than expected probability. The test for funneling is merely a test
   * for 1-bit characteristics.
   *
   * <p>There is more general code provided by Bob Jenkins to test arbitrarily sized characteristics
   * using the magic of gaussian elimination: http://burtleburtle.net/bob/crypto/findingc.html.
   */
  static void checkNo2BitCharacteristics(HashFunction function) {
    Random rand = new Random(0);
    int keyBits = 32;

    // get every one of (keyBits choose 2) deltas:
    for (int i = 0; i < keyBits; i++) {
      for (int j = 0; j < keyBits; j++) {
        if (j <= i) continue;
        int count = 0;
        int maxCount = 20; // the probability of error here is miniscule
        boolean diff = false;

        while (diff == false) {
          int delta = (1 << i) | (1 << j);
          int key1 = rand.nextInt();
          // apply delta
          int key2 = key1 ^ delta;

          // get hashes
          int hash1 = function.newHasher().putInt(key1).hash().asInt();
          int hash2 = function.newHasher().putInt(key2).hash().asInt();

          // this 2-bit candidate delta is not a characteristic
          // if deltas are different
          if ((hash1 ^ hash2) != delta) {
            diff = true;
            continue;
          }

          // check if we've exceeded the probabilistically
          // likely number of trials to have proven 2-bit candidate
          // is not a characteristic
          count++;
          if (count > maxCount) {
            Assert.fail("2-bit delta (" + i + ", " + j + ") is likely a " +
                 "characteristic for this hash. This was " +
                 "determined after " + count + " trials");
          }
        }
      }
    }
  }

  /**
   * Test for avalanche with 2-bit deltas. Most probabilities of output bit(j) differing are well
   * within 50%.
   */
  static void check2BitAvalanche(HashFunction function, int trials, double epsilon) {
    Random rand = new Random(0);
    int keyBits = 32;
    int hashBits = function.bits();
    for (int bit1 = 0; bit1 < keyBits; bit1++) {
      for (int bit2 = 0; bit2 < keyBits; bit2++) {
        if (bit2 <= bit1) continue;
        int delta = (1 << bit1) | (1 << bit2);
        int[] same = new int[hashBits];
        int[] diff = new int[hashBits];
        // go through trials to compute probability
        for (int j = 0; j < trials; j++) {
          int key1 = rand.nextInt();
          // flip input bit for key2
          int key2 = key1 ^ delta;
          // compute hash values
          int hash1 = function.newHasher().putInt(key1).hash().asInt();
          int hash2 = function.newHasher().putInt(key2).hash().asInt();
          for (int k = 0; k < hashBits; k++) {
            if ((hash1 & (1 << k)) == (hash2 & (1 << k))) {
              same[k] += 1;
            } else {
              diff[k] += 1;
            }
          }
        }
        // measure probability and assert it's within margin of error
        for (int j = 0; j < hashBits; j++) {
          double prob = (double) diff[j] / (double) (diff[j] + same[j]);
          Assert.assertEquals(0.50d, prob, epsilon);
        }
      }
    }
  }

  /**
   * Checks that a Hasher returns the same HashCode when given the same input, and also
   * that the collision rate looks sane.
   */
  static void assertInvariants(HashFunction hashFunction) {
    int objects = 100;
    Set<HashCode> hashcodes = Sets.newHashSetWithExpectedSize(objects);
    for (int i = 0; i < objects; i++) {
      Object o = new Object();
      HashCode hashcode1 = hashFunction.newHasher().putObject(o, HashTestUtils.BAD_FUNNEL).hash();
      HashCode hashcode2 = hashFunction.newHasher().putObject(o, HashTestUtils.BAD_FUNNEL).hash();
      Assert.assertEquals(hashcode1, hashcode2); // idempotent
      Assert.assertEquals(hashFunction.bits(), hashcode1.bits());
      Assert.assertEquals(hashFunction.bits(), hashcode1.asBytes().length * 8);
      hashcodes.add(hashcode1);
    }
    Assert.assertTrue(hashcodes.size() > objects * 0.95); // quite relaxed test

    assertHashBytesThrowsCorrectExceptions(hashFunction);
    assertIndependentHashers(hashFunction);
  }

  static void assertHashBytesThrowsCorrectExceptions(HashFunction hashFunction) {
    hashFunction.hashBytes(new byte[64], 0, 0);

    try {
      hashFunction.hashBytes(new byte[128], -1, 128);
      Assert.fail();
    } catch (IndexOutOfBoundsException expected) {}
    try {
      hashFunction.hashBytes(new byte[128], 64, 256 /* too long len */);
      Assert.fail();
    } catch (IndexOutOfBoundsException expected) {}
    try {
      hashFunction.hashBytes(new byte[64], 0, -1);
      Assert.fail();
    } catch (IndexOutOfBoundsException expected) {}
  }

  static void assertIndependentHashers(HashFunction hashFunction) {
    int numActions = 100;
    // hashcodes from non-overlapping hash computations
    HashCode expected1 = randomHash(hashFunction, new Random(1L), numActions);
    HashCode expected2 = randomHash(hashFunction, new Random(2L), numActions);

    // equivalent, but overlapping, computations (should produce the same results as above)
    Random random1 = new Random(1L);
    Random random2 = new Random(2L);
    Hasher hasher1 = hashFunction.newHasher();
    Hasher hasher2 = hashFunction.newHasher();
    for (int i = 0; i < numActions; i++) {
      RandomHasherAction.pickAtRandom(random1).performAction(random1, ImmutableSet.of(hasher1));
      RandomHasherAction.pickAtRandom(random2).performAction(random2, ImmutableSet.of(hasher2));
    }

    Assert.assertEquals(expected1, hasher1.hash());
    Assert.assertEquals(expected2, hasher2.hash());
  }

  static HashCode randomHash(HashFunction hashFunction, Random random, int numActions) {
    Hasher hasher = hashFunction.newHasher();
    for (int i = 0; i < numActions; i++) {
      RandomHasherAction.pickAtRandom(random).performAction(random, ImmutableSet.of(hasher));
    }
    return hasher.hash();
  }
}