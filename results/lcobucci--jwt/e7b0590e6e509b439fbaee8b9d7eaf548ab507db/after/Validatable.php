<?php
/**
 * This file is part of Lcobucci\JWT, a simple library to handle JWT and JWS
 *
 * @license http://opensource.org/licenses/BSD-3-Clause BSD-3-Clause
 */

namespace Lcobucci\JWT\Claim;

/**
 * Basic interface for validatable token claims
 *
 * @author Luís Otávio Cobucci Oblonczyk <lcobucci@gmail.com>
 * @since 1.2.0
 */
interface Validatable
{
    /**
     * Returns if claim is valid according with given data
     *
     * @param array $data
     *
     * @return boolean
     */
    public function validate(array $data);
}