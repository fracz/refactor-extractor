commit 4131bcbec745485161edd0f304186dbc649b9e90
Author: Adrien Grand <jpountz@gmail.com>
Date:   Tue May 19 14:13:44 2015 +0200

    Search: Make FilteredQuery a forbidden API.

    This commit makes FilteredQuery a forbidden API and also removes some more usage
    of the Filter API. There are some remaining code using filters for parent/child
    queries but I'm not touching this as they are already being refactored in #6511.