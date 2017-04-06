commit 9ee315a9c840e2189404edfb2a33b9e352e6f098
Author: Ryan Ernst <ryan@iernst.net>
Date:   Fri Nov 6 23:20:40 2015 -0800

    Build: Improve integ test to match ant behavior

    Many other improvements:
    * Use spaces in ES path
    * Use space in path for plugin file installation
    * Use a different cwd than ES home
    * Use jps to ensure process being stopped is actually elasticsearch
    * Stop ES if pid file already exists
    * Delete pid file when successfully killed

    Also, refactored the cluster formation code to be a little more organized.

    closes #14464