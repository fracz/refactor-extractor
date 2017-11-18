commit 4be87e3468b89b6733da006b5b0a76c3473725a7
Author: anna <anna.kozlova@jetbrains.com>
Date:   Wed Apr 21 13:21:52 2010 +0400

    erasure type parameters instead of type inference as inferenced type would be a super type for the desired one thus refactoring would stop (@see toArray)