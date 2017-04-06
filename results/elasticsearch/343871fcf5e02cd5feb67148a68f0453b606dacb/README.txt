commit 343871fcf5e02cd5feb67148a68f0453b606dacb
Author: Alexander Reelsen <alexander@reelsen.net>
Date:   Thu Aug 1 14:42:41 2013 +0200

    Allow bin/plugin to set -D JVM parameters

    Currently the bin/plugin command did not allow one to set jvm parameters
    for startup. Usually this parameters are not needed (no need to configure
    heap sizes for such a short running process), but one could not set the
    configuration path. And that one is important for plugins in order find
    out, where the plugin directory is.

    This is especially problematic when elasticsearch is installed as
    debian/rpm package, because the configuration file is not placed in the
    same directory structure the plugin shell script is put.

    This pull request allows to call bin/plugin like this

    bin/plugin -Des.default.config=/etc/elasticsearch/elasticsearch.yml -install mobz/elasticsearch-head

    As a last small improvement, the PluginManager now outputs the directort
    the plugin was installed to in order to avoid confusion.

    Closes #3304