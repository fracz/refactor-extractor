commit fff27ef8f6b39fc4140345037d6656547a1f43de
Author: Aaron Davidson <aaron@databricks.com>
Date:   Mon Jun 16 10:15:17 2014 -0700

    Use JSON as the EditLog's serialization format

    Using JSON as a serialization format brings a couple great benefits:
      - Evolvability of the log format -- JSON allows us to easily add,
        remove, or change fields in our serialization format and still
        successfully read from old logs. This is infeasible today.
      - Human readability of the log, enabling easier manual intervention
        if something goes wrong during recovery and better error reports.

    There are some potential downsides of using JSON over binary:
      - Performance overheads of serializing more data and as JSON.
        Deserialization costs are less important, as they are only during
        recovery.
      - Space overheads of human-readable String data overy binary data.

    In order to help quantify the cost of JSON, I did a brief performance
    evaluation between this implementation and the prior binary format.
    The evaluation included simply continuously writing one type of log
    message for 10 seconds, then counting (A) how many entries were written
    and (B) how large the log had grown. Note that a flush() was inserted
    after every write.

    The results are as follows:

    **Large Entries [1]**
    ```
    Version | Ops/sec | Bytes/op
    JSON    | 105,894 | 300
    Binary  |   5,410 | 155
    ```

    **Small Entries [2]**
    ```
    Version | Ops/sec | Bytes/op
    JSON    | 174,400 | 100
    Binary  | 124,267 |  10
    ```

    Somewhat inexplicably, the JSON version always won in speed, perhaps because
    it buffers each entry before writing it out, whereas the binary format
    writes every field individually. Regardless of relative difference, the JSON
    format was able to sustain >100k operations per second, which seems sufficient.

    There was certainly significant blowup in the actual log size, though, up to
    10x for very small logs. Hopefully this will not be impactful on actual disk
    usage, though, as we continuously clean up old edit logs as we merge them into
    the durable image file.

    [1] Large entry:

    ```java
    editLog.createDependency(Lists.newArrayList(1,2,3), Lists.newArrayList(4,5,6), "commandPrefix",
        Lists.newArrayList(ByteBuffer.wrap(new byte[4]), ByteBuffer.wrap(new byte[8])),
        "comment", "framework", "frameworkVersion", DependencyType.getDependencyType(1),
        1234, 34932482432L);
    ```

    [2] Small entry:
    ```java
    editLog.setPinned(123, true)
    ```