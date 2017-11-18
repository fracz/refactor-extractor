commit 9f46e01c8867f1d7ab00294fcd3afd23b6f48b4c
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Tue Dec 13 17:47:27 2016 +0300

    PY-21339: Use AppConfig for INSTALLED_APPS for Django 1.9+

     * AppConfig added
     * #getTopLevelClasses() marked @NotNull since the only implementation is @NotNull
     * QualifiedNameResolverImpl verbosity improved