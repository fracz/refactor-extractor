commit 0b56831be599ccc8f73c51943f43b6765bfe4d7a
Author: Colin Viebrock <colin@viebrock.ca>
Date:   Fri Jun 12 22:54:26 2015 -0500

    Documentation!
    diff --git a/.semver b/.semver
    index 9f57831..ece307a 100644
    --- a/.semver
    +++ b/.semver
    @@ -2,5 +2,5 @@
     :major: 3
     :minor: 0
     :patch: 0
    -:special: ''
    +:special: beta
     :metadata: ''
    diff --git a/.travis.yml b/.travis.yml
    index 060cfcd..9c8e8c1 100644
    --- a/.travis.yml
    +++ b/.travis.yml
    @@ -1,18 +1,18 @@
    -# Travis CI configuration
    -
    -language: php
    -
    -services:
    -  - redis-server
    -
    -php:
    -  - 5.4
    -  - 5.5
    -  - 5.6
    -  - hhvm
    -
    -before_script:
    -  - curl -s http://getcomposer.org/installer | php
    -  - php composer.phar install --dev
    -
    -script: phpunit
    +# Travis CI configuration
    +
    +language: php
    +
    +services:
    +  - redis-server
    +
    +php:
    +  - 5.4
    +  - 5.5
    +  - 5.6
    +  - hhvm
    +
    +before_script:
    +  - curl -s http://getcomposer.org/installer | php
    +  - php composer.phar install --dev
    +
    +script: phpunit
    diff --git a/CHANGELOG.md b/CHANGELOG.md
    index 2c68961..6cf7351 100644
    --- a/CHANGELOG.md
    +++ b/CHANGELOG.md
    @@ -1,8 +1,10 @@
     # Change Log

    -## 3.0.0 - 12-Jun-2015
    +## 3.0.0-beta - 12-Jun-2015

     - Laravel 5.1 support (#141/#148 thanks @Keoghan, @Bouhnosaure)
    +- Removed `use_cache` option and support
    +- Use (Cocur\Slugify)[https://github.com/cocur/slugify] as default slugging method
     - Fix for `include_trashed` option not working for models that inherit the SoftDeletes trait (#136 thanks @ramirezd42)
     - Added `generateSuffix()` method so you could use different strategies other than integers for making incremental slugs (#129 thanks @EspadaV8)
     - Various scope and lookup fixes (thanks @xire28)
    diff --git a/README.md b/README.md
    index 3e3625e..73fb8fa 100644
    --- a/README.md
    +++ b/README.md
    @@ -70,13 +70,10 @@ Aftwards, run `composer update` from your command line.
     Then, update `config/app.php` by adding an entry for the service provider.

     ```php
    -       'providers' => array(
    -
    -               // ...
    -
    -               'Cviebrock\EloquentSluggable\SluggableServiceProvider',
    -
    -       );
    +'providers' => [
    +    // ...
    +    'Cviebrock\EloquentSluggable\SluggableServiceProvider',
    +];
     ```

     Finally, from the command line again, run `php artisan vendor:publish` to publish the default configuration file.
    @@ -94,10 +91,10 @@ class Post extends Model implements SluggableInterface
     {
            use SluggableTrait;

    -       protected $sluggable = array(
    +       protected $sluggable = [
                    'build_from' => 'title',
                    'save_to'    => 'slug',
    -       );
    +       ];

     }
     ```
    @@ -126,9 +123,9 @@ That's it ... your model is now "sluggable"!
     Saving a model is easy:

     ```php
    -$post = new Post(array(
    +$post = new Post([
            'title' => 'My Awesome Blog Post',
    -));
    +]);

     $post->save();
     ```
    @@ -168,7 +165,7 @@ Configuration was designed to be as flexible as possible. You can set up default
     By default, global configuration can be set in the `app/config/sluggable.php` file. If a configuration isn't set, then the package defaults from `vendor/cviebrock/eloquent-sluggable/config/sluggable.php` are used. Here is an example configuration, with all the default settings shown:

     ```php
    -return array(
    +return [
            'build_from'      => null,
            'save_to'         => 'slug',
            'max_length'      => null,
    @@ -178,8 +175,7 @@ return array(
            'include_trashed' => false,
            'on_update'       => false,
            'reserved'        => null,
    -       'use_cache'       => false,
    -);
    +];
     ```

     ### build_from
    @@ -191,12 +187,11 @@ class Person extends Eloquent implements SluggableInterface
     {
            use SluggableTrait;

    -       protected $sluggable = array(
    +       protected $sluggable = [
                    'build_from' => 'fullname',
    -       );
    +       ]

    -       public function getFullnameAttribute()
    -       {
    +       public function getFullnameAttribute() {
                    return $this->firstname . ' ' . $this->lastname;
            }
     }
    @@ -228,12 +223,12 @@ Note: If `unique` is enabled (which it is by default), and you anticipate having

     Defines the method used to turn the sluggable string into a slug. There are three possible options for this configuration:

    -1. When `method` is null (the default setting), the package uses Laravel's `Str::slug()` method to create the slug.
    +1. When `method` is null (the default setting), the package uses [Cocur/Slugify](https://github.com/cocur/slugify) to create the slug.

     2. When `method` is a callable, then that function or class method is used. The function/method should expect two parameters: the string to process, and a separator string. For example, to duplicate the default behaviour, you could do:

     ```php
    -       'method' => array('Illuminate\\Support\\Str', 'slug'),
    +       'method' => ['Illuminate\\Support\\Str', 'slug'],
     ```

     3. You can also define `method` as a closure (again, expecting two parameters):
    @@ -272,15 +267,6 @@ A boolean. If it is `false` (the default value), then slugs will not be updated

     An array of values that will never be allowed as slugs, e.g. to prevent collisions with existing routes or controller methods, etc.. This can be an array, or a closure that returns an array. Defaults to `null`: no reserved slug names.

    -### use_cache
    -
    -When checking for uniqueness, the package will query the database for any existing models with the same or similar slugs, and then see if an increment is required. This means more DB usage, and could also lead to a race condition if you are saving a lot of models at the same time.
    -
    -Turning on `use_cache` will store the last generated increment using Laravel's cache so that your app can save those database requests.
    -
    -The default value is `false`: don't use caching. If you are already using a cache system that supports the `Cache::tags()` feature (i.e. anything except database and file caches), then you should really enable this setting. Change it to a positive integer, which equals the number of minutes to store slug information in the cache.
    -
    -(If for whatever reason you want to clear out all of Sluggable's cache entries, then just run `Cache::tags('sluggable')->flush()`.)

     <a name="route-model"></a>
     ##Route-model Binding
    diff --git a/TODO.md b/TODO.md
    index 2799cd8..bfa3775 100644
    --- a/TODO.md
    +++ b/TODO.md
    @@ -1,16 +1,17 @@
     # Todos

     - [x] Write tests
    -- [ ] Better docblock and inline-commenting
    -- [ ] Make code style consistent
    +- [x] Better docblock and inline-commenting
    +- [x] Make code style consistent
     - [x] Drop `develop` branch and just have `master` and tagged releases
     - [x] Add check that model uses softDelete trait when using `with_trashed` (see issue #47)

     ## Planned changes (possibly BC-breaking) for next major version

    -- [ ] switch default slugging method from `Str::slug` to an external package/class that can handle transliteration of other languages (e.g. https://github.com/cocur/slugify)
    +- [x] switch default slugging method from `Str::slug` to an external package/class that can handle transliteration of other languages (e.g. https://github.com/cocur/slugify)
    +    - [ ] provide interface into `cocur/slugify` to allow for custom rules, etc.
     - [X] convert `findBySlug` into a scope (as suggested by @unitedworks in #40)
     - [ ] more configurable `unique` options (see issue #53)
    -- [ ] refactor, or remove, caching code (it wasn't really thought out well enough, IMO)
    +- [x] refactor, or remove, caching code (it wasn't really thought out well enough, IMO)
     - [ ] add events, e.g. `eloquent.slug.created`, `eloquent.slug.changed`, etc. (as suggested in #96 and #101)
    diff --git a/composer.json b/composer.json
    index 80517ed..b3d036e 100644
    --- a/composer.json
    +++ b/composer.json
    @@ -1,30 +1,30 @@
    -{
    -       "name": "cviebrock/eloquent-sluggable",
    -       "description": "Easy creation of slugs for your Eloquent models in Laravel",
    -       "keywords": ["laravel","eloquent","slug"],
    -       "homepage": "https://github.com/cviebrock/eloquent-sluggable",
    -       "license": "MIT",
    -       "authors": [
    -               {
    -                       "name": "Colin Viebrock",
    -                       "email": "colin@viebrock.ca"
    -               }
    -       ],
    -       "require": {
    -               "php": ">=5.4.0",
    -               "illuminate/config": "5.*",
    -               "illuminate/database": "5.*",
    -               "illuminate/support": "5.*",
    -               "predis/predis": "~1.0"
    -       },
    -       "require-dev": {
    -               "phpunit/phpunit": "4.*",
    -               "orchestra/testbench": "3.0.*"
    -       },
    -       "autoload": {
    -               "psr-4": {
    -                       "Cviebrock\\EloquentSluggable\\": "src/"
    -               }
    -       },
    -       "minimum-stability": "dev"
    -}
    +{
    +       "name": "cviebrock/eloquent-sluggable",
    +       "description": "Easy creation of slugs for your Eloquent models in Laravel",
    +       "keywords": ["laravel","eloquent","slug"],
    +       "homepage": "https://github.com/cviebrock/eloquent-sluggable",
    +       "license": "MIT",
    +       "authors": [
    +               {
    +                       "name": "Colin Viebrock",
    +                       "email": "colin@viebrock.ca"
    +               }
    +       ],
    +       "require": {
    +               "php": ">=5.4.0",
    +               "illuminate/config": "5.*",
    +               "illuminate/database": "5.*",
    +               "illuminate/support": "5.*",
    +               "cocur/slugify": "1.1.*"
    +       },
    +       "require-dev": {
    +               "phpunit/phpunit": "4.*",
    +               "orchestra/testbench": "3.0.*"
    +       },
    +       "autoload": {
    +               "psr-4": {
    +                       "Cviebrock\\EloquentSluggable\\": "src/"
    +               }
    +       },
    +       "minimum-stability": "dev"
    +}
    diff --git a/config/sluggable.php b/config/sluggable.php
    index e18b203..f598b3a 100644
    --- a/config/sluggable.php
    +++ b/config/sluggable.php
    @@ -1,7 +1,6 @@
     <?php

    -
    -return array(
    +return [

            /**
             * What attributes do we use to build the slug?
    @@ -51,7 +50,7 @@ return array(
             *
             * Otherwise, this will be treated as a callable to be used.  e.g.:
             *
    -        *              'method' => array('Str','slug'),
    +        *    'method' => array('Str','slug'),
             */
            'method' => null,

    @@ -92,36 +91,23 @@ return array(
             * Defaults to null (i.e. no reserved names).
             * Can be a static array, e.g.:
             *
    -        *              'reserved' => array('add', 'delete'),
    +        *    'reserved' => array('add', 'delete'),
             *
             * or a closure that returns an array of reserved names.
             * If using a closure, it will accept one parameter: the model itself, and should
             * return an array of reserved names, or null. e.g.
             *
    -        *              'reserved' => function( Model $model) {
    -        *                      return $model->some_method_that_returns_an_array();
    -        *              }
    +        *    'reserved' => function( Model $model) {
    +        *      return $model->some_method_that_returns_an_array();
    +        *    }
             *
             * In the case of a slug that gets generated with one of these reserved names,
             * we will do:
             *
    -        *      $slug .= $seperator + "1"
    +        *    $slug .= $seperator + "1"
             *
             * and continue from there.
             */
            'reserved' => null,

    -       /**
    -        * Whether or not to use Laravel's caching system to help generate
    -        * incremental slug.  Defaults to false.
    -        *
    -        * Set it to a positive integer to use the cache (the value is the
    -        * time to store slug increments in the cache).
    -        *
    -        * If you use this -- and we really recommend that you do, especially
    -        * if 'unique' is true -- then you must use a cache backend that
    -        * supports tags, i.e. not 'file' or 'database'.
    -        */
    -       'use_cache' => false,
    -
    -);
    +];
    diff --git a/phpunit.xml b/phpunit.xml
    index db30afb..f3932b8 100644
    --- a/phpunit.xml
    +++ b/phpunit.xml
    @@ -1,18 +1,18 @@
    -<?xml version="1.0" encoding="UTF-8"?>
    -<phpunit backupGlobals="false"
    -         backupStaticAttributes="false"
    -         bootstrap="vendor/autoload.php"
    -         colors="true"
    -         convertErrorsToExceptions="true"
    -         convertNoticesToExceptions="true"
    -         convertWarningsToExceptions="true"
    -         processIsolation="false"
    -         stopOnFailure="true"
    -         syntaxCheck="false"
    ->
    -    <testsuites>
    -        <testsuite name="Sluggable Test Suite">
    -            <directory suffix=".php">./tests/</directory>
    -        </testsuite>
    -    </testsuites>
    -</phpunit>
    \ No newline at end of file
    +<?xml version="1.0" encoding="UTF-8"?>
    +<phpunit
    +       backupGlobals="false"
    +       backupStaticAttributes="false"
    +       bootstrap="vendor/autoload.php"
    +       colors="true"
    +       convertErrorsToExceptions="true"
    +       convertNoticesToExceptions="true"
    +       convertWarningsToExceptions="true"
    +       processIsolation="false"
    +       stopOnFailure="true"
    +       syntaxCheck="false">
    +       <testsuites>
    +               <testsuite name="Sluggable Test Suite">
    +                       <directory suffix=".php">./tests/</directory>
    +               </testsuite>
    +       </testsuites>
    +</phpunit>
    diff --git a/src/SluggableInterface.php b/src/SluggableInterface.php
    index 7dd9009..9c16631 100644
    --- a/src/SluggableInterface.php
    +++ b/src/SluggableInterface.php
    @@ -1,14 +1,11 @@
    -<?php
    -
    -namespace Cviebrock\EloquentSluggable;
    +<?php namespace Cviebrock\EloquentSluggable;

     interface SluggableInterface {

            public function getSlug();

    -       public function sluggify($force=false);
    +       public function sluggify($force = false);

            public function resluggify();
    -
    -}
    \ No newline at end of file
    +}
    diff --git a/src/SluggableMigrationCreator.php b/src/SluggableMigrationCreator.php
    index 144676a..01322cc 100644
    --- a/src/SluggableMigrationCreator.php
    +++ b/src/SluggableMigrationCreator.php
    @@ -2,6 +2,7 @@

     use Illuminate\Database\Migrations\MigrationCreator;

    +
     class SluggableMigrationCreator extends MigrationCreator {

            /**
    @@ -16,34 +17,30 @@ class SluggableMigrationCreator extends MigrationCreator {
             *
             * @return string
             */
    -       public function getStubPath()
    -       {
    +       public function getStubPath() {
                    return __DIR__ . '/../stubs';
            }

            /**
             * Get the migration stub file.
             *
    -        * @param  string  $table
    -        * @param  bool    $create
    +        * @param  string $table
    +        * @param  bool $create
             * @return string
             */
    -       protected function getStub($table, $create)
    -       {
    -               return $this->files->get($this->getStubPath().'/migration.stub');
    +       protected function getStub($table, $create) {
    +               return $this->files->get($this->getStubPath() . '/migration.stub');
            }

            /**
             * Populate the place-holders in the migration stub.
             *
    -        * @param  string  $name
    -        * @param  string  $stub
    -        * @param  string  $table
    -        * @param  string  $column
    +        * @param  string $name
    +        * @param  string $stub
    +        * @param  string $table
             * @return string
             */
    -       protected function populateStub($name, $stub, $table)
    -       {
    +       protected function populateStub($name, $stub, $table) {
                    $stub = parent::populateStub($name, $stub, $table);

                    return str_replace('{{column}}', $this->column, $stub);
    @@ -52,9 +49,7 @@ class SluggableMigrationCreator extends MigrationCreator {
            /**
             * @param string $column
             */
    -       public function setColumn($column)
    -       {
    +       public function setColumn($column) {
                    $this->column = $column;
            }
    -
     }
    diff --git a/src/SluggableRouter.php b/src/SluggableRouter.php
    index 031068e..36fd52e 100644
    --- a/src/SluggableRouter.php
    +++ b/src/SluggableRouter.php
    @@ -3,50 +3,48 @@
     use Illuminate\Routing\Router;
     use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;

    -class SluggableRouter extends Router
    -{
    -    /**
    +
    +class SluggableRouter extends Router {
    +
    +       /**
             * Register a model binder for a wildcard. However, it's changed so that if the
             * model implements the SluggableInterface, we'll use a different method.
             *
    -        * @param  string  $key
    -        * @param  string  $class
    -        * @param  \Closure|null  $callback
    -        * @return void
    -        *
    +        * @param string $key
    +        * @param string $class
    +        * @param Closure|null $callback
             * @throws NotFoundHttpException
             */
    -    public function model($key, $class, Closure $callback = null)
    -    {
    -        $this->bind($key, function($value) use ($class, $callback)
    -        {
    -            if (is_null($value)) return null;
    -            // For model binders, we attempt to get the model using the findBySlugOrId
    -            // method when the model uses a SluggableInterface, or by using the find
    -            // method on the model instance. If we cannot retrieve the models we'll
    -            // throw a not found exception otherwise we will return the instance.
    -            $model = new $class;
    -            if($model instanceof SluggableInterface)
    -            {
    -                $model = $model->findBySlugOrId($value);
    -            }
    -            else
    -            {
    -                $model = $model->find($value);
    -            }
    -
    -            if ( ! is_null($model))
    -            {
    -                return $model;
    -            }
    -            // If a callback was supplied to the method we will call that to determine
    -            // what we should do when the model is not found. This just gives these
    -            // developer a little greater flexibility to decide what will happen.
    -            if ($callback instanceof Closure)
    -            {
    -                return call_user_func($callback, $value);
    -            }
    -            throw new NotFoundHttpException;
    -        });
    -    }
    +       public function model($key, $class, Closure $callback = null) {
    +               $this->bind($key, function ($value) use ($class, $callback) {
    +                       if (is_null($value)) {
    +                               return null;
    +                       }
    +
    +                       // For model binders, we attempt to get the model using the findBySlugOrId
    +                       // method when the model uses a SluggableInterface, or by using the find
    +                       // method on the model instance. If we cannot retrieve the models we'll
    +                       // throw a not found exception otherwise we will return the instance.
    +                       $model = new $class;
    +
    +                       if ($model instanceof SluggableInterface) {
    +                               $model = $model->findBySlugOrId($value);
    +                       } else {
    +                               $model = $model->find($value);
    +                       }
    +
    +                       if (!is_null($model)) {
    +                               return $model;
    +                       }
    +
    +                       // If a callback was supplied to the method we will call that to determine
    +                       // what we should do when the model is not found. This just gives these
    +                       // developer a little greater flexibility to decide what will happen.
    +                       if ($callback instanceof Closure) {
    +                               return call_user_func($callback, $value);
    +                       }
    +
    +                       throw new NotFoundHttpException;
    +               });
    +       }
     }
    diff --git a/src/SluggableServiceProvider.php b/src/SluggableServiceProvider.php
    index 639e255..866ae18 100644
    --- a/src/SluggableServiceProvider.php
    +++ b/src/SluggableServiceProvider.php
    @@ -1,8 +1,8 @@
     <?php namespace Cviebrock\EloquentSluggable;

    -
     use Illuminate\Support\ServiceProvider;

    +
     class SluggableServiceProvider extends ServiceProvider {

            /**
    @@ -17,8 +17,7 @@ class SluggableServiceProvider extends ServiceProvider {
             *
             * @return void
             */
    -       public function boot()
    -       {
    +       public function boot() {
                    $this->handleConfigs();
            }

    @@ -27,8 +26,7 @@ class SluggableServiceProvider extends ServiceProvider {
             *
             * @return void
             */
    -       public function register()
    -       {
    +       public function register() {
                    $this->registerCreator();
                    $this->registerEvents();
                    $this->registerCommands();
    @@ -37,8 +35,7 @@ class SluggableServiceProvider extends ServiceProvider {
            /**
             * Register the configuration.
             */
    -       private function handleConfigs()
    -       {
    +       private function handleConfigs() {
                    $configPath = __DIR__ . '/../config/sluggable.php';
                    $this->publishes([$configPath => config_path('sluggable.php')]);
                    $this->mergeConfigFrom($configPath, 'sluggable');
    @@ -49,10 +46,8 @@ class SluggableServiceProvider extends ServiceProvider {
             *
             * @return void
             */
    -       protected function registerCreator()
    -       {
    -               $this->app->singleton('sluggable.creator', function($app)
    -               {
    +       protected function registerCreator() {
    +               $this->app->singleton('sluggable.creator', function ($app) {
                            return new SluggableMigrationCreator($app['files']);
                    });
            }
    @@ -62,12 +57,9 @@ class SluggableServiceProvider extends ServiceProvider {
             *
             * @return void
             */
    -       public function registerEvents()
    -       {
    -               $this->app['events']->listen('eloquent.saving*', function($model)
    -               {
    -                       if ($model instanceof SluggableInterface)
    -                       {
    +       public function registerEvents() {
    +               $this->app['events']->listen('eloquent.saving*', function ($model) {
    +                       if ($model instanceof SluggableInterface) {
                                    $model->sluggify();
                            }
                    });
    @@ -78,10 +70,8 @@ class SluggableServiceProvider extends ServiceProvider {
             *
             * @return void
             */
    -       public function registerCommands()
    -       {
    -               $this->app['sluggable.table'] = $this->app->share(function($app)
    -               {
    +       public function registerCommands() {
    +               $this->app['sluggable.table'] = $this->app->share(function ($app) {
                            // Once we have the migration creator registered, we will create the command
                            // and inject the creator. The creator is responsible for the actual file
                            // creation of the migrations, and may be extended by these developers.
    @@ -100,9 +90,7 @@ class SluggableServiceProvider extends ServiceProvider {
             *
             * @return array
             */
    -       public function provides()
    -       {
    +       public function provides() {
                    return ['sluggable.creator', 'sluggable.table'];
            }
    -
     }
    diff --git a/src/SluggableTableCommand.php b/src/SluggableTableCommand.php
    index eb5bd0c..5268c21 100644
    --- a/src/SluggableTableCommand.php
    +++ b/src/SluggableTableCommand.php
    @@ -1,10 +1,10 @@
     <?php namespace Cviebrock\EloquentSluggable;

     use Illuminate\Database\Console\Migrations\BaseCommand;
    -use Illuminate\Database\Migrations\MigrationCreator;
     use Illuminate\Foundation\Composer;
     use Symfony\Component\Console\Input\InputArgument;

    +
     class SluggableTableCommand extends BaseCommand {

            /**
    @@ -22,7 +22,7 @@ class SluggableTableCommand extends BaseCommand {
            protected $description = 'Create a migration for the Sluggable database columns';

            /**
    -        * @var \Cviebrock\EloquentSluggable
    +        * @var SluggableMigrationCreator
             */
            protected $creator;

    @@ -34,12 +34,10 @@ class SluggableTableCommand extends BaseCommand {
            /**
             * Create a new migration sluggable instance.
             *
    -        * @param  \Cviebrock\EloquentSluggable  $creator
    -        * @param  \Illuminate\Foundation\Composer  $composer
    -        * @return void
    +        * @param SluggableMigrationCreator $creator
    +        * @param Composer $composer
             */
    -       public function __construct(SluggableMigrationCreator $creator, Composer $composer)
    -       {
    +       public function __construct(SluggableMigrationCreator $creator, Composer $composer) {
                    parent::__construct();

                    $this->creator = $creator;
    @@ -51,13 +49,12 @@ class SluggableTableCommand extends BaseCommand {
             *
             * @return void
             */
    -       public function fire()
    -       {
    +       public function fire() {
                    $table = $this->input->getArgument('table');

                    $column = $this->input->getArgument('column');

    -               $name = 'add_'.$table.'_'.$column.'_column';
    +               $name = 'add_' . $table . '_' . $column . '_column';

                    // Now we are ready to write the migration out to disk. Once we've written
                    // the migration out, we will dump-autoload for the entire framework to
    @@ -70,13 +67,12 @@ class SluggableTableCommand extends BaseCommand {
            /**
             * Write the migration file to disk.
             *
    -        * @param  string  $name
    -        * @param  string  $table
    -        * @param  bool    $column
    +        * @param  string $name
    +        * @param  string $table
    +        * @param  bool $column
             * @return string
             */
    -       protected function writeMigration($name, $table, $column)
    -       {
    +       protected function writeMigration($name, $table, $column) {
                    $path = $this->getMigrationPath();

                    $this->creator->setColumn($column);
    @@ -91,12 +87,10 @@ class SluggableTableCommand extends BaseCommand {
             *
             * @return array
             */
    -       protected function getArguments()
    -       {
    -               return array(
    -                       array('table',  InputArgument::REQUIRED, 'The name of your sluggable table.'),
    -                       array('column', InputArgument::OPTIONAL, 'The name of your slugged column (defaults to "slug").', 'slug'),
    -               );
    +       protected function getArguments() {
    +               return [
    +                       ['table', InputArgument::REQUIRED, 'The name of your sluggable table.'],
    +                       ['column', InputArgument::OPTIONAL, 'The name of your slugged column (defaults to "slug").', 'slug'],
    +               ];
            }
    -
     }
    diff --git a/src/SluggableTrait.php b/src/SluggableTrait.php
    index d0b8604..790ed00 100644
    --- a/src/SluggableTrait.php
    +++ b/src/SluggableTrait.php
    @@ -1,12 +1,22 @@
     <?php namespace Cviebrock\EloquentSluggable;

    +use Cocur\Slugify\Slugify;
     use Illuminate\Support\Collection;
    -use Illuminate\Support\Str;

    +
    +/**
    + * Class SluggableTrait
    + *
    + * @package Cviebrock\EloquentSluggable
    + */
     trait SluggableTrait {

    -       protected function needsSlugging()
    -       {
    +       /**
    +        * Determines whether the model needs slugging.
    +        *
    +        * @return bool
    +        */
    +       protected function needsSlugging() {
                    $config = $this->getSluggableConfig();
                    $save_to = $config['save_to'];
                    $on_update = $config['on_update'];
    @@ -19,23 +29,24 @@ trait SluggableTrait {
                            return false;
                    }

    -               return ( !$this->exists || $on_update );
    +               return (!$this->exists || $on_update);
            }

    -
    -       protected function getSlugSource()
    -       {
    +       /**
    +        * Get the source string for the slug.
    +        *
    +        * @return string
    +        */
    +       protected function getSlugSource() {
                    $config = $this->getSluggableConfig();
                    $from = $config['build_from'];

    -               if ( is_null($from) )
    -               {
    +               if (is_null($from)) {
                            return $this->__toString();
                    }

                    $source = array_map(
    -                       function($attribute)
    -                       {
    +                       function ($attribute) {
                                    return $this->{$attribute};
                            },
                            (array) $from
    @@ -44,90 +55,79 @@ trait SluggableTrait {
                    return join($source, ' ');
            }

    -
    -
    -       protected function generateSlug($source)
    -       {
    +       /**
    +        * Generate a slug from the given source string.
    +        *
    +        * @param string $source
    +        * @return string
    +        * @throws \UnexpectedValueException
    +        */
    +       protected function generateSlug($source) {
                    $config = $this->getSluggableConfig();
    -               $separator  = $config['separator'];
    -               $method     = $config['method'];
    +               $separator = $config['separator'];
    +               $method = $config['method'];
                    $max_length = $config['max_length'];

    -               if ( $method === null )
    -               {
    -                       $slug = Str::slug($source, $separator);
    -               }
    -               elseif ( is_callable($method) )
    -               {
    +               if ($method === null) {
    +                       $slug = (new Slugify)->slugify($source, $separator);
    +               } elseif (is_callable($method)) {
                            $slug = call_user_func($method, $source, $separator);
    -               }
    -               else
    -               {
    -                       throw new \UnexpectedValueException("Sluggable method is not callable or null.");
    +               } else {
    +                       throw new \UnexpectedValueException('Sluggable method is not callable or null.');
                    }

    -               if (is_string($slug) && $max_length)
    -               {
    +               if (is_string($slug) && $max_length) {
                            $slug = substr($slug, 0, $max_length);
                    }

                    return $slug;
            }

    -
    -       protected function validateSlug($slug)
    -       {
    +       /**
    +        * Checks that the given slug is not a reserved word.
    +        *
    +        * @param string $slug
    +        * @return string
    +        * @throws \UnexpectedValueException
    +        */
    +       protected function validateSlug($slug) {
                    $config = $this->getSluggableConfig();
                    $reserved = $config['reserved'];

    -               if ( $reserved === null ) return $slug;
    +               if ($reserved === null) {
    +                       return $slug;
    +               }

                    // check for reserved names
    -               if ( $reserved instanceof \Closure )
    -               {
    +               if ($reserved instanceof \Closure) {
                            $reserved = $reserved($this);
                    }

    -               if ( is_array($reserved) )
    -               {
    -                       if ( in_array($slug, $reserved) )
    -                       {
    +               if (is_array($reserved)) {
    +                       if (in_array($slug, $reserved)) {
                                    return $slug . $config['separator'] . '1';
                            }
    +
                            return $slug;
                    }

    -               throw new \UnexpectedValueException("Sluggable reserved is not null, an array, or a closure that returns null/array.");
    -
    +               throw new \UnexpectedValueException('Sluggable reserved is not null, an array, or a closure that returns null/array.');
            }

    -       protected function makeSlugUnique($slug)
    -       {
    +       /**
    +        * Checks if the slug should be unique, and makes it so if needed.
    +        *
    +        * @param string $slug
    +        * @return string
    +        */
    +       protected function makeSlugUnique($slug) {
                    $config = $this->getSluggableConfig();
    -               if (!$config['unique']) return $slug;
    -
    -               $separator  = $config['separator'];
    -               $use_cache  = $config['use_cache'];
    -               $save_to    = $config['save_to'];
    -
    -               // if using the cache, check if we have an entry already instead
    -               // of querying the database
    -               if ( $use_cache )
    -               {
    -                       $increment = \Cache::tags('sluggable')->get($slug);
    -                       if ( $increment === null )
    -                       {
    -                               \Cache::tags('sluggable')->put($slug, 0, $use_cache);
    -                       }
    -                       else
    -                       {
    -                               \Cache::tags('sluggable')->put($slug, ++$increment, $use_cache);
    -                               $slug .= $separator . $increment;
    -                       }
    +               if (!$config['unique']) {
                            return $slug;
                    }

    -               // no cache, so we need to check directly
    +               $separator = $config['separator'];
    +
                    // find all models where the slug is like the current one
                    $list = $this->getExistingSlugs($slug);

    @@ -137,30 +137,29 @@ trait SluggableTrait {
                    //      c) our slug is in the list and it's for our model
                    // ... we are okay
                    if (
    -                       count($list)===0 ||
    +                       count($list) === 0 ||
                            !in_array($slug, $list) ||
    -                       ( array_key_exists($this->getKey(), $list) && $list[$this->getKey()]===$slug )
    -               )
    -               {
    +                       (array_key_exists($this->getKey(), $list) && $list[$this->getKey()] === $slug)
    +               ) {
                            return $slug;
                    }

                    $suffix = $this->generateSuffix($slug, $list);

                    return $slug . $separator . $suffix;
    -
            }

            /**
    +        * Generate a unique suffix for the given slug (and list of existing, "similar" slugs.
    +        *
             * @param string $slug
    -        * @param array  $list
    +        * @param array $list
             *
             * @return string
             */
    -       protected function generateSuffix($slug, $list)
    -       {
    +       protected function generateSuffix($slug, $list) {
                    $config = $this->getSluggableConfig();
    -               $separator  = $config['separator'];
    +               $separator = $config['separator'];
                    $len = strlen($slug . $separator);

                    array_walk($list, function (&$value, $key) use ($len) {
    @@ -169,57 +168,77 @@ trait SluggableTrait {

                    // find the highest increment
                    rsort($list);
    +
                    return reset($list) + 1;
            }

    -       protected function getExistingSlugs($slug)
    -       {
    +       /**
    +        * Get all existing slugs that are similar to the given slug.
    +        *
    +        * @param string $slug
    +        * @return array
    +        */
    +       protected function getExistingSlugs($slug) {
                    $config = $this->getSluggableConfig();
    -               $save_to         = $config['save_to'];
    +               $save_to = $config['save_to'];
                    $include_trashed = $config['include_trashed'];

                    $instance = new static;

    -               $query = $instance->where( $save_to, 'LIKE', $slug.'%' );
    +               $query = $instance->where($save_to, 'LIKE', $slug . '%');

                    // include trashed models if required
    -               if ( $include_trashed && $this->usesSoftDeleting() )
    -               {
    +               if ($include_trashed && $this->usesSoftDeleting()) {
                            $query = $query->withTrashed();
                    }

                    // get a list of all matching slugs
                    $list = $query->lists($save_to, $this->getKeyName());

    +               // Laravel 5.0/5.1 check
                    return $list instanceof Collection ? $list->all() : $list;
            }

    -
    +       /**
    +        * Does this model use softDeleting?
    +        *
    +        * @return bool
    +        */
            protected function usesSoftDeleting() {
    -               return method_exists($this,'BootSoftDeletes');
    +               return method_exists($this, 'BootSoftDeletes');
            }

    -
    -       protected function setSlug($slug)
    -       {
    +       /**
    +        * Set the slug manually.
    +        *
    +        * @param string $slug
    +        */
    +       protected function setSlug($slug) {
                    $config = $this->getSluggableConfig();
                    $save_to = $config['save_to'];
    -               $this->setAttribute( $save_to, $slug );
    +               $this->setAttribute($save_to, $slug);
            }

    -
    -       public function getSlug()
    -       {
    +       /**
    +        * Get the current slug.
    +        *
    +        * @return mixed
    +        */
    +       public function getSlug() {
                    $config = $this->getSluggableConfig();
                    $save_to = $config['save_to'];
    -               return $this->getAttribute( $save_to );
    -       }

    +               return $this->getAttribute($save_to);
    +       }

    -       public function sluggify($force=false)
    -       {
    -               if ($force || $this->needsSlugging())
    -               {
    +       /**
    +        * Manually slug the current model.
    +        *
    +        * @param bool $force
    +        * @return $this
    +        */
    +       public function sluggify($force = false) {
    +               if ($force || $this->needsSlugging()) {
                            $source = $this->getSlugSource();
                            $slug = $this->generateSlug($source);

    @@ -232,62 +251,87 @@ trait SluggableTrait {
                    return $this;
            }

    -       public function resluggify()
    -       {
    +       /**
    +        * Force slugging of current model.
    +        *
    +        * @return SluggableTrait
    +        */
    +       public function resluggify() {
                    return $this->sluggify(true);
            }

    -       public function scopeWhereSlug($scope, $slug)
    -       {
    +       /**
    +        * Query scope for finding a model by its slug.
    +        *
    +        * @param $scope
    +        * @param $slug
    +        * @return mixed
    +        */
    +       public function scopeWhereSlug($scope, $slug) {
                    $config = $this->getSluggableConfig();
    -               return $scope->where($config['save_to'],$slug);
    +
    +               return $scope->where($config['save_to'], $slug);
            }

    -       public static function findBySlug($slug)
    -       {
    +       /**
    +        * Find a model by slug.
    +        *
    +        * @param $slug
    +        * @return Model|null.
    +        */
    +       public static function findBySlug($slug) {
                    return self::whereSlug($slug)->first();
            }

    -       public static function findBySlugOrFail($slug)
    -       {
    +       /**
    +        * Find a model by slug or fail.
    +        *
    +        * @param $slug
    +        * @return Model
    +        */
    +       public static function findBySlugOrFail($slug) {
                    return self::whereSlug($slug)->firstOrFail();
            }

    -       protected function getSluggableConfig()
    -       {
    -               $defaults = \App::make('config')->get('sluggable');
    -               if (property_exists($this, 'sluggable'))
    -               {
    -                       return array_merge($defaults, $this->sluggable);
    +       /**
    +        * Get the default configuration and merge in any model-specific overrides.
    +        *
    +        * @return array
    +        */
    +       protected function getSluggableConfig() {
    +               $defaults = app('config')->get('sluggable');
    +               if (property_exists($this, 'sluggable')) {
    +                       return array_merge($defaults, $this->sluggable);
                    }
    +
                    return $defaults;
            }

            /**
             * Simple find by Id if it's numeric or slug if not. Fail if not found.
             *
    -        * @return Model/Collection
    +        * @param $slug
    +        * @return Model|Collection
             */
    +       public static function findBySlugOrIdOrFail($slug) {
    +               if (is_numeric($slug) && $slug > 0) {
    +                       return self::findOrFail($slug);
    +               }

    -       public static function findBySlugOrIdOrFail($slug)
    -    {
    -        if(is_numeric($slug) && $slug > 0) {
    -            return self::findOrFail($slug);
    -        }
    -        return self::findBySlugOrFail($slug);
    -    }
    +               return self::findBySlugOrFail($slug);
    +       }

            /**
             * Simple find by Id if it's numeric or slug if not.
             *
    -        * @return Model/Collection
    +        * @param $slug
    +        * @return Model|Collection|null
             */
    +       public static function findBySlugOrId($slug) {
    +               if (is_numeric($slug) && $slug > 0) {
    +                       return self::find($slug);
    +               }

    -    public static function findBySlugOrId($slug)
    -    {
    -        if(is_numeric($slug) && $slug > 0) {
    -            return self::find($slug);
    -        }
    -           return self::findBySlug($slug);
    -    }
    +               return self::findBySlug($slug);
    +       }
     }
    diff --git a/tests/SluggableTest.php b/tests/SluggableTest.php
    index 2cd7524..c14fd18 100644
    --- a/tests/SluggableTest.php
    +++ b/tests/SluggableTest.php
    @@ -1,89 +1,85 @@
     <?php

    -use Orchestra\Testbench\TestCase;
     use Illuminate\Support\Str;
    +use Orchestra\Testbench\TestCase;

    +
    +/**
    + * Class SluggableTest
    + */
     class SluggableTest extends TestCase {

    -  /**
    -   * Setup the test environment.
    -   */
    -       public function setUp()
    -       {
    +       /**
    +        * Setup the test environment.
    +        */
    +       public function setUp() {
                    parent::setUp();

                    // Create an artisan object for calling migrations
                    $artisan = $this->app->make('Illuminate\Contracts\Console\Kernel');

                    // Call migrations specific to our tests, e.g. to seed the db
    -               $artisan->call('migrate', array(
    +               $artisan->call('migrate', [
                            '--database' => 'testbench',
    -                       '--path'     => '../tests/database/migrations',
    -               ));
    -
    +                       '--path' => '../tests/database/migrations',
    +               ]);
            }

    -
    -  /**
    -   * Define environment setup.
    -   *
    -   * @param  Illuminate\Foundation\Application    $app
    -   * @return void
    -   */
    -       protected function getEnvironmentSetUp($app)
    -       {
    +       /**
    +        * Define environment setup.
    +        *
    +        * @param  Illuminate\Foundation\Application $app
    +        * @return void
    +        */
    +       protected function getEnvironmentSetUp($app) {
                    // reset base path to point to our package's src directory
                    $app['path.base'] = __DIR__ . '/../src';

                    // set up database configuration
                    $app['config']->set('database.default', 'testbench');
    -               $app['config']->set('database.connections.testbench', array(
    -                               'driver'   => 'sqlite',
    -                               'database' => ':memory:',
    -                               'prefix'   => '',
    -               ));
    -
    -               // set up caching configuration
    -               $app['config']->set('cache.default', 'redis');
    -               $app['config']->set('cache.prefix', 'SluggableTest');
    +               $app['config']->set('database.connections.testbench', [
    +                       'driver' => 'sqlite',
    +                       'database' => ':memory:',
    +                       'prefix' => '',
    +               ]);
            }

    -
    -  /**
    -   * Get Sluggable package providers.
    -   *
    -   * @return array
    -   */
    -       protected function getPackageProviders($app)
    -       {
    -               return array('Cviebrock\EloquentSluggable\SluggableServiceProvider');
    +       /**
    +        * Get Sluggable package providers.
    +        *
    +        * @return array
    +        */
    +       protected function getPackageProviders($app) {
    +               return ['Cviebrock\EloquentSluggable\SluggableServiceProvider'];
            }

    -
    -       protected function makePost($title, $subtitle=null, $slug=null)
    -       {
    +       /**
    +        * Helper to create "Post" model for tests.
    +        *
    +        * @param string $title
    +        * @param string|null $subtitle
    +        * @param string|null $slug
    +        * @return Post
    +        */
    +       protected function makePost($title, $subtitle = null, $slug = null) {
                    $post = new Post;
                    $post->title = $title;
    -               if ($subtitle)
    -               {
    +               if ($subtitle) {
                            $post->subtitle = $subtitle;
                    }
    -               if ($slug)
    -               {
    +               if ($slug) {
                            $post->slug = $slug;
                    }
    +
                    return $post;
            }

    -
    -
            /**
             * Test basic slugging functionality.
             *
             * @test
             */
    -       public function testSimpleSlug()
    -       {
    +       public function testSimpleSlug() {
                    $post = $this->makePost('My First Post');
                    $post->save();
                    $this->assertEquals('my-first-post', $post->slug);
    @@ -94,8 +90,7 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testAccentedCharacters()
    -       {
    +       public function testAccentedCharacters() {
                    $post = $this->makePost('My Dinner With André & François');
                    $post->save();
                    $this->assertEquals('my-dinner-with-andre-francois', $post->slug);
    @@ -107,8 +102,7 @@ class SluggableTest extends TestCase {
             * @param  Post $post
             * @test
             */
    -       public function testRenameSlugWithoutUpdate()
    -       {
    +       public function testRenameSlugWithoutUpdate() {
                    $post = $this->makePost('My First Post');
                    $post->save();
                    $post->title = 'A New Title';
    @@ -122,12 +116,11 @@ class SluggableTest extends TestCase {
             * @param  Post $post
             * @test
             */
    -       public function testRenameSlugWithUpdate()
    -       {
    +       public function testRenameSlugWithUpdate() {
                    $post = $this->makePost('My First Post');
    -               $post->setSlugConfig(array(
    +               $post->setSlugConfig([
                            'on_update' => true
    -               ));
    +               ]);
                    $post->save();
                    $post->title = 'A New Title';
                    $post->save();
    @@ -139,47 +132,14 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testUnique()
    -       {
    -               for ($i=0; $i < 20; $i++)
    -               {
    +       public function testUnique() {
    +               for ($i = 0; $i < 20; $i++) {
                            $post = $this->makePost('A post title');
                            $post->save();
    -                       if ($i==0)
    -                       {
    +                       if ($i == 0) {
                                    $this->assertEquals('a-post-title', $post->slug);
    -                       }
    -                       else
    -                       {
    -                               $this->assertEquals('a-post-title-'.$i, $post->slug);
    -                       }
    -               }
    -       }
    -
    -       /**
    -        * Test uniqueness of generated slugs using caching
    -        *
    -        * @test
    -        */
    -       public function testUniqueWithCache()
    -       {
    -               // Manually flush the cache for tests
    -               \Cache::tags('sluggable')->flush();
    -
    -               for ($i=0; $i < 20; $i++)
    -               {
    -                       $post = $this->makePost('A post title');
    -                       $post->setSlugConfig(array(
    -                               'use_cache' => 10,
    -                       ));
    -                       $post->save();
    -                       if ($i==0)
    -                       {
    -                               $this->assertEquals('a-post-title', $post->slug);
    -                       }
    -                       else
    -                       {
    -                               $this->assertEquals('a-post-title-'.$i, $post->slug);
    +                       } else {
    +                               $this->assertEquals('a-post-title-' . $i, $post->slug);
                            }
                    }
            }
    @@ -189,12 +149,11 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testMultipleSource()
    -       {
    -               $post =$this->makePost('A Post Title','A Subtitle');
    -               $post->setSlugConfig(array(
    -                       'build_from' => array('title','subtitle')
    -               ));
    +       public function testMultipleSource() {
    +               $post = $this->makePost('A Post Title', 'A Subtitle');
    +               $post->setSlugConfig([
    +                       'build_from' => ['title', 'subtitle']
    +               ]);
                    $post->save();
                    $this->assertEquals('a-post-title-a-subtitle', $post->slug);
            }
    @@ -204,51 +163,47 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testCustomMethod()
    -       {
    -               $post =$this->makePost('A Post Title','A Subtitle');
    -               $post->setSlugConfig(array(
    -                       'method' => function($string, $separator)
    -                       {
    -                               return strrev( Str::slug($string,$separator) );
    +       public function testCustomMethod() {
    +               $post = $this->makePost('A Post Title', 'A Subtitle');
    +               $post->setSlugConfig([
    +                       'method' => function ($string, $separator) {
    +                               return strrev(Str::slug($string, $separator));
                            }
    -               ));
    +               ]);
                    $post->save();
                    $this->assertEquals('eltit-tsop-a', $post->slug);
            }

    -    /**
    -     * Test building a slug using a custom suffix.
    -     *
    -     * @test
    -     */
    -    public function testCustomSuffix()
    -    {
    -        for ($i = 0; $i < 20; $i++) {
    -            $post = new PostSuffix;
    -            $post->title = 'A Post Title';
    -            $post->subtitle = 'A Subtitle';
    -            $post->save();
    -
    -            if ($i === 0) {
    -                $this->assertEquals('a-post-title', $post->slug);
    -            } else {
    -                $this->assertEquals('a-post-title-' . chr($i + 96), $post->slug);
    -            }
    -        }
    -    }
    +       /**
    +        * Test building a slug using a custom suffix.
    +        *
    +        * @test
    +        */
    +       public function testCustomSuffix() {
    +               for ($i = 0; $i < 20; $i++) {
    +                       $post = new PostSuffix;
    +                       $post->title = 'A Post Title';
    +                       $post->subtitle = 'A Subtitle';
    +                       $post->save();
    +
    +                       if ($i === 0) {
    +                               $this->assertEquals('a-post-title', $post->slug);
    +                       } else {
    +                               $this->assertEquals('a-post-title-' . chr($i + 96), $post->slug);
    +                       }
    +               }
    +       }

            /**
             * Test building a slug using the __toString method
             *
             * @test
             */
    -       public function testToStringMethod()
    -       {
    +       public function testToStringMethod() {
                    $post = $this->makePost('A Post Title');
    -               $post->setSlugConfig(array(
    +               $post->setSlugConfig([
                            'build_from' => null
    -               ));
    +               ]);
                    $post->save();
                    $this->assertEquals('a-post-title', $post->slug);
            }
    @@ -258,8 +213,7 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testUniqueAfterDelete()
    -       {
    +       public function testUniqueAfterDelete() {
                    $post1 = $this->makePost('A post title');
                    $post1->save();
                    $this->assertEquals('a-post-title', $post1->slug);
    @@ -280,12 +234,11 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testCustomSeparator()
    -       {
    +       public function testCustomSeparator() {
                    $post = $this->makePost('A post title');
    -               $post->setSlugConfig(array(
    +               $post->setSlugConfig([
                            'separator' => '.'
    -               ));
    +               ]);
                    $post->save();
                    $this->assertEquals('a.post.title', $post->slug);
            }
    @@ -295,12 +248,11 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testReservedWord()
    -       {
    +       public function testReservedWord() {
                    $post = $this->makePost('Add');
    -               $post->setSlugConfig(array(
    -                       'reserved' => array('add')
    -               ));
    +               $post->setSlugConfig([
    +                       'reserved' => ['add']
    +               ]);
                    $post->save();
                    $this->assertEquals('add-1', $post->slug);
            }
    @@ -310,12 +262,11 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testIssue5()
    -       {
    +       public function testIssue5() {
                    $post = $this->makePost('My first post');
    -               $post->setSlugConfig(array(
    +               $post->setSlugConfig([
                            'on_update' => true
    -               ));
    +               ]);
                    $post->save();
                    $this->assertEquals('my-first-post', $post->slug);

    @@ -333,25 +284,24 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testSoftDeletesWithoutTrashed()
    -       {
    -               $post1 = new PostSoft(array(
    +       public function testSoftDeletesWithoutTrashed() {
    +               $post1 = new PostSoft([
                            'title' => 'A Post Title'
    -               ));
    -               $post1->setSlugConfig(array(
    +               ]);
    +               $post1->setSlugConfig([
                            'include_trashed' => false
    -               ));
    +               ]);
                    $post1->save();
                    $this->assertEquals('a-post-title', $post1->slug);

                    $post1->delete();

    -               $post2 = new PostSoft(array(
    +               $post2 = new PostSoft([
                            'title' => 'A Post Title'
    -               ));
    -               $post2->setSlugConfig(array(
    +               ]);
    +               $post2->setSlugConfig([
                            'include_trashed' => false
    -               ));
    +               ]);
                    $post2->save();
                    $this->assertEquals('a-post-title', $post2->slug);
            }
    @@ -361,25 +311,24 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testSoftDeletesWithTrashed()
    -       {
    -               $post1 = new PostSoft(array(
    +       public function testSoftDeletesWithTrashed() {
    +               $post1 = new PostSoft([
                            'title' => 'A Post Title'
    -               ));
    -               $post1->setSlugConfig(array(
    +               ]);
    +               $post1->setSlugConfig([
                            'include_trashed' => true
    -               ));
    +               ]);
                    $post1->save();
                    $this->assertEquals('a-post-title', $post1->slug);

                    $post1->delete();

    -               $post2 = new PostSoft(array(
    +               $post2 = new PostSoft([
                            'title' => 'A Post Title'
    -               ));
    -               $post2->setSlugConfig(array(
    +               ]);
    +               $post2->setSlugConfig([
                            'include_trashed' => true
    -               ));
    +               ]);
                    $post2->save();
                    $this->assertEquals('a-post-title-1', $post2->slug);
            }
    @@ -389,16 +338,15 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testIssue16()
    -       {
    +       public function testIssue16() {
                    $post = $this->makePost('My first post');
                    $post->save();
                    $this->assertEquals('my-first-post', $post->slug);

    -               $post->setSlugConfig(array(
    -                       'unique'    => true,
    +               $post->setSlugConfig([
    +                       'unique' => true,
                            'on_update' => true,
    -               ));
    +               ]);
                    $post->dummy = 'Dummy data';
                    $post->save();
                    $this->assertEquals('my-first-post', $post->slug);
    @@ -409,8 +357,7 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testIssue20()
    -       {
    +       public function testIssue20() {
                    $post1 = $this->makePost('My first post');
                    $post1->save();
                    $this->assertEquals('my-first-post', $post1->slug);
    @@ -425,8 +372,7 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testFindBySlug()
    -       {
    +       public function testFindBySlug() {
                    $post1 = $this->makePost('My first post');
                    $post1->save();

    @@ -446,8 +392,7 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testFindBySlugOrFail()
    -       {
    +       public function testFindBySlugOrFail() {
                    $post1 = $this->makePost('My first post');
                    $post1->save();

    @@ -460,7 +405,7 @@ class SluggableTest extends TestCase {
                    $post = Post::findBySlugOrFail('my-second-post');
                    $this->assertEquals($post2->id, $post->id);

    -               try{
    +               try {
                            Post::findBySlugOrFail('my-fourth-post');
                            $this->fail('Not found exception not raised');
                    } catch (Exception $e) {
    @@ -473,11 +418,10 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testNonSluggableModels()
    -       {
    -               $post = new PostNotSluggable(array(
    +       public function testNonSluggableModels() {
    +               $post = new PostNotSluggable([
                            'title' => 'My First Post'
    -               ));
    +               ]);
                    $post->save();
                    $this->assertEquals(null, $post->slug);
            }
    @@ -487,12 +431,11 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testMaxLength()
    -       {
    +       public function testMaxLength() {
                    $post = $this->makePost('A post with a really long title');
    -               $post->setSlugConfig(array(
    +               $post->setSlugConfig([
                            'max_length' => 10,
    -               ));
    +               ]);
                    $post->save();
                    $this->assertEquals('a-post-wit', $post->slug);
            }
    @@ -502,22 +445,17 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testMaxLengthWithIncrements()
    -       {
    -               for ($i=0; $i < 20; $i++)
    -               {
    +       public function testMaxLengthWithIncrements() {
    +               for ($i = 0; $i < 20; $i++) {
                            $post = $this->makePost('A post with a really long title');
    -                       $post->setSlugConfig(array(
    +                       $post->setSlugConfig([
                                    'max_length' => 10,
    -                       ));
    +                       ]);
                            $post->save();
    -                       if ($i==0)
    -                       {
    +                       if ($i == 0) {
                                    $this->assertEquals('a-post-wit', $post->slug);
    -                       }
    -                       elseif ($i<10)
    -                       {
    -                               $this->assertEquals('a-post-wit-'.$i, $post->slug);
    +                       } elseif ($i < 10) {
    +                               $this->assertEquals('a-post-wit-' . $i, $post->slug);
                            }
                    }
            }
    @@ -527,8 +465,7 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testDoesNotNeedSluggingWhenSlugIsSet()
    -       {
    +       public function testDoesNotNeedSluggingWhenSlugIsSet() {
                    $post = $this->makePost('My first post', null, 'custom-slug');
                    $post->save();
                    $this->assertEquals('custom-slug', $post->slug);
    @@ -539,12 +476,11 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testDoesNotNeedSluggingWithUpdateWhenSlugIsSet()
    -       {
    +       public function testDoesNotNeedSluggingWithUpdateWhenSlugIsSet() {
                    $post = $this->makePost('My first post', null, 'custom-slug');
    -               $post->setSlugConfig(array(
    +               $post->setSlugConfig([
                            'on_update' => true,
    -               ));
    +               ]);
                    $this->assertEquals('custom-slug', $post->slug);

                    $post->title = 'A New Title';
    @@ -555,7 +491,6 @@ class SluggableTest extends TestCase {
                    $post->slug = 'new-custom-slug';
                    $post->save();
                    $this->assertEquals('new-custom-slug', $post->slug);
    -
            }

            /**
    @@ -563,25 +498,23 @@ class SluggableTest extends TestCase {
             *
             * @test
             */
    -       public function testSoftDeletesWithNonSoftDeleteModel()
    -       {
    -               $post1 = new Post(array(
    +       public function testSoftDeletesWithNonSoftDeleteModel() {
    +               $post1 = new Post([
                            'title' => 'A Post Title'
    -               ));
    -               $post1->setSlugConfig(array(
    +               ]);
    +               $post1->setSlugConfig([
                            'include_trashed' => true
    -               ));
    +               ]);
                    $post1->save();
                    $this->assertEquals('a-post-title', $post1->slug);
            }

    -    /**
    +       /**
             * Test findBySlug() scope method
             *
             * @test
             */
    -       public function testFindBySlugOrId()
    -       {
    +       public function testFindBySlugOrId() {
                    $post1 = $this->makePost('My first post');
                    $post1->save();

    @@ -595,18 +528,17 @@ class SluggableTest extends TestCase {

                    $this->assertEquals($post2->id, $post->id);

    -        $post = Post::findBySlugOrId(3);
    +               $post = Post::findBySlugOrId(3);

                    $this->assertEquals($post3->id, $post->id);
            }

    -    /**
    +       /**
             * Test findBySlugOrFail() scope method
             *
             * @test
             */
    -       public function testFindBySlugOrIdOrFail()
    -       {
    +       public function testFindBySlugOrIdOrFail() {
                    $post1 = $this->makePost('My first post');
                    $post1->save();

    @@ -619,10 +551,10 @@ class SluggableTest extends TestCase {
                    $post = Post::findBySlugOrIdOrFail('my-second-post');
                    $this->assertEquals($post2->id, $post->id);

    -        $post = Post::findBySlugOrIdOrFail(3);
    +               $post = Post::findBySlugOrIdOrFail(3);
                    $this->assertEquals($post3->id, $post->id);

    -               try{
    +               try {
                            Post::findBySlugOrFail('my-fourth-post');
                            $this->fail('Not found exception not raised');
                    } catch (Exception $e) {
    @@ -630,31 +562,28 @@ class SluggableTest extends TestCase {
                    }
            }

    -    /**
    -     * Test findBySlug returns null when no record found
    -     *
    -     * @test
    -     */
    -    public function testFindBySlugReturnsNullForNoRecord()
    -    {
    -        $this->assertNull(Post::findBySlug('not a real record'));
    -    }
    -
    -    /**
    -     * Test Non static call for findBySlug is working
    -     *
    -     * @test
    -     */
    +       /**
    +        * Test findBySlug returns null when no record found
    +        *
    +        * @test
    +        */
    +       public function testFindBySlugReturnsNullForNoRecord() {
    +               $this->assertNull(Post::findBySlug('not a real record'));
    +       }

    -    public function testNonStaticCallOfFindBySlug()
    -    {
    -        $post1 = $this->makePost('My first post');
    -        $post1->save();
    +       /**
    +        * Test Non static call for findBySlug is working
    +        *
    +        * @test
    +        */

    -        $post = Post::first();
    -        $resultId = $post->findBySlug('my-first-post')->id;
    +       public function testNonStaticCallOfFindBySlug() {
    +               $post1 = $this->makePost('My first post');
    +               $post1->save();

    -        $this->assertEquals($post1->id, $resultId);
    -    }
    +               $post = Post::first();
    +               $resultId = $post->findBySlug('my-first-post')->id;

    +               $this->assertEquals($post1->id, $resultId);
    +       }
     }
    diff --git a/tests/config/cache.php b/tests/config/cache.php
    deleted file mode 100644
    index 0232481..0000000
    --- a/tests/config/cache.php
    +++ /dev/null
    @@ -1,9 +0,0 @@
    -<?php
    -
    -return array(
    -
    -       'driver' => 'redis',
    -
    -       'prefix' => 'SluggableTest',
    -
    -);
    diff --git a/tests/config/database.php b/tests/config/database.php
    index 63d9933..c798988 100644
    --- a/tests/config/database.php
    +++ b/tests/config/database.php
    @@ -4,8 +4,8 @@
      * Test against in-memory SQLite database
      */

    -return array(
    -  'driver'    => 'sqlite',
    -  'database'  => ':memory:',
    -  'prefix'    => ''
    -);
    +return [
    +       'driver' => 'sqlite',
    +       'database' => ':memory:',
    +       'prefix' => ''
    +];
    diff --git a/tests/database/migrations/2013_11_04_163552_posts.php b/tests/database/migrations/2013_11_04_163552_posts.php
    index 484bb8e..1586385 100644
    --- a/tests/database/migrations/2013_11_04_163552_posts.php
    +++ b/tests/database/migrations/2013_11_04_163552_posts.php
    @@ -1,7 +1,12 @@
     <?php

     use Illuminate\Database\Migrations\Migration;
    +use Illuminate\Database\Schema\Blueprint;

    +
    +/**
    + * Class Posts
    + */
     class Posts extends Migration {

            /**
    @@ -12,7 +17,7 @@ class Posts extends Migration {
            public function up()
            {

    -               Schema::create('posts', function($table)
    +               Schema::create('posts', function(Blueprint $table)
                    {
                            $table->increments('id');
                            $table->string('title');
    diff --git a/tests/models/Post.php b/tests/models/Post.php
    index c91d772..8654123 100644
    --- a/tests/models/Post.php
    +++ b/tests/models/Post.php
    @@ -1,43 +1,65 @@
     <?php

    -use Illuminate\Database\Eloquent\Model;
     use Cviebrock\EloquentSluggable\SluggableInterface;
     use Cviebrock\EloquentSluggable\SluggableTrait;
    +use Illuminate\Database\Eloquent\Model;

    +/**
    + * Class Post
    + */
     class Post extends Model implements SluggableInterface {

            use SluggableTrait;

    -  protected $table = 'posts';
    -
    -  public $timestamps = false;
    +       /**
    +        * The table associated with the model.
    +        *
    +        * @var string
    +        */
    +       protected $table = 'posts';

    -       protected $fillable = array('title','subtitle');
    +       /**
    +        * Indicates if the model should be timestamped.
    +        *
    +        * @var bool
    +        */
    +       public $timestamps = false;

    -       protected $sluggable = array(
    -               'build_from'      => 'title',
    -               'save_to'         => 'slug',
    -       );
    +       /**
    +        * The attributes that are mass assignable.
    +        *
    +        * @var array
    +        */
    +       protected $fillable = ['title', 'subtitle'];

    +       /**
    +        * Sluggable configuration.
    +        *
    +        * @var array
    +        */
    +       protected $sluggable = [
    +               'build_from' => 'title',
    +               'save_to' => 'slug',
    +       ];

            /**
             * Helper to set slug options for tests.
             *
             * @param array $array Array of new slug options
             */
    -       public function setSlugConfig($array)
    -       {
    -               foreach($array as $key=>$value)
    -               {
    +       public function setSlugConfig($array) {
    +               foreach ($array as $key => $value) {
                            $this->sluggable[$key] = $value;
                    }
            }

    -
    -       public function __toString()
    -       {
    +       /**
    +        * Convert the model to its string representation.
    +        *
    +        * @return string
    +        */
    +       public function __toString() {
                    return $this->title;
            }
    -
    -}
    \ No newline at end of file
    +}
    diff --git a/tests/models/PostNotSluggable.php b/tests/models/PostNotSluggable.php
    index 0d87f7f..e747edf 100644
    --- a/tests/models/PostNotSluggable.php
    +++ b/tests/models/PostNotSluggable.php
    @@ -3,12 +3,32 @@
     use Illuminate\Database\Eloquent\Model;

    +/**
    + * Class PostNotSluggable
    + *
    + * A test model that doesn't use the Sluggable package.
    + */
     class PostNotSluggable extends Model {

    -  protected $table = 'posts';

    -  public $timestamps = false;
    +       /**
    +        * The table associated with the model.
    +        *
    +        * @var string
    +        */
    +       protected $table = 'posts';

    -       protected $fillable = array('title','subtitle');
    +       /**
    +        * Indicates if the model should be timestamped.
    +        *
    +        * @var bool
    +        */
    +       public $timestamps = false;

    -}
    \ No newline at end of file
    +       /**
    +        * The attributes that are mass assignable.
    +        *
    +        * @var array
    +        */
    +       protected $fillable = ['title', 'subtitle'];
    +}
    diff --git a/tests/models/PostSoft.php b/tests/models/PostSoft.php
    index ab05e91..cb033a3 100644
    --- a/tests/models/PostSoft.php
    +++ b/tests/models/PostSoft.php
    @@ -2,6 +2,13 @@

     use Illuminate\Database\Eloquent\SoftDeletes;

    +
    +/**
    + * Class PostSoft
    + *
    + * A test model that uses the Sluggable package and uses Laravel's SoftDeleting trait.
    + */
     class PostSoft extends Post {
    -               use SoftDeletes;
    +
    +       use SoftDeletes;
     }
    diff --git a/tests/models/PostSuffix.php b/tests/models/PostSuffix.php
    index fb2b505..bc68520 100644
    --- a/tests/models/PostSuffix.php
    +++ b/tests/models/PostSuffix.php
    @@ -1,22 +1,32 @@
     <?php

    -use Illuminate\Database\Eloquent\SoftDeletes;
    -
    +/**
    + * Class PostSuffix
    + *
    + * A test model that uses a custom suffix generation method.
    + */
     class PostSuffix extends Post {
    -    protected $sluggable = array(
    -        'build_from'    => 'title',
    -        'save_to'       => 'slug',
    -    );

    -    /**
    -     * @param string $slug
    -     * @param array  $list
    -     *
    -     * @return string
    -     */
    -    protected function generateSuffix($slug, $list)
    -    {
    -        $size = count($list);
    -        return chr($size + 96);
    -    }
    +       /**
    +        * Sluggable configuration.
    +        *
    +        * @var array
    +        */
    +       protected $sluggable = [
    +               'build_from' => 'title',
    +               'save_to' => 'slug',
    +       ];
    +
    +       /**
    +        * Custom suffix generator.
    +        *
    +        * @param string $slug
    +        * @param array $list
    +        * @return string
    +        */
    +       protected function generateSuffix($slug, $list) {
    +               $size = count($list);
    +
    +               return chr($size + 96);
    +       }
     }