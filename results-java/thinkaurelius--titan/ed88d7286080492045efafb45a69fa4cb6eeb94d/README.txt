commit ed88d7286080492045efafb45a69fa4cb6eeb94d
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Mon Aug 20 16:08:25 2012 -0700

    Significant rewriting/improvements to the query engine. Fixes #44. Fixes possible double constraint counting bug. Fixes null handling in general. Speeds up query execution by smarter interval intersection. Extends test cases.