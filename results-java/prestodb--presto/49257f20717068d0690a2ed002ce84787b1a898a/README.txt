commit 49257f20717068d0690a2ed002ce84787b1a898a
Author: Christopher Berner <christopherberner@gmail.com>
Date:   Mon Dec 5 09:34:30 2016 -0800

    Make long running exchanges more tolerant of errors

    Also refactor HttpPageBufferClient to use Backoff