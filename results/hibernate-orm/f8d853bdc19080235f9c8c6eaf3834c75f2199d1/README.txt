commit f8d853bdc19080235f9c8c6eaf3834c75f2199d1
Author: Christian Beikov <christian.beikov@gmail.com>
Date:   Fri Apr 26 16:10:18 2013 +0300

    HHH-8831 improved exception message

    When having @JoinColumn in a @CollectionTable that do not point to valid columns, the message can be really confusing. These new lines will actually enrich the exception which is thrown that the problem appeared on a specific property of a specific class.

    Please include this, since it is not always clear what the actual problem is with a mapping when an exception occurs.