commit e1370be7a0a86dd52241c2bf37105654d7e4a61e
Author: Beau Simensen <beau@dflydev.com>
Date:   Thu Jan 26 19:53:32 2012 -0800

    Continued refactoring of install() method, mainly by way of adding Composite Repository

     * Rewritten `install()` method now takes a repository instead of a list of packages (per @nadermen)
     * Added Composite Repository
     * Added tests for Composite Repository
     * Removed "local repository" concept from Platform Repository
     * Removed some `use` statements for Platform Repository where it was not actually being used