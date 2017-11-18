commit fcb46f1421fcafe5c0d1d87549254fac99d355c9
Author: Bas Leijdekkers <basleijdekkers@gmail.com>
Date:   Fri Mar 10 12:18:42 2006 +0300

    The grand refactoring:
     the two buildErrorString methods in BaseInspectionVisitor replaced by one abstract vararg method.
    Changes affect every inspection. Much code could be simplified as a result.