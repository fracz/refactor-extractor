commit 15c2686b846a82d4a634bcba4e3b7adf99d057b1
Author: Ismayil Khayredinov <ismayil.khayredinov@gmail.com>
Date:   Sun Dec 13 21:16:31 2015 +0100

    feature(forms): moves datepicker init to AMD and improves dev usability

    Datepicker initialization now takes place in an AMD module on per input basis, which allows
    plugin to configure datepicker options by passing datepicker_options to the input view.