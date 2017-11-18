commit de0a7b27e7bb060befddf7a229752125a4893cd3
Author: Deepak <deepujain@gmail.com>
Date:   Thu May 22 15:06:56 2014 +0530

    Update DetermineHashedPartitionsJob.java

    CombineTextInputFormat instead of TextInputFormat combines multiple splits for a single mapper and reduces the strain on hadoop platform. It greatly improves job completion time as there are fewer number of mappers to bookkeep.