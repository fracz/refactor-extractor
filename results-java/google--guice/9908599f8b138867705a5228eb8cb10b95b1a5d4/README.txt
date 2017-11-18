commit 9908599f8b138867705a5228eb8cb10b95b1a5d4
Author: lukes <lukes@google.com>
Date:   Fri Sep 16 15:12:13 2016 -0700

    Speed up ProviderMethodsModule.getProviderMethods.

    Test profiling at google shows this method taking large amounts of time.  While the profile results do not provide very detailed data, from examining collected stack traces we can see that it is likely due to calculating the generic signatures of all the methods.  To improve this I have

    * short circuited scanning for overrides for any type >= the super-most type that declared a provider method.  This will prevent us from calculating signatures for AbstractModule
    * don't calculate any signatures for classes that don't define provider methods
    * micro optimizations to avoid allocating data structures we likely don't need. since many modules don't define any provider methods
    * make the common ModuleAnnotatedMethodScanner cache the annotations set to
      avoid generating a bunch of garbage

    I don't have a great way to validate that this actually improves the
    performance problem reported, since the data gathering methodology is pretty
    ad-doc.  But this should reduce the overall amount of work we are doing.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=133433192