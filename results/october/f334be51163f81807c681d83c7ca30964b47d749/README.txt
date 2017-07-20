commit f334be51163f81807c681d83c7ca30964b47d749
Author: Samuel Georges <sam@daftspunk.com>
Date:   Sat May 2 14:43:14 2015 +1000

    Complete overhaul of module service providers
    This is to improve readability of these ever growing classes, also we can prune specific registrations based on the execution context for performance reasons.