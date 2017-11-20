commit f5f127b684608c8c06ed6e9dec7cbcbd2146da0a
Author: Alexander Shoshin <Alexander_Shoshin@epam.com>
Date:   Thu Mar 23 16:12:24 2017 +0300

    [MINOR] Job parameters synchronization

    ### What is this PR for?
    We need to synchronize setting up job result, exception, errorMessage and dateFinished in the end of `Job.run()` method. This will guarantee that we won't see these parameters in inconsistent state in multithreading environment, e.g., if we see that a dateFinished parameter has not been set up yet we will be sure that we also don't have a job final result.
    This code refactoring will make it possible to resolve issue [ZEPPELIN-1856](https://issues.apache.org/jira/browse/ZEPPELIN-1856).

    ### What type of PR is it?
    Refactoring

    ### Todos
    * [ ] - Synchronize `setResult()`, `exception`, `errorMessage` and `dateFinished` in `Job.run()` method.
    * [ ] - Synchronize getters and setters for `exception`, `errorMessage` and `dateFinished`.

    ### Questions:
    * Does the licenses files need update? **no**
    * Is there breaking changes for older versions? **no**
    * Does this needs documentation? **no**

    Author: Alexander Shoshin <Alexander_Shoshin@epam.com>

    Closes #2188 from AlexanderShoshin/job-results-synchronization and squashes the following commits:

    bc9bbc9 [Alexander Shoshin] setting job results, error, dateFinished were synchronized