commit 78036f7835941df05649fee9fd5a18782a8e5930
Author: gustav_delius <gustav_delius>
Date:   Thu Dec 23 07:28:14 2004 +0000

    Fixed event handling. Events must be deleted with delete_event.

    Instead of modifying event dates the quiz module currently deletes them and adds new ones. This could be improved in the future.