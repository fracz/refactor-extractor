commit a7ddca6caa56170f86189051388a538df74ea215
Author: Ketan Padegaonkar <KetanPadegaonkar@gmail.com>
Date:   Tue Jul 11 16:33:48 2017 +0530

    Compress console logs using gzip before sending it via a websocket

    There were a few limitations of the current implementation:
    * sending 1000 lines every 500ms caused large console logs to take a
      long time to render completely
    * lack of compression meant that it took significantly longer to
      download and render the console logs

    While compression can be performed by using the `permessage-deflate`
    websocket extension, it is unsupported by safari. Safari uses a
    non-standard `x-webkit-deflate-frame`. Also the current version of jetty
    that we use, uses a draft version of the compression spec.

    The new implementation gets around the these issues by buffering upto
    1MB of logs. The 1MB buffer is now compressed before sending it across
    the wire, so the data actually sent across the wire is < 1MB.

    Any content that is less than 512b is not compressed, since the
    compression does not bring about any significant improvement.

    1MB seems rather large, but choosing a smaller number meant a lot of
    round-trip for longer log files, thereby increasing the console log
    render time.

    This also increases memory usage by 1MB for the buffer and atleast 10%
    overhead for compression. The overhead may increase slightly based on
    how the console log chunk compresses, but we don't expect it to be
    greater than 20%.