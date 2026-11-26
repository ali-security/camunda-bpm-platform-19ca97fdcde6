/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.client.variable.impl.format.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.camunda.bpm.client.exception.DataFormatException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Element;

public class DomXmlDataFormatXxeTest {

  protected static final String SECRET = "top-secret-content";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void shouldParsePlainXml() {
    DomXmlDataFormat dataFormat = new DomXmlDataFormat("application/xml");

    Element element = dataFormat.readAsElement("<customer><name>foo</name></customer>");

    assertThat(element.getTagName()).isEqualTo("customer");
    assertThat(element.getTextContent()).isEqualTo("foo");
  }

  @Test
  public void shouldNotResolveExternalEntities() throws Exception {
    File secret = temporaryFolder.newFile("secret.txt");
    Files.write(secret.toPath(), SECRET.getBytes(StandardCharsets.UTF_8));

    String xml = "<?xml version=\"1.0\"?>"
        + "<!DOCTYPE customer [<!ENTITY xxe SYSTEM \"" + secret.toURI() + "\">]>"
        + "<customer><name>&xxe;</name></customer>";

    DomXmlDataFormat dataFormat = new DomXmlDataFormat("application/xml");

    assertThatThrownBy(() -> dataFormat.readAsElement(xml))
        .isInstanceOf(DataFormatException.class);
  }

  @Test
  public void shouldRejectDoctypeDeclarationOnReadValue() throws Exception {
    File secret = temporaryFolder.newFile("secret2.txt");
    Files.write(secret.toPath(), SECRET.getBytes(StandardCharsets.UTF_8));

    String xml = "<?xml version=\"1.0\"?>"
        + "<!DOCTYPE customer [<!ENTITY xxe SYSTEM \"" + secret.toURI() + "\">]>"
        + "<customer><name>&xxe;</name></customer>";

    DomXmlDataFormat dataFormat = new DomXmlDataFormat("application/xml");

    assertThatThrownBy(() -> dataFormat.readValue(xml, Object.class.getName()))
        .isInstanceOf(DataFormatException.class);
  }

}
