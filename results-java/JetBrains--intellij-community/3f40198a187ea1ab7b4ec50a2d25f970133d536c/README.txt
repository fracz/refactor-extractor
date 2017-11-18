commit 3f40198a187ea1ab7b4ec50a2d25f970133d536c
Author: Aleksey Pivovarov <AMPivovarov@gmail.com>
Date:   Mon Aug 1 16:17:25 2016 +0300

    github: remove two-step deserialization - fix non-mandatory fields

    * these checks were in constructors, and were missed during the refactoring