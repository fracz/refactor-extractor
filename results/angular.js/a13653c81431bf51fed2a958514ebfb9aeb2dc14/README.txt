commit a13653c81431bf51fed2a958514ebfb9aeb2dc14
Author: Di Peng <pengdi@google.com>
Date:   Mon Aug 15 16:17:12 2011 -0700

    refactor(angular): externalize script load order into JSON

    - move all script load order into angularFiles.js
    - rakefile and angular-bootstrap.js use angularFiles.js to get script orders
    - gen_jstd_configs.js uses angularFiles.js to generate various jstd config files
    - run gen_jstd_configs.js whenever we run server.sh

    Closes #470