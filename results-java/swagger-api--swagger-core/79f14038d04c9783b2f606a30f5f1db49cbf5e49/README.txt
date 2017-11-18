commit 79f14038d04c9783b2f606a30f5f1db49cbf5e49
Author: russellb337 <russbollesjr@gmail.com>
Date:   Wed Jul 1 16:23:35 2015 -0700

    Update RefModel, RefParameter and RefProperty to allow relative references

    - add the ability to specify a relative reference (e.g.
    ./path/to/file.json, ./path/to/file.json#/thing)
    - refactor duplicated ref logic into GenericRef and delegate to from
    RefModel, RefParameter, and RefProperty