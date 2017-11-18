commit defb6a336daa8fb13e22a6b05ac47886f20e819f
Author: kimchy <kimchy@gmail.com>
Date:   Tue Feb 23 21:22:40 2010 +0200

    refactor json handling to use byte[] instead of string for better performance, storage, and memory consumption (apply to count and delete by query)