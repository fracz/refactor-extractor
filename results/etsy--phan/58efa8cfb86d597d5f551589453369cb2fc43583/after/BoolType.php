<?php declare(strict_types=1);
namespace Phan\Language\Type;

use Phan\Language\UnionType;
use Phan\Language\Type;

// Temporary hack to load FalseType and TrueType before BoolType::instance() is called
// (Due to bugs in php static variables)
assert(class_exists(FalseType::class));
assert(class_exists(TrueType::class));

class BoolType extends ScalarType
{
    const NAME = 'bool';
    public static function unionTypeInstance(bool $is_nullable) : UnionType
    {
        // Optimized equivalent of `return new UnionType([FalseType::instance($is_nullable), TrueType::instance($is_nullable)]);`
        if ($is_nullable) {
            static $nullable_instance = null;
            if ($nullable_instance === null) {
                $nullable_instance = new UnionType([FalseType::instance(true), TrueType::instance(true)]);
            }
            return clone($nullable_instance);
        }
        static $instance = null;
        if ($instance === null) {
            $instance = new UnionType([FalseType::instance(false), TrueType::instance(false)]);
        }
        return clone($instance);
    }

    public function getIsPossiblyFalsey() : bool
    {
        return true;  // it's always falsey, since this is conceptually a collection of FalseType and TrueType
    }

    public function asNonFalseyType() : Type
    {
        return TrueType::instance(false);
    }

    public function asNonTruthyType() : Type
    {
        return FalseType::instance($this->is_nullable);
    }

    public function getIsPossiblyFalse() : bool
    {
        return true;  // it's possibly false, since this is conceptually a collection of FalseType and TrueType
    }

    public function asNonFalseType() : Type
    {
        return TrueType::instance($this->is_nullable);
    }

    public function getIsPossiblyTrue() : bool
    {
        return true;  // it's possibly true, since this is conceptually a collection of FalseType and TrueType
    }

    public function asNonTrueType() : Type
    {
        return FalseType::instance($this->is_nullable);
    }

    public function getIsInBoolFamily() : bool
    {
        return true;
    }

    public function getIsAlwaysTruthy() : bool
    {
        return false;  // overridden in various types. This base class (Type) is implicitly the type of an object, which is always truthy.
    }
}