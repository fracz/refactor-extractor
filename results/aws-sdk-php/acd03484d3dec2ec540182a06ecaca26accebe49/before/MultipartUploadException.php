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

namespace Aws\S3\Exception;

use Aws\S3\Model\MultipartUpload\TransferState;

/**
 * Exception thrown when a {@see Aws\S3\Model\MultipartUpload\TransferInterface}
 * object encounters an error during transfer
 */
class MultipartUploadException extends S3Exception
{
    /**
     * @var TransferState State of the transfer when the error was encountered
     */
    protected $state;

    /**
     * @param TransferState $state     Transfer state
     * @param \Exception    $exception Last encountered exception
     */
    public function __construct(TransferState $state, \Exception $exception = null)
    {
        parent::__construct('An error was encountered while performing a multipart upload', 0, $exception);
    }

    /**
     * Get the state of the transfer
     *
     * @return TransferState
     */
    public function getState()
    {
        return $this->state;
    }
}