commit e69109e521028ef6141da8bfee0e4efd2aaba974
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Mon Sep 26 15:50:22 2016 +0700

    StreamApiMigrationInspection improvements: expression+break conversion to anyMatch; pull out cast of stream.findFirst().map(x -> (T)x).orElse(null) to (T) stream.findFirst().orElse(null)