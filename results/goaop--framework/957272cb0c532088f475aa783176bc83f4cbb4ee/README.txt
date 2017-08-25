commit 957272cb0c532088f475aa783176bc83f4cbb4ee
Author: blanchonvincent <blanchon.vincent@gmail.com>
Date:   Wed Dec 19 21:56:07 2012 +0100

    Proposition to improve the transformer workflow

    Wrap the source with the stream metadatas. Because, someone transformer
    need source, someone do not need source. With a global wrapper (the
    StreamMetaData), each transform is based on only one object.