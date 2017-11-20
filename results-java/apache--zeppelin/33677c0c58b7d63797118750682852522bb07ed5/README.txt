commit 33677c0c58b7d63797118750682852522bb07ed5
Author: AhyoungRyu <ahyoungryu@apache.org>
Date:   Thu Oct 6 17:31:39 2016 +0900

    [ZEPPELIN-1489] Remove jdbc-like interpreter example properties and improve JDBC docs

    ### What is this PR for?
    Currently we can use `%jdbc(prefix)` for multi database connections(e.g. configuring both postgresql and hive in one JDBC interpreter). But after #1360 merged, Zeppelin doesn't support `%jdbc(prefix)` to `%prefix` anymore. So auto-prefix feature is not working for `%jdbc(prefix)`. The auto-prefix only works for `%prefix` now. Considering this status, it would be better we remove the JDBC connection examples in `jdbc/interpreter-setting.json` (this is come from #1096) so that users can create several JDBC interpreters instead of using multiple connections with one JDBC interpreter.  I removed the related contents in `jdbc.md` as well.

    Also, the contents of [current JDBC documentation page](http://zeppelin.apache.org/docs/0.7.0-SNAPSHOT/interpreter/jdbc.html) is quite confusing and unorganized. So I updated the contents with some screenshot images to guide "How to create JDBC interpreter", "How to edit the interpreter properties for the connection", "How to use `%prefix` with the interpreter", and so on.

    ### What type of PR is it?
    Improvement & Documentation

    ### What is the Jira issue?
    [ZEPPELIN-1489](https://issues.apache.org/jira/browse/ZEPPELIN-1489)

    ### How should this be tested?
    * Removing example properties in `interpreter-setting.json`
      1. after applying this patch and build with `mvn clean package -DskipTests -Pspark-1.6 -pl 'jdbc,zeppelin-interpreter,zeppelin-web,zeppelin-server,zeppelin-zengine,zeppelin-display'`
      2. create JDBC interpreter and check whether the example settings are gone or not

    * JDBC docs
      1. Build only `docs/` dir as described in [here](https://github.com/apache/zeppelin/blob/master/docs/README.md#build-documentation)
      2. Go to `interpreter -> JDBC` and just compare this locally builded page with [the original JDBC page](https://zeppelin.apache.org/docs/0.7.0-SNAPSHOT/interpreter/jdbc.html)

    ### Screenshots (if appropriate)
     - Before
    <img width="1579" alt="before" src="https://cloud.githubusercontent.com/assets/10060731/19045323/1ff0c706-89d3-11e6-9b6f-dc75877f81f3.png">

     - After
    <img width="944" alt="screen shot 2016-10-04 at 1 05 00 am" src="https://cloud.githubusercontent.com/assets/10060731/19045324/24a9187a-89d3-11e6-90d6-b80acbc6af7c.png">

     - Some parts of updated JDBC docs
    Since many contents are changed, it would be better to build `docs/` locally to review all of the change.

    <img width="695" alt="screen shot 2016-10-04 at 12 36 39 am" src="https://cloud.githubusercontent.com/assets/10060731/19043794/9d9a32fc-89cc-11e6-9d15-f6036a1b738e.png">

    <img width="704" alt="screen shot 2016-10-04 at 12 36 59 am" src="https://cloud.githubusercontent.com/assets/10060731/19043800/a62fc90e-89cc-11e6-976d-5c697729eca4.png">

    <img width="677" alt="screen shot 2016-10-04 at 12 37 31 am" src="https://cloud.githubusercontent.com/assets/10060731/19043807/acbc9766-89cc-11e6-8c73-eab1cc18440b.png">

    <img width="688" alt="screen shot 2016-10-04 at 12 37 43 am" src="https://cloud.githubusercontent.com/assets/10060731/19043816/b06b5690-89cc-11e6-9298-a20b49fea622.png">

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? yes

    Author: AhyoungRyu <ahyoungryu@apache.org>

    Closes #1480 from AhyoungRyu/ZEPPELIN-1489 and squashes the following commits:

    76bf55e [AhyoungRyu] Minor update
    d5aaa97 [AhyoungRyu] Remove useless screenshot images
    c6f9ed4 [AhyoungRyu] Add screenshot images
    99a18e2 [AhyoungRyu] Remove jdbc setting examples in interpreter-setting.json
    050ecc0 [AhyoungRyu] Update jdbc.md
    20da102 [AhyoungRyu] Redshit -> Redshift in JDBCInterpreter.java