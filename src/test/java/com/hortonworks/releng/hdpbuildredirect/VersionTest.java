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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class VersionTest {

  @Test
  public void testUseCase() {
    Version v1, v2, v3;
    v1 = new Version( "2.2.1.0-2340" );
    v2 = new Version( "2.*" );
    v3 = new Version( "2.3.*" );
    assertThat( Version.match( v1, v2 ), is( true ) );
    assertThat( Version.match( v1, v3 ), is( false ) );
  }

  @Test
  public void testMatch() {
    Version v1, v2;

    v1 = new Version( "1" );
    v2 = new Version( "1" );
    assertThat( Version.match( v1, v2 ), is( true ) );
    assertThat( Version.match( v2, v1 ), is( true ) );

    v1 = new Version( "1.2" );
    v2 = new Version( "1" );
    assertThat( Version.match( v1, v2 ), is( true ) );
    assertThat( Version.match( v2, v1 ), is( false ) );

    v1 = new Version( "1" );
    v2 = new Version( "*" );
    assertThat( Version.match( v1, v2 ), is( true ) );
    assertThat( Version.match( v2, v1 ), is( false ) );

    v1 = new Version( "1.2" );
    v2 = new Version( "*" );
    assertThat( Version.match( v1, v2 ), is( true ) );
    assertThat( Version.match( v2, v1 ), is( false ) );

    v1 = new Version( "1.2" );
    v2 = new Version( "*.*" );
    assertThat( Version.match( v1, v2 ), is( true ) );
    assertThat( Version.match( v2, v1 ), is( false ) );

    v1 = new Version( "1rc2" );
    v2 = new Version( "1rc*" );
    assertThat( Version.match( v1, v2 ), is( true ) );
    assertThat( Version.match( v2, v1 ), is( false ) );
    v2 = new Version( "1" );
    assertThat( Version.match( v1, v2 ), is( true ) );
    assertThat( Version.match( v2, v1 ), is( false ) );
  }

  @Test
  public void testVersionEdgeCases() {
    Version v1, v2;

    v1 = new Version( null );
    v2 = new Version( null );
    assertThat( Version.match( v1, v2 ), is( true ) );

    v1 = new Version( null );
    v2 = new Version( "*" );
    assertThat( Version.match( v1, v2 ), is( false ) );

    v1 = new Version( "" );
    v2 = new Version( "" );
    assertThat( Version.match( v1, v2 ), is( true ) );

    v1 = new Version( "" );
    v2 = new Version( "*" );
    assertThat( Version.match( v1, v2 ), is( false ) );
  }

  @Test
  public void testCompareTo() {
    Version v1, v2;

    v1 = new Version( "1" );
    v2 = new Version( "1" );
    assertThat( v1.compareTo( v2 ), is( 0 ) );

    v1 = new Version( "1" );
    v2 = new Version( "2" );
    assertThat( v1.compareTo( v2 ), is( -1 ) );

    v1 = new Version( "2" );
    v2 = new Version( "1" );
    assertThat( v1.compareTo( v2 ), is( 1 ) );

    v1 = new Version( "1" );
    v2 = new Version( "1.2" );
    assertThat( v1.compareTo( v2 ), is( -1 ) );
    assertThat( v2.compareTo( v1 ), is( 1 ) );

    v1 = new Version( "1.1" );
    v2 = new Version( "1.*" );
    assertThat( v1.compareTo( v2 ), is( -1 ) );
    assertThat( v2.compareTo( v1 ), is( 1 ) );
  }

  @Test
  public void testHashCode() {
    Version v1, v2;

    v1 = new Version( "1" );
    v2 = new Version( "2" );
    assertThat( v1.hashCode() == v2.hashCode(), is( false ) );

    v1 = new Version( "1" );
    v2 = new Version( "1" );
    assertThat( v1.hashCode() == v2.hashCode(), is( true ) );
  }

  @Test
  public void testEquals() {
    Version v1, v2;

    v1 = new Version( "1" );
    v2 = new Version( "2" );
    assertThat( v1.equals( v2 ), is( false ) );

    v1 = new Version( "1" );
    v2 = new Version( "1" );
    assertThat( v1.equals( v2 ), is( true ) );
  }

  @Test
  public void testSortedList() {
    List<Version> list = new ArrayList<>();
    list.add( new Version("1.1") );
    list.add( new Version("2") );
    list.add( new Version("1.1.2") );
    Collections.sort( list );
    assertThat( list.get( 2 ), is( new Version("2") ) );
    assertThat( list.get( 1 ), is( new Version("1.1.2") ) );
    assertThat( list.get( 0 ), is( new Version("1.1") ) );
  }

}
