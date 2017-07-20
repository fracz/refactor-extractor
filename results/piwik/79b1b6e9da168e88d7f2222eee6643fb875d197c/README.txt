commit 79b1b6e9da168e88d7f2222eee6643fb875d197c
Author: Thomas Steur <thomas.steur@googlemail.com>
Date:   Thu Jul 3 00:53:26 2014 +0200

    this is a huge performance improvement if it works... hideShowMetrics took about 400ms on my i7 as it was called 900 times and dropped to about 40ms now... the getRequestVar was quite expensive. Also cache reportMetadata but I am not sure if it will work with the tests. Only works unless the reports do not depend on GET or POST params... but they should not be used... saves about 300ms each time getReportMetadata is called more than once (some to it 5 times or so with same params)