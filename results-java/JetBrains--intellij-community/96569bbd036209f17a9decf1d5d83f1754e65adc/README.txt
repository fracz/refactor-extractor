commit 96569bbd036209f17a9decf1d5d83f1754e65adc
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Sun Nov 27 17:40:31 2011 +0300

    IDEA-76988 IDEA-77239 Git Http adjustments to handle redirects and repository urls without ".git'

    1. GitHttpAdapter refactoring: introduce GitHttpRemoteCommand and its implementations for fetch, push and clone. Adjust callWithAuthRetry to use them. Main reason - ability to change url of the command for #2 and #3.
    2. Catch TransportException for error described in IDEA-76988 "301 Moved Permanently". It happens when one is trying to connect a BitBucket repository via http instead of https, and the request is redirected. If that happens, adjust fetch/push/clone url to use https instead of http, and try again.
    3. Catch NoRemoteRepositoryException for IDEA-77239 where repository on GitHub can be defined without ".git" (and native Git understands this). If that happens, adjust the request by adding .git to the end of it, and try again.
    4. Because of the possibility of url-modifications don't pass remote name to fetch and push commands. Instead pass directly the url of the remote. But to make JGit understand, what to fetch/push, pass the fetch/push refspec to the command.
     5. Fix GitConfig and GitRemote to understand multiple fetch and push specs. Add test.