commit 7475e836dc2bdd29142eaf210262fba354b745ed
Author: Vinod Kumar Vavilapalli <vinodkv@apache.org>
Date:   Tue Feb 28 00:32:19 2012 +0000

    MAPREDUCE-3901. Modified JobHistory records in YARN to lazily load job and task reports so as to improve UI response times. Contributed by Siddarth Seth.


    git-svn-id: https://svn.apache.org/repos/asf/hadoop/common/trunk@1294417 13f79535-47bb-0310-9956-ffa450edef68