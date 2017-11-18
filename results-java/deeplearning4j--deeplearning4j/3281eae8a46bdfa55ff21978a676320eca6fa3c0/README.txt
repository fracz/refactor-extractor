commit 3281eae8a46bdfa55ff21978a676320eca6fa3c0
Author: Sadat <sadatanwer@gmail.com>
Date:   Sat Feb 27 03:24:55 2016 +0100

    ScoreImprovementEpochTerminationCondition.java can now be initialized with the minImprovement factor. This is considered along with the maxEpochsWithImprovement. The difference of the improvements should cross this new threshold to be considered a significant improvement. This in help prevent overfitting of the data

    Added a new testcase (testMinImprovementNEpochsTermination) to ensure its working correctly


    Former-commit-id: 45c099e4743f2f15a20a52b7956780dc2ca600d7