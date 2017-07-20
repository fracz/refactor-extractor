commit dfd6bdb5dc96b47dc3be8ebf2b5df5be14df8a1c
Author: Jose Lorenzo Rodriguez <jose.zap@gmail.com>
Date:   Thu Jan 1 19:11:24 2015 +0100

    Removing automatic setting of content-length header in Response.

    It was added back then in the hope it would help improve some bechmarks
    but ended up causing multiple bugs in its history to little or not
    benefit.