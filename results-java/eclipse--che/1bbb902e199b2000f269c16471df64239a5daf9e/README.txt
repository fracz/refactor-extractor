commit 1bbb902e199b2000f269c16471df64239a5daf9e
Author: Igor Vinokur <ivinokur@codenvy.com>
Date:   Thu Nov 3 16:32:50 2016 +0200

    CHE-1297: Git service refactoring (#1515)

    The goal of this refactoring is to set correct type of HTTP methods in GitService. Now almost all HTTP methods in this service are POST methods, but there are methods that are used to get some content, so they should be marked as GET method. Also I had to change such methods to receive query parameters instead of body.
    Implemented parameter objects to use them instead of using DTO as parameter in GitConnection methods.