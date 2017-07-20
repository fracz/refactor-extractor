commit 49e68de96b420a444c826995746a5f09470e76d9
Author: Andrey Andreev <narf@bofh.bg>
Date:   Thu Feb 21 16:30:55 2013 +0200

    Disable autoloader call from class_exists() occurences to improve performance

    Note: The Driver libary tests seem to depend on that, so one occurence in CI_Loader is left until we resolve that.