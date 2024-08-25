/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.storm.security.auth.plain;

import java.util.Optional;
import org.apache.storm.security.auth.AuthUtils;
import org.apache.storm.security.auth.sasl.SaslTransportPlugin;
import org.apache.storm.security.auth.sasl.SimpleSaslServerCallbackHandler;
import org.apache.thrift.transport.TSaslClientTransport;
import org.apache.thrift.transport.TSaslServerTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;
import java.security.Security;

/**
 * This should never be used except for testing.  It provides no security at all.
 * The password is hard coded, and even if it were not it is sent in plain text.
 */
@Deprecated
public class PlainSaslTransportPlugin extends SaslTransportPlugin {
    public static final String PLAIN = "PLAIN";
    private static final Logger LOG = LoggerFactory.getLogger(PlainSaslTransportPlugin.class);

    @Override
    protected TTransportFactory getServerTransportFactory() throws IOException {
        //create an authentication callback handler
        CallbackHandler serverCallbackHandler = new SimpleSaslServerCallbackHandler((userName) -> Optional.of("password".toCharArray()));
        if (Security.getProvider(SaslPlainServer.SecurityProvider.SASL_PLAIN_SERVER) == null) {
            Security.addProvider(new SaslPlainServer.SecurityProvider());
        }
        //create a transport factory that will invoke our auth callback for digest
        TSaslServerTransport.Factory factory = new TSaslServerTransport.Factory();
        factory.addServerDefinition(PLAIN, AuthUtils.SERVICE, "localhost", null, serverCallbackHandler);

        LOG.error("SASL PLAIN transport factory will be used.  This is totally insecure.  Please do not use this.");
        return factory;
    }

    @Override
    public TTransport connect(TTransport transport, String serverHost, String asUser) throws IOException, TTransportException {
        PlainClientCallbackHandler clientCallbackHandler = new PlainClientCallbackHandler();
        TSaslClientTransport wrapperTransport = new TSaslClientTransport(PLAIN,
            null,
            AuthUtils.SERVICE,
            serverHost,
            null,
            clientCallbackHandler,
            transport);

        wrapperTransport.open();
        LOG.error("SASL PLAIN client transport has been established.  This is totally insecure.  Please do not use this.");

        return wrapperTransport;
    }
}
