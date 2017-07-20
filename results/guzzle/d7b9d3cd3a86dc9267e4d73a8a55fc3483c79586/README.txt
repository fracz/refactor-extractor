commit d7b9d3cd3a86dc9267e4d73a8a55fc3483c79586
Author: Michael Dowling <mtdowling@gmail.com>
Date:   Wed Mar 6 23:11:41 2013 -0800

    Using union rather than merge when extending operations and adding tests. This has no effect other than speed improvements because when merging arrays where the key is a string, the last value overwrites the previous. However, this may fix edge cases where someone is defining arrays in their models for some reason.