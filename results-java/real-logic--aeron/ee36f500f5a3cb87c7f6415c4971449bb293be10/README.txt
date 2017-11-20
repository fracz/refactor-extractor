commit ee36f500f5a3cb87c7f6415c4971449bb293be10
Author: Todd L. Montgomery <todd.montgomery@kaazing.com>
Date:   Tue Aug 5 20:15:55 2014 -0700

    [Java]: refactored retransmit handling to have retransmit performed on Sender thread. Separated flow control strategy and retransmit handler out of driver publication to separate the concerns and make threading cleaner. Refactored send channel endpoint to hold the components of publication instead of just the publication.