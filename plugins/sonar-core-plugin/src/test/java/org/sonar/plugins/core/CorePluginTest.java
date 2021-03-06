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
package org.sonar.plugins.core;

import org.junit.Test;
import org.reflections.Reflections;
import org.sonar.api.web.AbstractRubyTemplate;

import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

public class CorePluginTest {
  @Test
  public void should_define_many_extensions() {
    assertThat(new CorePlugin().getExtensions().size()).isGreaterThan(10);
  }

  @Test
  public void should_contain_all_core_widgets() {
    Set<Class<? extends AbstractRubyTemplate>> widgets = new Reflections("org.sonar.plugins.core.widgets").getSubTypesOf(AbstractRubyTemplate.class);

    assertThat(widgets).isNotEmpty();
    assertThat(new CorePlugin().getExtensions()).contains(widgets.toArray());
  }
}
