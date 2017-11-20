commit fa510f2dab8f63b3dfaab39b1c94ec74d63b2547
Author: Jesse Vincent <jesse@fsck.com>
Date:   Fri Dec 24 22:48:19 2010 +0000

    Turns out that UUID generation is expensive. deferring it until first access improves startup performance.