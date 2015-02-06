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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path( "/HDP" )
public class Resource {

  private static final Pattern VERSION_REGEX = Pattern.compile( "^.*/(.*?)/$" );

  private static final String HDP_BUILD_INFO_URL = "http://s3.amazonaws.com/dev.hortonworks.com/HDP/hdp_urlinfo.json";
  private static final Map<String, String> REPO_FILES = new HashMap<>();
  static {
    REPO_FILES.put( "centos5", "hdpbn.repo" );
    REPO_FILES.put( "centos6", "hdpbn.repo" );
    REPO_FILES.put( "suse11", "hdp.repo" );
    REPO_FILES.put( "ubuntu12", "hdp.list" );
  }

  private static Client client = ClientBuilder.newClient().register( JacksonJsonProvider.class );
  private static WebTarget target = client.target( System.getProperty( "info.url", HDP_BUILD_INFO_URL ) );
  private static long timeout = getTimeout();
  private static long timestamp = 0;
  private static Map<String, TreeMap<Version, String>> cache = null;

  // http://s3.amazonaws.com/dev.hortonworks.com/HDP/hdp_urlinfo.json

  private static long getTimeout() {
    PeriodFormatter formatter = new PeriodFormatterBuilder()
        .appendHours().appendSuffix("h")
        .appendMinutes().appendSuffix("m")
        .appendSeconds().appendSuffix("s")
        .toFormatter();
    Period period = formatter.parsePeriod( System.getProperty( "cache.timeout", "15m" ) );
    long millis = period.toStandardDuration().getMillis();
    return millis;
  }

  private static Version extractVersionFromRepoUri( String uri ) {
    Matcher matcher = VERSION_REGEX.matcher( uri );
    if( !matcher.find() ) {
      throw new IllegalArgumentException( uri );
    }
    return new Version( matcher.group( 1 ) );
  }

  private static TreeMap<Version, String> getVerToUriMap( Map<String, TreeMap<Version, String>> mapping, String platform ) {
    TreeMap<Version, String> verToUriMap = mapping.get( platform );
    if( verToUriMap == null ) {
      verToUriMap = new TreeMap<>();
      mapping.put( platform, verToUriMap );
    }
    return verToUriMap;
  }

  private static synchronized void cacheRepoInfo() {
    Map<String, TreeMap<Version, String>> mapping = new HashMap<>();
    Response response = target.request().accept( MediaType.APPLICATION_JSON_TYPE ).get( Response.class );
    JsonNode json = response.readEntity( JsonNode.class );
    Iterator<Map.Entry<String, JsonNode>> versions = json.fields();
    while( versions.hasNext() ) {
      Map.Entry<String, JsonNode> series = versions.next();
      //System.out.println( series.getKey() );
      JsonNode latest = series.getValue().get( "latest" );
      Iterator<Map.Entry<String, JsonNode>> builds = latest.fields();
      while( builds.hasNext() ) {
        Map.Entry<String, JsonNode> build = builds.next();
        String platform = build.getKey();
        String uri = build.getValue().asText();
        Version version = extractVersionFromRepoUri( uri );
        getVerToUriMap( mapping, platform ).put( version, uri );
        //System.out.println( "  " + build.getKey() + ":" + build.getValue().asText() );
      }
    }
    cache = mapping;
    timestamp = System.currentTimeMillis();
  }

  private synchronized Map<String, TreeMap<Version, String>> getCachedRepoInfo() {
    if( ( cache == null ) || ( System.currentTimeMillis() - timestamp > timeout ) )  {
      cacheRepoInfo();
    }
    return cache;
  }

  private String getRepoFileName( String platform ) {
    return System.getProperty( "platform."+platform, REPO_FILES.get( platform ) );
  }

  @Path( "/{platform}/{series}/{type}/{version}/{file}" )
  @GET
  @Consumes( MediaType.WILDCARD )
  @Produces( MediaType.WILDCARD )
  public Response getRedirect(
      @PathParam( "platform" ) @DefaultValue( "centos6" ) String platform,
      @PathParam( "series" ) @DefaultValue( "x.x" ) String series,
      @PathParam( "type" ) @DefaultValue( "BUILDS" ) String type,
      @PathParam( "version" ) @DefaultValue( "x.x" ) String version,
      @PathParam( "file" ) @DefaultValue( "hdpbn.repo" ) String file ) throws URISyntaxException {
    Response response;
    Map<String,TreeMap<Version,String>> repoInfo = getCachedRepoInfo();
    Version reqVer = new Version( version );
    TreeMap<Version, String> verToUriMap = getVerToUriMap( repoInfo, platform );
    response = Response.status( Response.Status.NOT_FOUND ).build();
    for( Version repoVer : verToUriMap.descendingKeySet() ) {
      if( Version.match( repoVer, reqVer ) ) {
        response = Response.temporaryRedirect(
            new URI( verToUriMap.get( repoVer ) + getRepoFileName( platform ) ) ).build();
        break;
      }
    }
    return response;
  }

  @Path( "/{platform}/{series}/{type}/{version}" )
  @GET
  @Consumes( MediaType.WILDCARD )
  @Produces( MediaType.WILDCARD )
  public Response getRedirect(
      @PathParam( "platform" ) @DefaultValue( "centos6" ) String platform,
      @PathParam( "series" ) @DefaultValue( "x.x" ) String series,
      @PathParam( "type" ) @DefaultValue( "BUILDS" ) String type,
      @PathParam( "version" ) @DefaultValue( "x.x" ) String version ) throws URISyntaxException {
    return getRedirect( platform, series, type, version, "hdp.repo" );
  }

}
