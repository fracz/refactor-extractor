commit cb8e6ee0218fc068a350049052ddb62764fef212
Author: Irina.Chernushina <Irina.Chernushina@jetbrains.com>
Date:   Mon Apr 24 14:14:50 2017 +0200

    json schema refactoring:
    - simplify JsonSchemaVariantsTreeBuilder more: use it only from JsonSchemaResolver, with two methods: detailedResolve (for annotation) and resolve (other cases)