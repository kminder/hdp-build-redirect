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

import java.util.ArrayList;
import java.util.List;

public class Version implements Comparable<Version> {

  private static final char WILDCARD = '*';

  private static class Part {

    private String delimiter;
    private String qualifier;
    private String version;
    private Integer versionInt;
    private boolean wildcard;

    private Part( final String delimiter, final String qualifier, final String version ) {
      this.delimiter = delimiter;
      this.qualifier = qualifier;
      this.version = version;
      this.wildcard = ( ( version != null ) && ( version.indexOf( WILDCARD ) == 0 ) );
      this.versionInt = ( ( version == null || wildcard ) ? null : new Integer( version ) );
    }

  }

  private String original;
  private List<Part> parts;

  public Version( String version ) {
    original = version;
    parts = parseVersion( version);
  }

  @Override
  public String toString() {
    return original;
  }

  @Override
  public int hashCode() {
    return original.hashCode();
  }

  @Override
  public boolean equals( Object object ) {
    boolean equal = false;
    if( object instanceof String ) {
      equal = original.equals( object );
    } else if( object instanceof Version ) {
      equal = original.equals( ((Version)object).original );
    }
    return equal;
  }

  @Override
  public int compareTo( Version that ) {
    if( that == null ) {
      return 1;
    }
    int thisSize = this.parts.size();
    int thatSize = that.parts.size();
    int n = Math.min( thisSize, thatSize );
    for( int i=0; i<n; i++ ) {
      Part thisPart = this.parts.get( i );
      Part thatPart = that.parts.get( i );
      Integer thisVer = thisPart.versionInt;
      Integer thatVer = thatPart.versionInt;
      if( thisVer == null && thatVer == null ) {
        continue;
      } else if( thisVer == null ) {
        return thisPart.wildcard ? 1 : -1;
      } else if( thatVer == null ) {
        return thatPart.wildcard ? -1 : 1;
      } else {
        int s = thisVer.compareTo( thatVer );
        if( s != 0 ) {
          return s;
        }
      }
    }
    if( thisSize == thatSize ) {
      return 0;
    } else if( thisSize > thatSize ) {
      return 1;
    } else {
      return -1;
    }
  }

  private static List<Part> parseVersion( String version ) {
    List<Part> parts = new ArrayList<>();
    if( version != null ) {
      StringBuilder delimiter = new StringBuilder( 1 );
      StringBuilder qualifier = new StringBuilder();
      StringBuilder number = new StringBuilder();
      char c;
      for( int i = 0, n = version.length(); i < n; i++ ) {
        c = version.charAt( i );
        switch( c ) {
          case '.':
          case '_':
          case '-':
          case '#':
          case '~':
          case '^':
            parts.add( new Part( delimiter.toString(), qualifier.toString(), number.toString() ) );
            delimiter.setLength( 0 );
            delimiter.append( c );
            qualifier.setLength( 0 );
            number.setLength( 0 );
            break;
          case WILDCARD:
            number.append( c );
            break;
          default:
            if( Character.isDigit( c ) ) {
              number.append( c );
            } else if( number.length() == 0 ) {
              qualifier.append( c );
            } else {
              parts.add( new Part( delimiter.toString(), qualifier.toString(), number.toString() ) );
              delimiter.setLength( 0 );
              qualifier.setLength( 0 );
              qualifier.append( c );
              number.setLength( 0 );
            }
            break;
        }
      }
      if( delimiter.length() > 0 || qualifier.length() > 0 || number.length() > 0 ) {
        parts.add( new Part( delimiter.toString(), qualifier.toString(), number.toString() ) );
      }
    }
    return parts;
  }

  private static boolean match( Part literal, Part pattern ) {
    boolean match = true;
    match &= pattern.delimiter.equals( literal.delimiter );
    match &= pattern.qualifier.equals( literal.qualifier );
    if( !pattern.wildcard ) {
      match &= pattern.version.equals( literal.version );
    }
    return match;
  }

  public static boolean match( Version literal, Version pattern ) {
    boolean match = true;
    for( int i=0,n=pattern.parts.size(); match && i<n; i++ ) {
      if( i < literal.parts.size() ) {
        Part literalPart = literal.parts.get( i );
        Part patternPart = pattern.parts.get( i );
        match &= match( literalPart, patternPart );
      } else {
        match = false;
      }
    }
    return match;
  }

}
