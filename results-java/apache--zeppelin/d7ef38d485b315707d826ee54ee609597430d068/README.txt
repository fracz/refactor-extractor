commit d7ef38d485b315707d826ee54ee609597430d068
Author: cto <goi.cto@gmail.com>
Date:   Sun Aug 9 08:25:34 2015 +0300

    Improve magic display system

    I have changed the InterpreterResult behavior so that if a magic word is detected all preceding text is omitted. This way we can use the display system and print out using the magic words in paragraphs such as spark.
    example:
    ```
    sc.parallelize(1 to 10)
    ```
    will output:
    ```
    res9: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[2] at parallelize at <console>:24
    ```

    and mix paragraphs such as:
    ```
    sc.parallelize(1 to 10)
    println("%table col\tcol2\nqwe\t123\n")
    ```
    will output the expected magic table
    ![selection_003](https://cloud.githubusercontent.com/assets/2227083/8849793/25ecf24c-314b-11e5-8136-ef689eaeb917.png)

    This behavior can be later extended to handle complex types.
    I have also added a JUnit test for the class

    Author: cto <goi.cto@gmail.com>

    Closes #164 from eranwitkon/master and squashes the following commits:

    afefcf2 [cto] Merge remote-tracking branch 'upstream/master'
    2eee9a2 [cto] change logic to match the new rule: paragraph type will be set by the first magic word in the paragraph.
    30922f2 [cto] Merge remote-tracking branch 'upstream/master'
    29470ce [cto] added more simple text test and comments in code
    3ef1584 [cto] refactoring to avoid the duplication of output message parsing
    5b2d1c8 [cto] Improve magic display system
    f3c4df3 [cto] improve magic word handling by omitting the prefix to the magic word