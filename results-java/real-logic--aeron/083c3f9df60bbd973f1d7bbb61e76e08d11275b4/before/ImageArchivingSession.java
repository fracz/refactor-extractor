/*
 * Copyright 2014 - 2017 Real Logic Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.aeron.archiver;

import io.aeron.*;
import org.agrona.LangUtil;
import org.agrona.concurrent.EpochClock;

/**
 * Consumes an {@link Image} and archives data into file using {@link StreamInstanceArchiveWriter}.
 * TODO: refactor out of State pattern
 */
class ImageArchivingSession
{

    enum State
    {
        INIT
        {
            int doWork(final ImageArchivingSession session)
            {
                session.state(ARCHIVING);
                return 1;
            }
        },

        ARCHIVING
        {
            int doWork(final ImageArchivingSession session)
            {
                final StreamInstanceArchiveWriter writer = session.writer;
                try
                {
                    final int delta = session.image.rawPoll(writer, ArchiveFileUtil.ARCHIVE_FILE_SIZE);
                    if (delta != 0)
                    {
                        session.archiverConductor.notifyArchiveProgress(
                            writer.streamInstanceId(),
                            writer.initialTermId(),
                            writer.initialTermOffset(),
                            writer.lastTermId(),
                            writer.lastTermOffset());
                    }
                    if (session.image.isClosed())
                    {
                        session.state(CLOSE);
                    }
                    return delta;
                }
                catch (Exception e)
                {
                    session.state(CLOSE);
                    LangUtil.rethrowUnchecked(e);
                }
                return 1;
            }
        },

        CLOSE
        {
            int doWork(final ImageArchivingSession session)
            {
                if (session.writer != null)
                {
                    session.writer.close();
                }
                session.archiverConductor.notifyArchiveStopped(session.streamInstanceId);
                session.state(DONE);
                return 1;
            }
        },

        DONE
        {
            int doWork(final ImageArchivingSession session)
            {
                return 0;
            }
        };

        abstract int doWork(ImageArchivingSession session);
    }

    private final int streamInstanceId;
    private final ArchiverConductor archiverConductor;
    private final Image image;
    private final StreamInstanceArchiveWriter writer;

    private State state = State.INIT;

    ImageArchivingSession(final ArchiverConductor archiverConductor, final Image image, final EpochClock epochClock)
    {
        this.archiverConductor = archiverConductor;
        this.image = image;

        final Subscription subscription = image.subscription();
        final int streamId = subscription.streamId();
        final String channel = subscription.channel();
        final int sessionId = image.sessionId();
        final String source = image.sourceIdentity();
        streamInstanceId = archiverConductor.notifyArchiveStarted(source, sessionId, channel, streamId);
        final int termBufferLength = image.termBufferLength();


        try
        {
            this.writer = new StreamInstanceArchiveWriter(archiverConductor.archiveFolder(),
                                                          epochClock,
                                                          streamInstanceId,
                                                          termBufferLength,
                                                          new StreamInstance(source, sessionId, channel, streamId));

        }
        catch (Exception e)
        {
            State.CLOSE.doWork(this);
            LangUtil.rethrowUnchecked(e);
            // the next line is to keep compiler happy with regards to final fields init
            throw new RuntimeException();
        }
    }

    void close()
    {
        state(State.CLOSE);
    }

    int doWork()
    {
        int workDone = 0;
        State initialState;
        do
        {
            initialState = state();
            workDone += state().doWork(this);
        }
        while (initialState != state());

        return workDone;
    }

    Image image()
    {
        return image;
    }

    State state()
    {
        return state;
    }

    private void state(final State state)
    {
        this.state = state;
    }

    int streamInstanceId()
    {
        return writer.streamInstanceId();
    }
}