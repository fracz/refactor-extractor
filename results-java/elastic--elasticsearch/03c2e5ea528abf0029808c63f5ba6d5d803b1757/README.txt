commit 03c2e5ea528abf0029808c63f5ba6d5d803b1757
Author: Shay Banon <kimchy@gmail.com>
Date:   Thu Nov 24 20:03:25 2011 +0200

    improve how decoding is done on the transport layer, embedding FrameDecoder into the message handler, and reducing allocation of buffers and better guess into allocating cumalation buffers