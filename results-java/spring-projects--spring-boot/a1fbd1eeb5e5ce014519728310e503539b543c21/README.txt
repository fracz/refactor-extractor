commit a1fbd1eeb5e5ce014519728310e503539b543c21
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Tue Jan 20 11:39:50 2015 +0000

    Improve Servlet, Filter, and EventListener configuration diagnostics

    ServletContextInitializerBeans handles existing registration beans and
    also creates initializers for existing Servlet, Filter, etc beans.
    Debug logging has been added to this class to improve things in three
    main areas:

     - Distinguish between existing registration beans and those that are
       created to wrap a Servlet or Filter bean
     - Provide information about the resource (configuration class or XML)
       that resulted in the creation of the bean
     - Log information about EventListeners

    Closes gh-2177