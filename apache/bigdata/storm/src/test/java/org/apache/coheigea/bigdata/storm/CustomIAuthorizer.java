/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.coheigea.bigdata.storm;

import java.util.Map;

import org.apache.storm.security.auth.IAuthorizer;
import org.apache.storm.security.auth.ReqContext;

/**
 * A trivial custom IAuthorizer that allows access to "alice"
 */
public class CustomIAuthorizer implements IAuthorizer {

    @Override
    public boolean permit(ReqContext context, String operation, Map topology_conf) {
        if (context.principal() == null) {
            return false;
        }
        if ("alice".equals(context.principal().getName())) {
            return true;
        }
        return false;
    }

    @Override
    public void prepare(Map storm_conf) {
    }
}

