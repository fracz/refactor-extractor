commit 1007ced6ceb318e090b756ebe5494d7208c8c385
Author: mike <none@none>
Date:   Mon Aug 31 16:12:04 2009 -0700

    reorganize to allow building from maven2

    --HG--
    rename : src/roboguice/activity/GuiceActivity.java => src/main/java/roboguice/activity/GuiceActivity.java
    rename : src/roboguice/activity/GuiceExpandableListActivity.java => src/main/java/roboguice/activity/GuiceExpandableListActivity.java
    rename : src/roboguice/activity/GuiceListActivity.java => src/main/java/roboguice/activity/GuiceListActivity.java
    rename : src/roboguice/activity/GuiceMapActivity.java => src/main/java/roboguice/activity/GuiceMapActivity.java
    rename : src/roboguice/inject/ActivityScope.java => src/main/java/roboguice/inject/ActivityScope.java
    rename : src/roboguice/inject/ActivityScoped.java => src/main/java/roboguice/inject/ActivityScoped.java
    rename : src/roboguice/inject/ContentResolverProvider.java => src/main/java/roboguice/inject/ContentResolverProvider.java
    rename : src/roboguice/inject/DefaultBoolean.java => src/main/java/roboguice/inject/DefaultBoolean.java
    rename : src/roboguice/inject/DefaultInteger.java => src/main/java/roboguice/inject/DefaultInteger.java
    rename : src/roboguice/inject/DefaultString.java => src/main/java/roboguice/inject/DefaultString.java
    rename : src/roboguice/inject/ExtrasListener.java => src/main/java/roboguice/inject/ExtrasListener.java
    rename : src/roboguice/inject/InjectExtra.java => src/main/java/roboguice/inject/InjectExtra.java
    rename : src/roboguice/inject/InjectResource.java => src/main/java/roboguice/inject/InjectResource.java
    rename : src/roboguice/inject/ResourceListener.java => src/main/java/roboguice/inject/ResourceListener.java
    rename : src/roboguice/inject/ResourcesProvider.java => src/main/java/roboguice/inject/ResourcesProvider.java
    rename : src/roboguice/inject/SharedPreferencesProvider.java => src/main/java/roboguice/inject/SharedPreferencesProvider.java
    rename : src/roboguice/inject/StringResourceProvider.java => src/main/java/roboguice/inject/StringResourceProvider.java
    rename : src/roboguice/inject/ViewResourceProvider.java => src/main/java/roboguice/inject/ViewResourceProvider.java