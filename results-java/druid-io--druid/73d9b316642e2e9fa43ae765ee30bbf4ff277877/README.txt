commit 73d9b316642e2e9fa43ae765ee30bbf4ff277877
Author: Roman Leventov <leventov@users.noreply.github.com>
Date:   Mon Mar 27 13:17:31 2017 -0600

    GenericIndexed minor bug fixes, optimizations and refactoring (#3951)

    * Minor bug fixes in GenericIndexed; Refactor and optimize GenericIndexed; Remove some unnecessary ByteBuffer duplications in some deserialization paths; Add ZeroCopyByteArrayOutputStream

    * Fixes

    * Move GenericIndexedWriter.writeLongValueToOutputStream() and writeIntValueToOutputStream() to SerializerUtils

    * Move constructors

    * Add GenericIndexedBenchmark

    * Comments

    * Typo

    * Note in Javadoc that IntermediateLongSupplierSerializer, LongColumnSerializer and LongMetricColumnSerializer are thread-unsafe

    * Use primitive collections in IntermediateLongSupplierSerializer instead of BiMap

    * Optimize TableLongEncodingWriter

    * Add checks to SerializerUtils methods

    * Don't restrict byte order in SerializerUtils.writeLongToOutputStream() and writeIntToOutputStream()

    * Update GenericIndexedBenchmark

    * SerializerUtils.writeIntToOutputStream() and writeLongToOutputStream() separate for big-endian and native-endian

    * Add GenericIndexedBenchmark.indexOf()

    * More checks in methods in SerializerUtils

    * Use helperBuffer.arrayOffset()

    * Optimizations in SerializerUtils