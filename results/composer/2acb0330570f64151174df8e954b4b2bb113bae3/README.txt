commit 2acb0330570f64151174df8e954b4b2bb113bae3
Author: Till Klampaeckel <till@php.net>
Date:   Fri Aug 24 10:57:38 2012 +0200

    Initial feature-dist

     * extends BaseDumper, implements interface
     * put $keys into BaseDumper

     * WIP WIP WIP WIP
     * BaseDumper for utilities
     * interface to enforce 'dump()'
     * feature:
       * supports git
       * supports zip output
       * basic test to cover feature

     * add @todo for later
     * add vendor namespace to package name

     * add extension to getFilename() so we don't need to switch in there (HT, @naderman)

     * add extension (obviously 'zip' in ZipDumper)

     * create archive in destination dir (provided by __construct())

     * condensed ZipDumper
     * moved code to BaseDumper (hopefully easier re-use)

     * use ProcessExecutor from BaseDumper

     * fix assignments in __construct()
     * allow injection of ProcessExecutor

     * fix parameters

     * fix regex

     * write in 'system temp dir'
     * update test case (oh look, a duplicate regex)

     * move working directory related to BaseDumper

     * add quotes

     * place holder for these methods

     * use PharData to create zip/tar when necessary

     * add placeholder calls
     * add call to package() using PharData

     * finish downloadHg(), downloadSvn()

     * put to use

     * make BaseDumper abstract (to force extension)
     * make BaseDumper implement Interface (makes for less code in the implementation)

    new functionality for dumping as .tar.gz

    tar instead of tar.gz, new abstract dumpertest class

    creates a local git repo instead of fetching a remote one

    more oo-ish version of it

    no constructor

     * refactor tests to be less linux-specific (used Composer\Util to wrap calls)

     * make filename only the version

     * various cs fixes (idention, tabs/spaces, doc blocks, etc.)
     * fixed a typo'd exception name

     * refactored downloading:
       * removed download*() methods
       * added dep on Composer\Factory to setup a DownloadManager instance

     * update CS with feedback from @stof

     * ArrayDumper doesn't extend BaseDumper anymore (hence no conflict on the interface)
     * move keys from BaseDumper back to ArrayDumper
     * interface now declares dump() to always return void

    Apparently I had to update the lock.

    CS fixes (tabs for spaces)
    Bugfix: sprintf() was missing.

    Fix docblock for @stof. ;)

    Pull in lock from master.

    Update lock one more time (hope it still merges).

    whitespace

    Revert ArrayDumper static keys