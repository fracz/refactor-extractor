commit fc9820f40a7385433763b8b0c9e2c4af10d6b4a8
Author: yhawke <yhawke@gmail.com>
Date:   Fri Jan 17 15:13:53 2014 +0000

    Complete re-design and re-implementation of the Active Scan Progress panel with skipping and so on... currenlty it forces the isStop() method on the required plugins, so all developers should improve this with an isStop() check on main loops... Should be understood how to implement the completion percentage for running plugins...