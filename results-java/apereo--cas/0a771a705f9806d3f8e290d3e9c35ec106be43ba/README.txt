commit 0a771a705f9806d3f8e290d3e9c35ec106be43ba
Author: Marvin S. Addison <marvin.addison@gmail.com>
Date:   Tue Jul 17 11:18:30 2012 -0400

    CAS-1140 Additional monitors for vital CAS components.

    New monitors:
     - ContextSourceMonitor (monitors LdapContextSource)
     - DataSourceMonitor (generic data source/pooled data source monitor)
     - EhCacheMonitor
     - MemcachedMonitor

    Minor refactoring of existing monitors to conform convention of required versus optional parameters.