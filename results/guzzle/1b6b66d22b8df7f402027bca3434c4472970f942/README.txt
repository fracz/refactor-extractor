commit 1b6b66d22b8df7f402027bca3434c4472970f942
Author: Jeremy Livingston <jeremyjlivingston@gmail.com>
Date:   Fri Oct 3 14:04:24 2014 -0400

    Remove default message in RequestException

    The default message value in RequestException could never be used as a result
    of the requirement for a request. This fix removes the default value of
    '' from the constructor to improve clarity.