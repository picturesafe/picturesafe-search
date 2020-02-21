/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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

package de.picturesafe.search.elasticsearch.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.sniff.Sniffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Configuration presets for the elasticsearch REST client.
 */
public class RestClientConfiguration implements DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(RestClientConfiguration.class);

    private static final int DEFAULT_SNIFF_INTERVAL_MINUTES = 5;

    private final String hostAddresses;
    private int sniffIntervalMinutes = DEFAULT_SNIFF_INTERVAL_MINUTES;
    private Sniffer sniffer;
    private boolean snifferEnabled = false;
    private String userName;
    private String password;

    private RestHighLevelClient client;

    private final Lock lock = new ReentrantLock();

    /**
     * Constructor
     * @param hostAddresses List of host addresses to connect to (blank separated, format {@literal <hostname>:<port>} or {@literal <ip>:<port>})
     */
    public RestClientConfiguration(String hostAddresses) {
        this.hostAddresses = hostAddresses;
    }

    /**
     * Sets the user name (if mandatory by the elasticsearch installation).
     * @param userName User name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Sets the user password (if mandatory by the elasticsearch installation).
     * @param password User password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets if nodes sniffer / autodiscovery is enabled.
     * @param snifferEnabled TRUE if nodes sniffer is enabled
     */
    public void setSnifferEnabled(boolean snifferEnabled) {
        this.snifferEnabled = snifferEnabled;
    }

    /**
     * Sets the interval for the nodes sniffer.
     * @param sniffIntervalMinutes Interval for the nodes sniffer in minutes
     */
    public void setSniffIntervalMinutes(int sniffIntervalMinutes) {
        this.sniffIntervalMinutes = sniffIntervalMinutes;
    }

    /**
     * Gets the host addresses.
     * @return List of host addresses to connect to (blank separated, format {@literal <hostname>:<port>} or {@literal <ip>:<port>})
     */
    public String getHostAddresses() {
        return hostAddresses;
    }

    /**
     * Gets the elasticsearch REST client.
     * @return Elasticsearch REST client
     */
    public RestHighLevelClient getClient() {
        if (client == null) {
            lock.lock();
            try {
                if (client == null) {
                    client = createClient();
                }
            } finally {
                lock.unlock();
            }
        }
        return client;
    }

    /**
     * Closes the elasticsearch REST client and the nodes sniffer.
     */
    @Override
    public void destroy() {
        if (client != null) {
            lock.lock();
            try {
                client.close();
                client = null;
            } catch (IOException e) {
                LOG.error("Failed to close the rest client", e);
            } finally {
                if (sniffer != null) {
                    sniffer.close();
                }
                lock.unlock();
            }
        }
    }

    private RestHighLevelClient createClient() {
        final Set<String> addresses = splitHostAddresses();
        Validate.notEmpty(addresses, "Attribute 'hostAddresses' must be not empty");

        final HttpHost[] httpHosts = new HttpHost[addresses.size()];
        int offset = 0;
        for (String address : addresses) {
            try {

                final InetAddress host = InetAddress.getByName(StringUtils.substringBefore(address, ":"));
                final int port = Integer.parseInt(StringUtils.substringAfter(address, ":"));
                httpHosts[offset++] = new HttpHost(host.getHostName(), port, "http");
            } catch (UnknownHostException e) {
                throw new RuntimeException("Unkown host: " + address, e);
            }
        }
        final RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);
        final boolean useSecurity = StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password);
        if (useSecurity) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
            restClientBuilder.setHttpClientConfigCallback((httpClientBuilder) -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        final RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        if (snifferEnabled) {
            this.sniffer = Sniffer.builder(restHighLevelClient.getLowLevelClient()).setSniffIntervalMillis(sniffIntervalMinutes * 60 * 1000).build();
        }

        return restHighLevelClient;
    }

    private Set<String> splitHostAddresses() {
        final Set<String> addresses = new HashSet<>();
        for (String token : hostAddresses.split("\\s")) {
            final String address = token.trim();

            if (StringUtils.isNotBlank(address)) {
                if (validateHostAddress(address)) {
                    addresses.add(token.trim());
                } else {
                    throw new IllegalArgumentException("Invalid address \"" + address + "\" within host addresses");
                }
            }
        }

        return addresses;
    }

    private boolean validateHostAddress(String address) {
        if (!StringUtils.isNotBlank(address)) {
            LOG.warn("The host address must be not empty");
            return false;
        }

        final String[] token = address.split(":");
        if (token.length != 2) {
            LOG.warn("The host address " + address + "isn't valid");
            return false;
        }

        try {
            final int port = Integer.parseInt(token[1]);
            if (port < 0 || port > 0xFFFF) {
                LOG.warn("The port " + port + " of the host address " + address + " is invalid");
                return false;
            }


        } catch (NumberFormatException e) {
            LOG.warn("The host address " + address + " contains an invalid port");
            return false;
        }

        return true;
    }
}
