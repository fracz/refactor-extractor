commit fa35b1c3b33b3724ff7b42f6350fe707c2b1a1b7
Author: HyunGil Jeong <hyungil.jeong@navercorp.com>
Date:   Fri Jun 5 16:19:33 2015 +0900

    #329 refactored Thrift trace headers

    This was done to ease up future refactoring that would pull up trace
    headers used for tracing various RPC calls into a single common class.