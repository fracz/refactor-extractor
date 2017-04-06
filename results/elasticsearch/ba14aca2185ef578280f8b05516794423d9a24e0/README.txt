commit ba14aca2185ef578280f8b05516794423d9a24e0
Author: Jason Tedor <jason@tedor.me>
Date:   Sat May 21 11:02:41 2016 -0400

    Refactor property placeholder use of env. vars

    This commit is a slight refactoring of the use of environment variables
    in replacing property placeholders. In commit
    115f983827c0c29652bd444c072b658b76651317 the constructor for
    Settings.Builder was made package visible to provide a hook for tests to
    mock obtaining environment variables. But we do not need to go that far
    and can instead provide a small hook for this for tests without opening
    up the constructor. Thus, in this commit we refactor
    Settings.Builder#replacePropertyPlaceholders to a package-visible method
    that accepts a function providing environment variables by names. The
    public-visible method just delegates to this method passing in
    System::getenv and tests can use the package-visible method to mock the
    behavior they need without relying on external environment variables.