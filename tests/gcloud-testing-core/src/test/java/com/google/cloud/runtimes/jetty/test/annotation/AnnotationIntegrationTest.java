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

package com.google.cloud.runtimes.jetty.test.annotation;

import com.google.cloud.runtime.jetty.test.LocalRemoteTestRunner;
import com.google.cloud.runtime.jetty.test.annotation.LocalOnly;
import com.google.cloud.runtime.jetty.test.annotation.RemoteOnly;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(LocalRemoteTestRunner.class)
public class AnnotationIntegrationTest {

  /**
   * Test for the @LocalOnly annotation.
   */
  @Test
  @LocalOnly
  public void testLocalOnlyAnnotation() {
    System.out.println("executed @LocalOnly");

    String mode = System.getProperty("test.mode");
    Assert.assertEquals("@LocalOnly", "local", mode);
    Assert.assertNotEquals("@LocalOnly broken", "remote", mode);
  }

  /**
   * Test for the @RemoteOnly annotation.
   */
  @Test
  @RemoteOnly
  public void testRemoteOnlyAnnotation() {
    System.out.println("executed @LocalOnly");

    String mode = System.getProperty("test.mode");
    Assert.assertEquals("@RemoteOnly", "remote", mode);
    Assert.assertNotEquals("@RemoteOnly broken", "local", mode);
  }

  /**
   * Test for the @RemoteOnly annotation.
   */
  @Test
  @Ignore
  public void testIgnoreAnnotation() {
    System.out.println("executed @Ignore");

    Assert.fail("this test should not execute based on Runner logic");
  }

  /**
   * Test for the @Test annotation.
   */
  @Test
  public void testAnnotation() {
    System.out.println("executed @Test");
    // should have run
    Assert.assertEquals("123",Integer.toString(123));
  }



}
