commit ad79b90f21dcde6f741c47a251882c0a47c5f698
Author: Adam Powell <adamp@google.com>
Date:   Wed Jun 19 11:33:28 2013 -0700

    Action bar refactoring and transitions

    * Remove the extra occurrence of the Up caret view and reuse the
      standard home view.

    * Use new transition APIs to animate changes in action bar content.

    Change-Id: I7af3bb580ef4bff7d8dec9e21649b856fe73c77b