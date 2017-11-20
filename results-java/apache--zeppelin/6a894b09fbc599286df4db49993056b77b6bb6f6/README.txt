commit 6a894b09fbc599286df4db49993056b77b6bb6f6
Author: Lee moon soo <moon@apache.org>
Date:   Mon Jun 29 14:07:37 2015 -0700

    [ZEPPELIN-97][ZEPPELIN-134] pyspark issue with mllib api

    There were issue [ZEPPELIN-97](https://issues.apache.org/jira/browse/ZEPPELIN-97) with pyspark 1.4. The reason is, from pyspark 1.4, java gateway is created with `auto_convert = True` option. This PR fixes the problem.

    This PR also handles [ZEPPELIN-134](https://issues.apache.org/jira/browse/ZEPPELIN-134), inject sqlContext.

    And it finally improves to print more verbose stacktrace message, for example

    from

    ```
    (<type 'exceptions.AttributeError'>, AttributeError("'list' object has no attribute '_get_object_id'",), <traceback object at 0x392b638>)
    ```

    to

    ```
    Traceback (most recent call last):
      File "/var/folders/zt/nd4j13y14jjg7_5pc4xgy7t80000gn/T//zeppelin_pyspark.py", line 110, in <module>
        eval(compiledCode)
      File "<string>", line 3, in <module>
      File "/Users/moon/Projects/zeppelin/spark-1.4.0-bin-hadoop2.3/python/pyspark/sql/dataframe.py", line 1200, in withColumn
        return self.select('*', col.alias(colName))
      File "/Users/moon/Projects/zeppelin/spark-1.4.0-bin-hadoop2.3/python/pyspark/sql/dataframe.py", line 738, in select
        jdf = self._jdf.select(self._jcols(*cols))
      File "/Users/moon/Projects/zeppelin/spark-1.4.0-bin-hadoop2.3/python/pyspark/sql/dataframe.py", line 630, in _jcols
        return self._jseq(cols, _to_java_column)
      File "/Users/moon/Projects/zeppelin/spark-1.4.0-bin-hadoop2.3/python/pyspark/sql/dataframe.py", line 617, in _jseq
        return _to_seq(self.sql_ctx._sc, cols, converter)
      File "/Users/moon/Projects/zeppelin/spark-1.4.0-bin-hadoop2.3/python/pyspark/sql/column.py", line 60, in _to_seq
        return sc._jvm.PythonUtils.toSeq(cols)
      File "/Users/moon/Projects/zeppelin/spark-1.4.0-bin-hadoop2.3/python/lib/py4j-0.8.2.1-src.zip/py4j/java_gateway.py", line 529, in __call__
        [get_command_part(arg, self.pool) for arg in new_args])
      File "/Users/moon/Projects/zeppelin/spark-1.4.0-bin-hadoop2.3/python/lib/py4j-0.8.2.1-src.zip/py4j/protocol.py", line 265, in get_command_part
        command_part = REFERENCE_TYPE + parameter._get_object_id()
    AttributeError: 'list' object has no attribute '_get_object_id'
    ```

    Author: Lee moon soo <moon@apache.org>

    Closes #129 from Leemoonsoo/ZEPPELIN-97 and squashes the following commits:

    1fa4bf6 [Lee moon soo] apply auto_convert for spark 1.4
    bce3c1d [Lee moon soo] Print more stacktrace