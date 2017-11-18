commit 6ed7dfa95a6fa4b162f9ea3948a46a0b55b36174
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Sat Jul 26 22:04:43 2014 -0700

    Removed type maker methods from TitanGraph. Types have to be made inside an explicit transaction or through TitanManagement. Test cases are refactored accordingly.