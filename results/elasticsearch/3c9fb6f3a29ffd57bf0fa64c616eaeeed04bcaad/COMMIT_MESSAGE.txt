commit 3c9fb6f3a29ffd57bf0fa64c616eaeeed04bcaad
Author: Robert Muir <rmuir@apache.org>
Date:   Tue Aug 4 22:16:30 2015 -0400

    improve integration tests output when ES cannot be started.

    This can happen for a number of reasons, including bugs.
    Today you will get a super crappy failure, telling you a .pid file
    was not found... you can go look in target/integ-tests/elasticsearch-xxx/logs
    and examine the log file, but thats kinda a pain and not easy if its a jenkins
    server.

    Instead we can fail like this:
    ```
    start-external-cluster-with-plugin:
         [echo] Installing plugin elasticsearch-example-jvm-plugin...
        [mkdir] Created dir: /home/rmuir/workspace/elasticsearch/plugins/jvm-example/target/integ-tests/temp
         [exec] -> Installing elasticsearch-example-jvm-plugin...
         [exec] Plugins directory [/home/rmuir/workspace/elasticsearch/plugins/jvm-example/target/integ-tests/elasticsearch-2.0.0-SNAPSHOT/plugins] does not exist. Creating...
         [exec] Trying file:/home/rmuir/workspace/elasticsearch/plugins/jvm-example/target/releases/elasticsearch-example-jvm-plugin-2.0.0-SNAPSHOT.zip ...
         [exec] Downloading ...........DONE
         [exec] PluginInfo{name='example-jvm-plugin', description='Demonstrates all the pluggable Java entry points in Elasticsearch', site=false, jvm=true, classname=org.elasticsearch.plugin.example.ExampleJvmPlugin, isolated=true, version='2.0.0-SNAPSHOT'}
         [exec] Installed example-jvm-plugin into /home/rmuir/workspace/elasticsearch/plugins/jvm-example/target/integ-tests/elasticsearch-2.0.0-SNAPSHOT/plugins/example-jvm-plugin
         [echo] Starting up external cluster...
         [echo] [2015-08-04 22:02:55,130][INFO ][org.elasticsearch.node   ] [smoke_tester] version[2.0.0-SNAPSHOT], pid[4321], build[e2a47d8/2015-08-05T00:50:08Z]
         [echo] [2015-08-04 22:02:55,130][INFO ][org.elasticsearch.node   ] [smoke_tester] initializing ...
         [echo] [2015-08-04 22:02:55,259][INFO ][org.elasticsearch.plugins] [smoke_tester] loaded [uber-plugin], sites []
         [echo] [2015-08-04 22:02:55,260][ERROR][org.elasticsearch.bootstrap] Exception
         [echo] java.lang.NullPointerException
         [echo]     at org.elasticsearch.common.settings.Settings$Builder.put(Settings.java:1051)
         [echo]     at org.elasticsearch.plugins.PluginsService.updatedSettings(PluginsService.java:208)
         [echo]     at org.elasticsearch.node.Node.<init>(Node.java:148)
         [echo]     at org.elasticsearch.node.NodeBuilder.build(NodeBuilder.java:157)
         [echo]     at org.elasticsearch.bootstrap.Bootstrap.setup(Bootstrap.java:177)
         [echo]     at org.elasticsearch.bootstrap.Bootstrap.main(Bootstrap.java:272)
         [echo]     at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:28)
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD FAILURE
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 25.178 s
    [INFO] Finished at: 2015-08-04T22:03:14-05:00
    [INFO] Final Memory: 32M/515M
    [INFO] ------------------------------------------------------------------------
    [ERROR] Failed to execute goal org.apache.maven.plugins:maven-antrun-plugin:1.8:run (integ-setup) on project elasticsearch-example-jvm-plugin: An Ant BuildException has occured: The following error occurred while executing this line:
    [ERROR] /home/rmuir/workspace/elasticsearch/plugins/jvm-example/target/dev-tools/ant/integration-tests.xml:176: The following error occurred while executing this line:
    [ERROR] /home/rmuir/workspace/elasticsearch/plugins/jvm-example/target/dev-tools/ant/integration-tests.xml:142: ES instance did not start
    ```