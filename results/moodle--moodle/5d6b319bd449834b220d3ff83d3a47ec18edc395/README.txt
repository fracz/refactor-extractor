commit 5d6b319bd449834b220d3ff83d3a47ec18edc395
Author: moodler <moodler>
Date:   Thu Apr 14 09:28:07 2005 +0000

    Some serious performance improvements to messaging!

    Scripts have been split up and trimmed down.

    If a discussion window is left open then updates happen on a slower
    and slower rate (exponentially increasing delay) to help keep
    load low on a server.