<?php

return array(

    /*
    |--------------------------------------------------------------------------
    | Collections
    |--------------------------------------------------------------------------
    |
    | Basset is built around collections. A collection contains assets for
    | your application. Collections can contain both stylesheets and
    | javascripts.
    |
    | A default "application" collection is ready for immediate use. It makes
    | a couple of assumptions about your directory structure.
    |
    | /public
    |    /assets
    |        /stylesheets
    |            /less
    |            /sass
    |        /javascripts
    |            /coffeescripts
    |
    | You can overwrite this collection or remove it by publishing the config.
    |
    */

    'collections' => array(

        'pre' => function($collection) {
            $directory = $collection->directory('js', function($collection) {
                $collection->javascript('modernizr-2.6.2-respond-1.1.0.min.js');
            });
            $directory->apply('JsMin');
        },

        'main' => function($collection) {
            $directory = $collection->directory('css', function($collection) {
                if (Config::get('theme.name') == 'default') {
                    $collection->stylesheet('bootstrap.min.css');
                } elseif (Config::get('theme.name') == 'legacy') {
                    $collection->stylesheet('bootstrap.min.css');
                    $collection->stylesheet('bootstrap-theme.min.css');
                } else {
                    $collection->stylesheet('bootstrap.'.Config::get('theme.name').'.min.css');
                }
                $collection->stylesheet('jasny-bootstrap.min.css');
                $collection->stylesheet('font-awesome.min.css');
                $collection->stylesheet('cms-main.css')->apply('CssMin');
            });
            $directory->apply('UriRewriteFilter');

            $directory = $collection->directory('js', function($collection) {
                $collection->javascript('jquery-1.10.2.min.js');
                $collection->javascript('jquery.timeago.js');
                $collection->javascript('cms-timeago.js');
                $collection->javascript('cms-restfulizer.js');
                $collection->javascript('bootstrap.min.js');
                $collection->javascript('jasny-bootstrap.min.js');
            });
            $directory->apply('JsMin');
        },

        'form' => function($collection) {
            $directory = $collection->directory('css', function($collection) {
                $collection->stylesheet('bootstrap-switch.css');
                $collection->stylesheet('bootstrap-datetimepicker.min.css');
                $collection->stylesheet('bootstrap-editable.css');
                $collection->stylesheet('bootstrap-markdown.min.css');
            });
            $directory->apply('CssMin');
            $directory->apply('UriRewriteFilter');

            $directory = $collection->directory('js', function($collection) {
                $collection->javascript('jquery.form.js');
                $collection->javascript('typeahead.min.js');
                $collection->javascript('bootstrap-switch.js');
                $collection->javascript('bootstrap-datetimepicker.min.js');
                $collection->javascript('cms-picker.js');
                $collection->javascript('bootstrap-editable.min.js');
                $collection->javascript('bootstrap-markdown.js');
            });
            $directory->apply('JsMin');
        },

        'comment' => function($collection) {
            $directory = $collection->directory('js', function($collection) {
                $collection->javascript('cms-comment-core.js');
                $collection->javascript('cms-comment-edit.js');
                $collection->javascript('cms-comment-delete.js');
                $collection->javascript('cms-comment-create.js');
                $collection->javascript('cms-comment-main.js');
            });
            $directory->apply('JsMin');
        },

        'logviewer' => function($collection) {
            $directory = $collection->directory('css', function($collection) {
                $collection->stylesheet('cms-logviewer.css');
            });
            $directory->apply('CssMin');
            $directory->apply('UriRewriteFilter');

            $directory = $collection->directory('js', function($collection) {
                $collection->javascript('cms-logviewer.js');
            });
            $directory->apply('JsMin');
        }

    ),

    /*
    |--------------------------------------------------------------------------
    | Production Environment
    |--------------------------------------------------------------------------
    |
    | Basset needs to know what your production environment is so that it can
    | respond with the correct assets. When in production Basset will attempt
    | to return any built collections. If a collection has not been built
    | Basset will dynamically route to each asset in the collection and apply
    | the filters.
    |
    | The last method can be very taxing so it's highly recommended that
    | collections are built when deploying to a production environment.
    |
    | You can supply an array of production environment names if you need to.
    |
    */

    'production' => array('production', 'prod'),

    /*
    |--------------------------------------------------------------------------
    | Build, Publish, and Node Paths
    |--------------------------------------------------------------------------
    |
    | When building assets with Artisan the build path will be where the
    | collections are stored.
    |
    | If your collections publish assets outside of the public directory they
    | will be published to the publish path.
    |
    | Many filters use Node to build assets. We recommend you install your
    | Node modules locally at the root of your application, however you can
    | specify additional paths to your modules.
    |
    */

    'paths' => array(

        'build' => 'assets',

        'publish' => '/',

        'node' => array(

            base_path().'/node_modules'

        )

    ),

    /*
    |--------------------------------------------------------------------------
    | Debug
    |--------------------------------------------------------------------------
    |
    | Enable debugging to have potential errors or problems encountered
    | during operation logged to a rotating file setup.
    |
    */

    'debug' => false,

    /*
    |--------------------------------------------------------------------------
    | Gzip Built Collections
    |--------------------------------------------------------------------------
    |
    | To get the most speed and compression out of Basset you can enable Gzip
    | for every collection that is built via the command line. This is applied
    | to both collection builds and development builds.
    |
    | You can use the --gzip switch for on-the-fly Gzipping of collections.
    |
    */

    'gzip' => false,

    /*
    |--------------------------------------------------------------------------
    | Asset and Filter Aliases
    |--------------------------------------------------------------------------
    |
    | You can define aliases for commonly used assets or filters.
    | An example of an asset alias:
    |
    |   'layout' => 'stylesheets/layout/master.css'
    |
    | Filter aliases are slightly different. You can define a simple alias
    | similar to an asset alias.
    |
    |   'YuiCss' => 'Yui\CssCompressorFilter'
    |
    | However if you want to pass in options to an aliased filter then define
    | the alias as a nested array. The key should be the filter and the value
    | should be a callback closure where you can set parameters for a filters
    | constructor, etc.
    |
    |   'YuiCss' => array('Yui\CssCompressorFilter', function($filter)
    |   {
    |       $filter->setArguments('path/to/jar');
    |   })
    |
    |
    */

    'aliases' => array(

        'assets' => array(),

        'filters' => array(

            /*
            |--------------------------------------------------------------------------
            | Less Filter Alias
            |--------------------------------------------------------------------------
            |
            | Filter is applied only when asset has a ".less" extension and it will
            | attempt to find missing constructor arguments.
            |
            */

            'Less' => array('LessFilter', function($filter)
            {
                $filter->whenAssetIs('.*\.less')->findMissingConstructorArgs();
            }),

            /*
            |--------------------------------------------------------------------------
            | Sass Filter Alias
            |--------------------------------------------------------------------------
            |
            | Filter is applied only when asset has a ".sass" or ".scss" extension and
            | it will attempt to find missing constructor arguments.
            |
            */

            'Sass' => array('Sass\ScssFilter', function($filter)
            {
                $filter->whenAssetIs('.*\.(sass|scss)')->findMissingConstructorArgs();
            }),

            /*
            |--------------------------------------------------------------------------
            | CoffeeScript Filter Alias
            |--------------------------------------------------------------------------
            |
            | Filter is applied only when asset has a ".coffee" extension and it will
            | attempt to find missing constructor arguments.
            |
            */

            'CoffeeScript' => array('CoffeeScriptFilter', function($filter)
            {
                $filter->whenAssetIs('.*\.coffee')->findMissingConstructorArgs();
            }),

            /*
            |--------------------------------------------------------------------------
            | CssMin Filter Alias
            |--------------------------------------------------------------------------
            |
            | Filter is applied only when within the production environment and when
            | the "CssMin" class exists.
            |
            */

            'CssMin' => array('CssMinFilter', function($filter)
            {
                $filter->whenAssetIsStylesheet()->whenProductionBuild()->whenClassExists('CssMin');
            }),

            /*
            |--------------------------------------------------------------------------
            | JsMin Filter Alias
            |--------------------------------------------------------------------------
            |
            | Filter is applied only when within the production environment and when
            | the "JsMin" class exists.
            |
            */

            'JsMin' => array('JSMinFilter', function($filter)
            {
                $filter->whenAssetIsJavascript()->whenProductionBuild()->whenClassExists('JSMin');
            }),

            /*
            |--------------------------------------------------------------------------
            | UriRewrite Filter Alias
            |--------------------------------------------------------------------------
            |
            | Filter gets a default argument of the path to the public directory.
            |
            */

            'UriRewriteFilter' => array('UriRewriteFilter', function($filter)
            {
                $filter->setArguments(public_path())->whenAssetIsStylesheet();
            })

        )

    )

);