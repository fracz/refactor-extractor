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

namespace Aws\S3\Model\MultipartUpload;

use Aws\Common\Enum\UaString as Ua;
use Guzzle\Http\EntityBody;
use Guzzle\Http\ReadLimitEntityBody;

/**
 * Transfers multipart upload parts serially
 */
class SerialTransfer extends AbstractTransfer
{
    /**
     * {@inheritdoc}
     */
    protected function transfer()
    {
        while (!$this->stopped && !$this->source->isConsumed()) {

            if ($this->source->getContentLength() && $this->source->isSeekable()) {
                // If the stream is seekable and the Content-Length known, then stream from the data source
                $body = new ReadLimitEntityBody($this->source, $this->partSize, $this->source->ftell());
            } else {
                // We need to read the data source into a temporary buffer before streaming
                $body = EntityBody::factory();
                while ($body->getContentLength() < $this->partSize
                    && $body->write(
                        $this->source->read(max(1, min(10240, $this->partSize - $body->getContentLength())))
                    ));
            }

            // @codeCoverageIgnoreStart
            if ($body->getContentLength() == 0) {
                break;
            }
            // @codeCoverageIgnoreEnd

            $params = $this->state->getIdParams();
            $command = $this->client->getCommand('UploadPart', array_replace($params, array(
                'PartNumber' => count($this->state) + 1,
                'body'       => $body,
                'use_md5'    => $this->options['part_md5'],
                Ua::OPTION   => Ua::MULTIPART_UPLOAD
            )));

            // Notify observers that the part is about to be uploaded
            $eventData = $this->getEventData();
            $eventData['command'] = $command;
            $this->dispatch(self::BEFORE_PART_UPLOAD, $eventData);

            // Allow listeners to stop the transfer if needed
            if ($this->stopped) {
                break;
            }

            $response = $command->getResponse();

            $this->state->addPart(UploadPart::fromArray(array(
                'PartNumber'   => count($this->state) + 1,
                'ETag'         => $response->getHeader('ETag', true),
                'Size'         => $body->getContentLength(),
                'LastModified' => gmdate('r')
            )));

            // Notify observers that the part was uploaded
            $this->dispatch(self::AFTER_PART_UPLOAD, $eventData);
        }
    }
}