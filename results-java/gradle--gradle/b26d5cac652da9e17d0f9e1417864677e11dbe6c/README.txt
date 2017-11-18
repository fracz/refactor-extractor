commit b26d5cac652da9e17d0f9e1417864677e11dbe6c
Author: adammurdoch <a@rubygrapefruit.net>
Date:   Thu Dec 2 06:32:00 2010 +1100

    Some improvements to dsl reference generation:
    - use parameter names from source in method signatures
    - handle overloaded methods
    - handle missing html end tags in javadoc comments
    - handle <ol> elements