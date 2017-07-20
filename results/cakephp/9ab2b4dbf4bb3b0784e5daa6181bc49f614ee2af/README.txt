commit 9ab2b4dbf4bb3b0784e5daa6181bc49f614ee2af
Author: Mark Story <mark@mark-story.com>
Date:   Tue May 12 20:29:25 2015 -0400

    Mark @ file syntax as deprecated.

    The @ is not a completely safe way to send files. If a developer passes
    along user data, that user data can include arbitrary files. This is not
    desirable, and can go really wrong with URLs.

    This also improves content-type and filename detection for local stream
    resources.

    Refs #6540