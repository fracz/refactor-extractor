commit 8a43eb30daec65dbaf9dbd3f4ad29db122e6fda2
Author: Jakub Valtar <jakub.valtar@gmail.com>
Date:   Fri Oct 16 21:45:35 2015 +0200

    Depth sorter improvement

    Fix ordering when two triangles in a plane share vertices. Triangle
    still has to be tested with other triangles.