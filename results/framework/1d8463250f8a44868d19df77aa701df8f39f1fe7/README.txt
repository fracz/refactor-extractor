commit 1d8463250f8a44868d19df77aa701df8f39f1fe7
Author: Sébastien Nikolaou <info@sebdesign.eu>
Date:   Thu Jan 14 23:55:22 2016 +0200

    Add doesntExpectJobs method

    Now MockApplicationServices provides methods for testing both expected
    and unexpected events and jobs by mocking the underlying dispatchers.

    The trait has been refactored to share the common functionalities.