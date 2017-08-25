commit 1c4ae1c45d68be66e9e664d67d0cd72e91496f56
Author: KeRNel_x86 <kernins@gmail.com>
Date:   Sun Nov 2 07:09:18 2014 +0400

    Fix for #215 and some other related improvements

    - Replaced AMQPWriter chrByteSplit() & byteSplit() with more reliable and ~35% more effective packBigEndian(), which also supports negative numbers
    - Fixed #215 and some other issues on 64bit
    - Added support for signed longlong, some perf optimizations
    - Improved existing and added absent range checks, fixed write_long() with vals >php_int_max on 32bit
    - Fixed existing and added new tests, highly increased num of iters
    - Reformatted the code for better readability
    - Reverted int values to be encoded as long in write_array(), as longlong not supported in AMQP-0.8
    - fixed exception msg in test
    - Fixed #216. Added wrapper/helper classes AMQPArray & AMQPTable, which may be directly passed to AMQPWriter::write_array() & write_table()
    - examples now use AMQPTable
    - Fixed 'b' type to be read as short-short-int, as per amqp spec.
    - Added few data types
    - made read_value() private, just like its counterpart write_value()
    - Took care of field types mess - http://www.rabbitmq.com/amqp-0-9-1-errata.html#section_3
    - By default rabbitMQ set is used
    - reverted Decimal to use signed long as per http://www.rabbitmq.com/amqp-0-9-1-errata.html#section_3
    - removed unused ArrayAccess iface from AMQPAbstractCollection
    - Incoming message headers as AMQPTable, easily retrievable in their original form via getNativeData()
    - Added examples for message headers
    - fixed test for 32bit systems
    - added full void (null) support
    - adapted to prev changes (message application_headers is now AMQPTable instance)
    - fixed assertEquals sematic to conform ($expected, $actual)
    - fixed setUp() was affecting global state causing consecutive tests to fail
    - Changed some methods to accept empty $arguments to make them friendly to downstream type-hinted wrappers like someMethod($foo, $bar, AMQPTable $args=null) {basicConsume(...., $args);}
    - fixed tests to pass on Travis's ancient phpunit version