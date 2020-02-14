/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.util.test;

import org.elasticsearch.bootstrap.JarHell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarHellUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JarHellUtils.class);

    private JarHellUtils() {
    }

    public static void checkJarHell() throws Exception {
        JarHell.checkJarHell(LOGGER::debug);
    }
}
