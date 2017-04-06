commit 4f7ed2132e33d40f24b558bc6f4be553801e47f2
Author: Isabel Drost-Fromm <isabel.drostfromm@elasticsearch.com>
Date:   Wed Jun 24 11:01:29 2015 +0200

    Remove duplicate operator enums

    As we now have an enum Operator that comes with many useful helper methods switching to use
    that instead of the enums defined separately. Also switches to using the new enum's helper
    methods where applicable removing duplicate parsing logic.

    This breaks backwards compatibility. Documenting the break in
    migrate_query_refactoring.asciidoc

    Relates to #10217