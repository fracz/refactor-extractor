commit 4f58b2bd3e8b2aa9852e92a52ae698112f8ae615
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Sun Dec 21 13:25:54 2014 +0100

    add journal decode command, improve journal show

    journal show now displays:
     - number of messages in the journal
     - the first offset in the journal (useful for use with journal decode)
     - the actual last offset (not the next offset after the end of the log)

    simple version of journal decode
     - allows printing metadata about the message
     - uses the actual codec to parse the RawMessage from the log

    overriding the codec or dumping the raw values including all metadata is not yet supported (neither from RawMessage nor from Message)