commit c8ba433f1892f61e51991439e484e78322137eb6
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Fri Sep 23 13:14:30 2016 +0200

    refactor(jqLite): refactor the attr method

    The attr method was refactored to be divided into setter & getter parts
    and to handle boolean attributes in each one of them separately instead of
    dividing into boolean & non-boolean ones and then handling setter & getter
    in both of them. This is because handling boolean & non-boolean attributes
    has common parts; in particular handling of the `null` value or using
    getAttribute to get the value in the getter.