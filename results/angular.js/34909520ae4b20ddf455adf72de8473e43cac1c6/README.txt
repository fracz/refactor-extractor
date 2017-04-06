commit 34909520ae4b20ddf455adf72de8473e43cac1c6
Author: Andres Ornelas <aornelas@google.com>
Date:   Wed Oct 27 11:29:51 2010 -0700

    add optional label to dsl with selectors to improve test and output readability

    e.g.
    Before:
       code:   element('.actions ul li a').click();
       output: element .actions ul li a click
    After
       code:   element('.actions ul li a', "'Configuration' link").click();
       output: element 'Configuration' link ( .actions ul li a ) click