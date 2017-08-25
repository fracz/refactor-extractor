<?php declare(strict_types=1);
namespace Phan\Language\Type;

use Phan\Language\Type;

// Not sure if it made sense to extend BoolType, so not doing that.
class TrueType extends ScalarType
{
    const NAME = 'true';

    public function getIsPossiblyTruthy() : bool
    {
        return true;
    }

    public function getIsAlwaysTruthy() : bool
    {
        return true;
    }

    public function getIsPossiblyTrue() : bool
    {
        return true;
    }

    public function getIsAlwaysTrue() : bool
    {
        return !$this->is_nullable;  // If it can be null, it's not **always** identical to true
    }

    public function asNonTrueType() : Type
    {
        assert($this->is_nullable, 'should only call on ?true');
        return NullType::instance(false);
    }

    public function getIsInBoolFamily() : bool
    {
        return true;
    }
}