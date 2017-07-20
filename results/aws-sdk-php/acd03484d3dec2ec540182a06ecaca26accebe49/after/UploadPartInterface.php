<?php
/**
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

namespace Aws\Common\MultipartUpload;

/**
 * An object that encapsulates the data for an upload part
 */
interface UploadPartInterface extends \Serializable
{
    /**
     * Create an upload part from an array
     *
     * @param array $data data representing the upload part
     *
     * @return self
     */
    public static function fromArray(array $data);

    /**
     * Returns the part number of the upload part which is used as an identifier
     *
     * @return int
     */
    public function getPartNumber();

    /**
     * Returns the array form of the upload part
     *
     * @return array
     */
    public function toArray();
}