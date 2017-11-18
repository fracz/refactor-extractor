commit 28fe57b3f0f17b7be0be4d5702f35de9657349f5
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Feb 15 19:02:59 2013 +0100

    Fixed a problem with configuring module selection reasons. If the module was participating in the second pass of conlifct resolution, it selection reason was already 'conflict resolution'. So the decorating factory that adds the selection reason must take this under consideration. Added toString() to improve debugging.