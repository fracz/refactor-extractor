commit de815da9422ae87bc7cff58a154d098908e6625f
Author: Benedict Jin <1571805553@qq.com>
Date:   Tue Apr 25 11:46:32 2017 +0800

    Some code refactor for better performance of `Avro-Extension` (#4092)

    * 1. Collections.singletonList instand of Arrays.asList; 2. close FSDataInputStream/ByteBufferInputStream for releasing resource; 3. convert com.google.common.base.Function into java.util.function.Function; 4. others code refactor

    * Put each param on its own line for  code style

    * Revert GenericRecordAsMap back about `Function`