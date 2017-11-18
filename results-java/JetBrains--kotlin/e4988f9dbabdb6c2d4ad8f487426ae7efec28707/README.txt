commit e4988f9dbabdb6c2d4ad8f487426ae7efec28707
Author: Pavel V. Talanov <talanov.pavel@gmail.com>
Date:   Tue Jul 23 21:38:42 2013 +0400

    Use VirtualFileFinder in JavaClassResolver

    HACK: determine whether fqname is a name for class contained in class or namespace based on virtual file name
    This commit introduces code duplication which is hard to avoid without major refactoring