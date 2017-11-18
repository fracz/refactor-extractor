commit 7c66b6ed3b953adbe518203027d0ccba94e7cbc8
Author: Oliver Woodman <olly@google.com>
Date:   Tue Feb 10 12:25:13 2015 +0000

    HLS optimization #1 (refactor).

    This is the start of a sequence of changes to fix the ref'd
    github issue. Currently TsExtractor involves multiple memory
    copy steps:

    DataSource->Ts_BitArray->Pes_BitArray->Sample->SampleHolder

    This is inefficient, but more importantly, the copy into
    Sample is problematic, because Samples are of dynamically
    varying size. The way we end up expanding Sample objects to
    be large enough to hold the data being written means that we
    end up gradually expanding all Sample objects in the pool
    (which wastes memory), and that we generate a lot of GC churn,
    particularly when switching to a higher quality which can
    trigger all Sample objects to expand.

    The fix will be to reduce the copy steps to:

    DataSource->TsPacket->SampleHolder

    We will track Pes and Sample data with lists of pointers into
    TsPackets, rather than actually copying the data. We will
    recycle these pointers.

    The following steps are approximately how the refactor will
    progress:

    1. Start reducing use of BitArray. It's going to be way too
    complicated to track bit-granularity offsets into multiple packets,
    and allow reading across packet boundaries. In practice reads
    from Ts packets are all byte aligned except for small sections,
    so we'll move over to using ParsableByteArray instead, so we
    only need to track byte offsets.

    2. Move TsExtractor to use ParsableByteArray except for small
    sections where we really need bit-granularity offsets.

    3. Do the actual optimization.

    Issue: #278