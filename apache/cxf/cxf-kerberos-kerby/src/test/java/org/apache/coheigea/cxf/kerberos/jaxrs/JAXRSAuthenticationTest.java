/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.coheigea.cxf.kerberos.jaxrs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.coheigea.cxf.kerberos.common.KerbyServer;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.cxf.testutil.common.TestUtil;
import org.apache.cxf.transport.http.auth.SpnegoAuthSupplier;
import org.apache.kerby.kerberos.kdc.impl.NettyKdcServerImpl;
import org.apache.kerby.kerberos.kerb.KrbException;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.ietf.jgss.GSSName;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * This is a test-case that shows how to use Kerberos with a JAX-RS service.
 */

public class JAXRSAuthenticationTest extends org.junit.Assert {

    private static final String PORT = TestUtil.getPortNumber(Server.class);
    private static final String KDC_PORT = TestUtil.getPortNumber(Server.class, 2);
    private static final String KDC_UDP_PORT = TestUtil.getPortNumber(Server.class, 3);

    private static KerbyServer kerbyServer;

    @BeforeClass
    public static void setUp() throws Exception {

        WSSConfig.init();

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getCanonicalPath();
        }

        updatePort(basedir);

        // System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("java.security.auth.login.config", basedir + "/target/test-classes/kerberos/kerberos.jaas");

        Assert.assertTrue(
                          "Server failed to launch",
                          // run the server in the same process
                          // set this to false to fork
                          AbstractBusClientServerTestBase.launchServer(Server.class, true)
            );

        kerbyServer = new KerbyServer();

        kerbyServer.setKdcHost("localhost");
        kerbyServer.setKdcRealm("service.ws.apache.org");
        kerbyServer.setKdcTcpPort(Integer.parseInt(KDC_PORT));
        kerbyServer.setAllowUdp(true);
        kerbyServer.setKdcUdpPort(Integer.parseInt(KDC_UDP_PORT));

        kerbyServer.setInnerKdcImpl(new NettyKdcServerImpl(kerbyServer.getKdcSetting()));
        kerbyServer.init();

        // Create principals
        String alice = "alice@service.ws.apache.org";
        String bob = "bob/service.ws.apache.org@service.ws.apache.org";
        kerbyServer.createPrincipal(alice, "alice");
        kerbyServer.createPrincipal(bob, "bob");
        kerbyServer.createPrincipal("krbtgt/service.ws.apache.org@service.ws.apache.org", "krbtgt");
        kerbyServer.start();
    }

    @AfterClass
    public static void tearDown() throws KrbException {
        if (kerbyServer != null) {
            kerbyServer.stop();
        }
    }

    private static void updatePort(String basedir) throws Exception {

        // Read in krb5.conf and substitute in the correct port
        File f = new File(basedir + "/src/test/resources/kerberos/krb5.conf");

        FileInputStream inputStream = new FileInputStream(f);
        String content = IOUtils.toString(inputStream, "UTF-8");
        inputStream.close();
        // content = content.replaceAll("port", KDC_PORT);
        content = content.replaceAll("port", KDC_UDP_PORT);

        File f2 = new File(basedir + "/target/test-classes/kerberos/krb5.conf");
        FileOutputStream outputStream = new FileOutputStream(f2);
        IOUtils.write(content, outputStream, "UTF-8");
        outputStream.close();

        System.setProperty("java.security.krb5.conf", f2.getPath());
    }

    @org.junit.Test
    public void testKerberos() throws Exception {

        URL busFile = JAXRSAuthenticationTest.class.getResource("cxf-client.xml");

        String address = "https://localhost:" + PORT + "/doubleit/services";
        WebClient client = WebClient.create(address, busFile.toString());

        Map<String, Object> requestContext = WebClient.getConfig(client).getRequestContext();
        requestContext.put("auth.spnego.useKerberosOid", "true");

        SpnegoAuthSupplier authSupplier = new SpnegoAuthSupplier();
        authSupplier.setServicePrincipalName("bob@service.ws.apache.org");
        authSupplier.setServiceNameType(GSSName.NT_HOSTBASED_SERVICE);
        WebClient.getConfig(client).getHttpConduit().setAuthSupplier(authSupplier);

        Number numberToDouble = new Number();
        numberToDouble.setDescription("This is the number to double");
        numberToDouble.setNumber(25);

        Response response = client.post(numberToDouble);
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.readEntity(Number.class).getNumber(), 50);
    }

}
