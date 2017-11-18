commit bf66cd1164066fa3c7f8ef37aacb708b70639bc1
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Feb 21 22:38:44 2014 +0100

    GRADLE-3027 Synchronized creation of ivy ids. This concludes the changes and refactorings in the area of ivy ids for now. I'm not sure if I have chosen a good strategy here, a new synchronisation may introduce a performance hit. Also, in previous refactorings, I've replaced constructor calls with static factory methods on IvyUtil. This leads to more interning (memory hit?). Also, constructor in IvyUtil does replace null with empty string for group and version which deviates from the behavior of the constructor. Please review. I'm open to updating my strategy and/or reverting some of those changes if needed.