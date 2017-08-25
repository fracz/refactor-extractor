commit 58bedc8a310955dbc00e738bec1dc4f442eaff4e
Author: borekb <borekb@gmail.com>
Date:   Wed Jan 30 21:51:39 2013 +0100

    Updated idfilter() function for IIS

    There is a condition inside idfilter() function that helps Apache on Windows to properly handle colon as a namespace separator by replacing it with semicolon. However, this is not necessary on Microsoft IIS server so the condition was improved.