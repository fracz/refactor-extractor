commit 7d92cf2b3b90aaac615bead93267f228d762d9b1
Author: Deepak <deepujain@gmail.com>
Date:   Thu May 22 15:08:12 2014 +0530

    Update IndexGeneratorJob.java

    CombineTextInputFormat instead of TextInputFormat combines multiple splits for a single mapper and reduces the strain on hadoop platform. It greatly improves job completion time as there are fewer number of mappers to bookkeep.