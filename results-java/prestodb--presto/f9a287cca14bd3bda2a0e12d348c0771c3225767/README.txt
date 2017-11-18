commit f9a287cca14bd3bda2a0e12d348c0771c3225767
Author: Raghav Sethi <raghavsethi@fb.com>
Date:   Thu Dec 22 17:59:13 2016 -0800

    Add parallel deserialization and compression to exchanges

    TaskOutputOperator now splits, serializes and compresses pages
    before putting them in output buffers. ExchangeOperators deserialize
    and decompress pages. Serializing and compressing/decompressing pages
    one at a time increases the allocation rate. Several compute-light
    queries show remarkable wall time reductions.

    Enabling compression helps pack output buffers (especially
    PartitionedOutputBuffer) more densely to improve exchange
    throughput.