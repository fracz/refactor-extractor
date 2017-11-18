commit 17f1ec42fa15cc9755cde38a78eedb132ae501b3
Author: Adam Murdoch <adam@gradle.com>
Date:   Tue May 23 17:56:49 2017 +1000

    Started reworking `ExternalResourceRepository` so that no network request spans more than one method call on `ExternalResourceRepository` or `ExternalResource`.

    This is work in progress and this change leaves `ExternalResourceRepository` in an intermediate state between the old and new behaviours. A subsequent change will remove the old behaviours entirely.

    The semantics of the `ExternalResourceDownloadBuildOperationType` have changed in several ways:

    - The build operation wraps the entire network request, rather than the "download" portion of the request.
    - The build operation events are fired regardless of whether the network request was successful or not.
    - No content type or content length are included in the operation's detail.

    This build operation type is yet to be refactored or documented to reflect these changes.