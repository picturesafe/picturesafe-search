module picturesafe.search {
    requires java.sql;
    requires spring.context;
    requires spring.beans;
    requires elasticsearch;
    requires elasticsearch.rest.client;
    requires elasticsearch.rest.client.sniffer;
    requires org.apache.commons.lang3;
    requires commons.collections;
    requires org.apache.logging.log4j.core;
    requires org.slf4j;
}