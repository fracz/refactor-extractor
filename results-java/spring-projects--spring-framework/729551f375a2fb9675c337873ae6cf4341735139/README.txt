commit 729551f375a2fb9675c337873ae6cf4341735139
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Tue Nov 29 13:27:27 2016 -0500

    Use full package names in ReactiveAdapterRegistry

    The recent refactoring lead to
    java.lang.NoClassDefFoundError: io/reactivex/Completable

    where only RxJava 1 is in the classpath. Most likely due to the
    lack of prefix in Completable::complete with rx package to avoid
    the RxJava 2 reference.