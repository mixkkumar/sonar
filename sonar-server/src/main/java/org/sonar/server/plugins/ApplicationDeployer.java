/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.server.plugins;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.platform.PluginMetadata;
import org.sonar.api.platform.PluginRepository;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.server.plugins.ClassLoaderUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

/**
 * Ruby on Rails requires the files to be on filesystem but not in Java classpath (JAR). This component extracts
 * all the needed files from plugins and copy them to $SONAR_HOME/temp
 *
 * @since 2.15
 */
public class ApplicationDeployer {
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationDeployer.class);
  private static final String ROR_PATH = "org/sonar/ror/";

  private ServerFileSystem fileSystem;
  private PluginRepository pluginRepository;

  public ApplicationDeployer(ServerFileSystem fileSystem, PluginRepository pluginRepository) {
    this.fileSystem = fileSystem;
    this.pluginRepository = pluginRepository;
  }

  public void start() throws IOException {
    deployRubyRailsApps();
  }

  private void deployRubyRailsApps() {
    LOG.info("Deploy Ruby on Rails applications");
    File appsDir = prepareRubyRailsRootDirectory();

    for (PluginMetadata pluginMetadata : pluginRepository.getMetadata()) {
      String pluginKey = pluginMetadata.getKey();
      try {
        deployRubyRailsApp(appsDir, pluginKey, pluginRepository.getPlugin(pluginKey).getClass().getClassLoader());
      } catch (Exception e) {
        throw new IllegalStateException("Fail to deploy Ruby on Rails application: " + pluginKey, e);
      }
    }
  }

  @VisibleForTesting
  File prepareRubyRailsRootDirectory() {
    File appsDir = new File(fileSystem.getTempDir(), "ror");
    prepareDir(appsDir);
    return appsDir;
  }

  @VisibleForTesting
  static void deployRubyRailsApp(File appsDir, final String pluginKey, ClassLoader appClassLoader) {
    if (hasRubyRailsApp(pluginKey, appClassLoader)) {
      LOG.info("Deploy app: " + pluginKey);
      File appDir = new File(appsDir, pluginKey);
      ClassLoaderUtils.copyResources(appClassLoader, ROR_PATH + pluginKey, appDir, new Function<String, String>() {
        @Override
        public String apply(@Nullable String relativePath) {
          // Relocate the deployed files :
          // relativePath format is: org/sonar/ror/sqale/app/controllers/foo_controller.rb
          // app path is: org/sonar/ror/sqale
          // -> deployed file is app/controllers/foo_controller.rb
          return StringUtils.substringAfter(relativePath, pluginKey + "/");
        }
      });
    }
  }

  @VisibleForTesting
  static boolean hasRubyRailsApp(String pluginKey, ClassLoader classLoader) {
    return classLoader.getResource(ROR_PATH + pluginKey) != null;

  }

  private void prepareDir(File appsDir) {
    if (appsDir.exists() && appsDir.isDirectory()) {
      try {
        FileUtils.deleteDirectory(appsDir);
      } catch (IOException e) {
        throw new IllegalStateException("Fail to delete temp directory: " + appsDir);
      }
    }
    try {
      FileUtils.forceMkdir(appsDir);
    } catch (IOException e) {
      throw new IllegalStateException("Fail to create temp directory: " + appsDir);
    }
  }
}