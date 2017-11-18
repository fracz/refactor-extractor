commit 1427a1bab649ba97b9917873468bc69d447369be
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Mon Dec 10 15:47:49 2012 -0800

    Simplying event stream implementation

    This version doesn't need the complexity of the multi-threaded writing so I refactored to have a single-writer with a simple queue drain model for handing the messages from poller thread to writer thread.