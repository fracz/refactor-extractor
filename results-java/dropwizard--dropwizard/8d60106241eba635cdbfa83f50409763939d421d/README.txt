commit 8d60106241eba635cdbfa83f50409763939d421d
Author: Jochen Schalanda <jochen@schalanda.name>
Date:   Tue Nov 3 10:15:44 2015 +0100

    Deprecate PooledDataSourceFactory#getHealthCheckValidation{Query,Timeout}

    The PooledDataSourceFactory#getHealthCheckValidation{Query,Timeout} methods were introduced while
    refactoring DataSourceFactory to allow custom DB connection pools (PR #1030) but broke the serialization
    of the default configuration for DataSourceFactory. By deprecating these methods and pulling the
    original DataSourceFactory#getValidation{Query,Timeout} methods into PooledDataSourceFactory, the
    original behaviour was restored.

    Fixes #1321, refs #1030

    (cherry picked from commit 84c7463ba853e62a38576f706c3b81cd08a0ad66)