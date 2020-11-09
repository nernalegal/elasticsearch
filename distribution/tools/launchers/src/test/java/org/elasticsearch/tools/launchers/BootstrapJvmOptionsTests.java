/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.tools.launchers;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.elasticsearch.tools.launchers.BootstrapJvmOptions.PluginInfo;

public class BootstrapJvmOptionsTests extends LaunchersTestCase {

    public void testGenerateOptionsHandlesNoPlugins() {
        final List<String> options = BootstrapJvmOptions.generateOptions(emptyList());
        assertThat(options, is(empty()));
    }

    public void testGenerateOptionsIgnoresNonBootstrapPlugins() {
        Properties props = new Properties();
        props.put("type", "isolated");
        List<PluginInfo> info = singletonList(new PluginInfo(emptyList(), props));

        final List<String> options = BootstrapJvmOptions.generateOptions(info);
        assertThat(options, is(empty()));
    }

    public void testGenerateOptionsHandlesBootstrapPlugins() {
        Properties propsWithoutJavaOpts = new Properties();
        propsWithoutJavaOpts.put("type", "bootstrap");
        PluginInfo info1 = new PluginInfo(singletonList("/path/first.jar"), propsWithoutJavaOpts);

        Properties propsWithEmptyJavaOpts = new Properties();
        propsWithEmptyJavaOpts.put("type", "bootstrap");
        propsWithEmptyJavaOpts.put("java.opts", "");
        PluginInfo info2 = new PluginInfo(singletonList("/path/second.jar"), propsWithEmptyJavaOpts);

        Properties propsWithBlankJavaOpts = new Properties();
        propsWithBlankJavaOpts.put("type", "bootstrap");
        propsWithBlankJavaOpts.put("java.opts", "   \t\n  ");
        PluginInfo info3 = new PluginInfo(singletonList("/path/third.jar"), propsWithBlankJavaOpts);

        Properties propsWithJavaOpts = new Properties();
        propsWithJavaOpts.put("type", "bootstrap");
        propsWithJavaOpts.put("java.opts", "-Dkey=value -DotherKey=otherValue");
        PluginInfo info4 = new PluginInfo(singletonList("/path/fourth.jar"), propsWithJavaOpts);

        final List<String> options = BootstrapJvmOptions.generateOptions(Arrays.asList(info1, info2, info3, info4));
        assertThat(
            options,
            contains(
                "-Dkey=value -DotherKey=otherValue",
                "-Xbootclasspath/a:/path/first.jar:/path/second.jar:/path/third.jar:/path/fourth.jar"
            )
        );
    }
}