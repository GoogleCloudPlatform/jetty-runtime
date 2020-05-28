/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.runtimes.jetty.test.smoke;

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

import java.util.concurrent.TimeUnit;

public class DumpOutputTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpOutputTest.class);

    @ClassRule
    public static GenericContainer jettyWarSmoke =
        new GenericContainer("test-war-smoke:latest")
            .withEnv( "HEAP_SIZE_MB","512" )
            .withEnv( "SHUTDOWN_LOGGING_THREAD_DUMP","true" )
            .withEnv( "SHUTDOWN_LOGGING_HEAP_INFO","true" );

    @Test
    public void checkOutput() throws Exception
    {
        WaitingConsumer waitingConsumer = new WaitingConsumer();
        try
        {
            jettyWarSmoke.start();
            Slf4jLogConsumer logConsumer = new Slf4jLogConsumer( LOGGER );


            jettyWarSmoke.followOutput( logConsumer.andThen( waitingConsumer ) );
            waitingConsumer.waitUntil( frame -> frame.getUtf8String().contains( "Server: Started" ), 30,
                                       TimeUnit.SECONDS );
        } finally
        {
            jettyWarSmoke.stop();
            // not sure how to get this output
//            waitingConsumer.waitUntil( frame -> frame.getUtf8String().contains( "Full thread dump OpenJDK 64-Bit Server VM" ), 30,
//                                       TimeUnit.SECONDS );
        }
    }

}
