commit 50c2a819a0c5ae50571d36a3e2dec4e8267fd13b
Author: Morris Jobke <hey@morrisjobke.de>
Date:   Thu Nov 27 16:40:12 2014 +0100

    Extract  interaction with config.php into SystemConfig

    * introduce SystemConfig to avoid DI circle (used by database connection which is itself needed by AllConfig that itself contains the methods to access the config.php which then would need the database connection - did you get it? ;))
    * use DI container and use that method in legacy code paths (for easier refactoring later)
    * create and use getSystemConfig instead of query() in DI container