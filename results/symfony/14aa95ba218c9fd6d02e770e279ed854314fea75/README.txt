commit 14aa95ba218c9fd6d02e770e279ed854314fea75
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Feb 15 04:47:19 2011 +0100

    added the concept of a main DIC extension for bundles

    This allows for better conventions and better error messages if you
    use the wrong configuration alias in a config file.

    This is also the first step for a bigger refactoring of how the configuration
    works (see next commits).

     * Bundle::registerExtensions() method has been renamed to Bundle::build()

     * The "main" DIC extension must be renamed to the new convention to be
       automatically registered:

          SensioBlogBundle -> DependencyInjection\SensioBlogExtension

     * The main DIC extension alias must follow the convention:

          sensio_blog for SensioBlogBundle

     * If you have more than one extension for a bundle (which should really
       never be the case), they must be registered manually by overriding the
       build() method

     * If you use YAML or PHP for your configuration, renamed the following
       configuration entry points in your configs:

          app -> framework
          webprofiler -> web_profiler
          doctrine_odm -> doctrine_mongo_db