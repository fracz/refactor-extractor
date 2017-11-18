commit 56dc6abdee413e9f9b65e1f8140f02ebede2c8fe
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Mon Dec 22 14:40:30 2014 +0100

    switch from EventHandler to WorkHandler in disruptor usage

     * remove assisted injection factories from process and output buffer processors
     * use simple providers instead
     * refactor journal module binding so it doesn't get in the way with the command line tools
     * remove latch usage in journalreader, the processbuffer now starts during construction and is always available
     * remove services to start processbuffer and outputbuffer, they aren't used anymore