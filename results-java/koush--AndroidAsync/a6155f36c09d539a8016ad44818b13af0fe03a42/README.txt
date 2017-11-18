commit a6155f36c09d539a8016ad44818b13af0fe03a42
Author: Koushik Dutta <koushd@gmail.com>
Date:   Thu Mar 7 16:42:15 2013 -0800

    refactor FilteredDataSink to use BufferedDataSink. Fix up BufferedDataSink.close to close once the buffer is flushed.