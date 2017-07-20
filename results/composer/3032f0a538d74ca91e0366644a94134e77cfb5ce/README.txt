commit 3032f0a538d74ca91e0366644a94134e77cfb5ce
Author: nevvermind <adragus@inviqa.com>
Date:   Tue Jun 2 17:39:02 2015 +0100

    Refactor based on code review

    - Move the version api getter to the PluginManager And make it such that it can be mocked, but not pollute the public interface. That means "protected" visibility.
    - The plugin api version constant should still be used throughout the code.
    - Use different fixtures class names
    - Use regex possessive quantifiers for performance
    - Use full words for readability