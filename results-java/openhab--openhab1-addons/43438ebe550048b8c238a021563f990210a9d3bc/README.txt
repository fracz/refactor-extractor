commit 43438ebe550048b8c238a021563f990210a9d3bc
Author: jarlebh <jarlebh@gmail.com>
Date:   Fri May 2 22:26:50 2014 +0200

    Set the current dim level when sending instead of waiting for reply event. This improves responsiveness with increment and decrement. This is related to #1014