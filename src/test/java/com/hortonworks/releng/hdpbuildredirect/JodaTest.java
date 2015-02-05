/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hortonworks.releng.hdpbuildredirect;

import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JodaTest {

  @Test
  public void testJoda() {

    PeriodFormatter formatter = new PeriodFormatterBuilder()
        .appendHours().appendSuffix("h")
        .appendMinutes().appendSuffix("m")
        .appendSeconds().appendSuffix("s")
        .toFormatter();

    assertThat( formatter.parsePeriod( "1s" ).toStandardDuration().getMillis(), is( 1000L ) );
    assertThat( formatter.parsePeriod( "1m" ).toStandardDuration().getMillis(), is( 1000L * 60 ) );
    assertThat( formatter.parsePeriod( "1h" ).toStandardDuration().getMillis(), is( 1000L * 60 * 60 ) );

    assertThat(
        formatter.parsePeriod( "1h1m1s" ).toStandardDuration().getMillis(),
        is( ( 1000L * 60L * 60L ) + ( 1000L * 60L ) + ( 1000L ) ) );
  }



}
