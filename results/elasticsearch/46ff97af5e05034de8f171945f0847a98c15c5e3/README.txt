commit 46ff97af5e05034de8f171945f0847a98c15c5e3
Author: kimchy <kimchy@gmail.com>
Date:   Tue Feb 23 21:30:10 2010 +0200

    refactor json handling to use byte[] instead of string for better performance, storage, and memory consumption (apply to search)