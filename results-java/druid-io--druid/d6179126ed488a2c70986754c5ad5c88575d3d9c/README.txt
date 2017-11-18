commit d6179126ed488a2c70986754c5ad5c88575d3d9c
Author: T R Kyaw <1007242+tkyaw@users.noreply.github.com>
Date:   Wed Aug 30 17:44:33 2017 -0400

    Allow index job to utilize hadoop cluster information from job config. (#4626)

    * Allow ndex job to utilize hadoop cluster information from job config.

    * Add new method that inject system configuration and then job configuration.

    * Make changes to use HadoopDruidIndexerConfig.addJobProperties method.

    * refactor code for overloaded addJobProperties.