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
package org.sonar.test.i18n;

import com.google.common.io.Closeables;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.utils.SonarException;
import org.sonar.test.TestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public final class RuleRepositoryTestHelper {
  private RuleRepositoryTestHelper() {
    // Static utility class
  }

  public static List<Rule> createRulesWithNameAndDescription(String plugin, RuleRepository repository) {
    Properties props = loadProperties(String.format("/org/sonar/l10n/%s.properties", plugin));

    List<Rule> rules = repository.createRules();
    for (Rule rule : rules) {
      String name = props.getProperty(String.format("rule.%s.%s.name", repository.getKey(), rule.getKey()));
      String description = TestUtils.getResourceContent(String.format("/org/sonar/l10n/%s/rules/%s/%s.html", plugin, repository.getKey(), rule.getKey()));

      rule.setName(name);
      rule.setDescription(description);
    }

    return rules;
  }

  private static Properties loadProperties(String resourcePath) {
    Properties properties = new Properties();

    InputStream input = null;
    try {
      input = TestUtils.class.getResourceAsStream(resourcePath);
      properties.load(input);
      return properties;
    } catch (IOException e) {
      throw new SonarException("Unable to read properties " + resourcePath, e);
    } finally {
      Closeables.closeQuietly(input);
    }
  }
}
