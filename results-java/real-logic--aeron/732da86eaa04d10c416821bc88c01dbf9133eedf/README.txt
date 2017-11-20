commit 732da86eaa04d10c416821bc88c01dbf9133eedf
Author: Todd L. Montgomery <todd.montgomery@kaazing.com>
Date:   Wed Apr 2 13:07:14 2014 -0700

    refactored out NIO selector handling into NioSelector class that is to be used by both ReceiverThread and AdminThread.