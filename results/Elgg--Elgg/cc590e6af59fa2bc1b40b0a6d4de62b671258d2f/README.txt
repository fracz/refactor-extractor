commit cc590e6af59fa2bc1b40b0a6d4de62b671258d2f
Author: Jerome Bakker <jeabakker@coldtrick.com>
Date:   Tue Jul 25 14:14:25 2017 +0200

    fix(email): improved formatting of email headers

    In order to better format the email headers a different approach is
    taken on how the auto detection of the headers works. This will improve
    the output in the actual email.

    ref: #10991