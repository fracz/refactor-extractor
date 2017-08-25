<?php declare(strict_types=1);
namespace Phan\Language\Type;

use Phan\CodeBase;
use Phan\Config;
use Phan\Language\Type;
use Phan\Language\UnionType;
use Phan\Language\Context;

abstract class ScalarType extends NativeType
{
    public function isScalar() : bool
    {
        return true;
    }

    public function isSelfType() : bool
    {
        return false;
    }

    public function isStaticType() : bool
    {
        return false;
    }

    public function isIterable() : bool
    {
        return false;
    }

    public function isArrayLike() : bool
    {
        return false;
    }

    public function isGenericArray() : bool
    {
        return false;
    }

    /**
     * @param CodeBase $code_base
     *
     * @param Type $parent
     *
     * @return bool
     * True if this type represents a class which is a sub-type of
     * the class represented by the passed type.
     */
    public function isSubclassOf(CodeBase $code_base, Type $parent) : bool
    {
        return false;
    }

    /**
     * @return bool
     * True if this Type can be cast to the given Type
     * cleanly
     */
    protected function canCastToNonNullableType(Type $type) : bool
    {
        // Scalars may be configured to always cast to eachother
        if ($type->isScalar()) {
            if (Config::getValue('scalar_implicit_cast')) {
                return true;
            }
            $scalar_implicit_partial = Config::getValue('scalar_implicit_partial');
            if (\count($scalar_implicit_partial) > 0) {
                // check if $type->getName() is in the list of permitted types $this->getName() can cast to.
                if (\in_array($type->getName(), Config::get()->scalar_implicit_partial[$this->getName()] ?? [], true)) {
                    return true;
                }
            }
        }

        return parent::canCastToNonNullableType($type);
    }

    /**
     * @override
     */
    public function isExclusivelyNarrowedFormOrEquivalentTo(
        UnionType $union_type,
        Context $context,
        CodeBase $code_base
    ) : bool {
        return $union_type->hasType($this) || $this->asUnionType()->canCastToUnionType($union_type);
    }

    /**
     * @override
     */
    public function asFQSENString() : string
    {
        return $this->name;
    }
}