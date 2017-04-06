commit e659f0e39daafd3c0e75733cb1e32bd091fb9af8
Author: Bernhard Schussek <bschussek@gmail.com>
Date:   Thu Jul 26 15:18:46 2012 +0200

    [OptionsResolver] Improved the performance of normalizers

    Normalizers are now stored in the Options instance only once. Previously,
    normalizers were stored in Options upon resolving, which meant that
    they were added a lot of time if the same resolver was used for many
    different options arrays.

    This improvement led to an improvement of 30ms on
    advancedform.gpserver.dk/app_dev.php/taxclasses/1