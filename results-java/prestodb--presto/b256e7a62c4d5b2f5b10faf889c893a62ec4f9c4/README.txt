commit b256e7a62c4d5b2f5b10faf889c893a62ec4f9c4
Author: Andrii Rosa <Andriy.Rosa@TERADATA.COM>
Date:   Tue Apr 19 15:26:34 2016 +0200

    Use createUnboundedVarcharType in Hive connector

    Use explicit VarcharType.createUnboundedVarcharType instead of VarcharType.VARCHAR
    constant in Hive connector.

    Varchar type should always be parametrized, and it must be checked with
    `Varchars.isVarcharType(type)` instead of `type.equals(VarcharType.VARCHAR)`,
    so the varchar with the length less then MAX_LENGTH is being handled properly.

    Having the `VarcharType.VARCHAR` constant is confusing and error prone, cause it
    suggests user that the only one single unparametrized `VARCHAR` type is available.

    After refactor all the other connecotors and `VARCHAR` operators that constant
    must be removed at all.