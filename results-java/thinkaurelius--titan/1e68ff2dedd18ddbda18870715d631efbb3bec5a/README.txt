commit 1e68ff2dedd18ddbda18870715d631efbb3bec5a
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Tue Oct 1 00:38:26 2013 -0400

    Refactoring CF compression in embedded Cassandra

    * Added support for disabling compression entirely in the embedded
      cassandra adapter per the config key introduced in #379

    * Removed code that instantiates a compressor class from the embedded
      store manager, instead passing a string to Cassandra's
      CompressionParameters constructor.  The constructor handles
      reflective instantiation on our behalf.  This is an improvement
      because CompressionParameters contains logic to prepend
      "org.apache.cassandra.io.compress." to compressor strings that
      contain no dots, allowing shorthands like "SnappyCompressor".
      That's also our current default compressor string.

    * Expanded debug log statements around compressor configuration in the
      embedded adapter