/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

/**
 * 
 */
package org.projectforge.ldap;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.projectforge.address.AddressDO;
import org.projectforge.fibu.kost.AccountingConfig;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlObjectReader;

/**
 * @author kai
 * 
 */
public class TestConnection
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TestConnection.class);

  private static final String CONFIG_FILE = System.getProperty("user.home") + "/ProjectForge/testldapConfig.xml";

  public static void main(final String[] args) throws Exception
  {
    new TestConnection().run();
  }

  private void run() throws Exception
  {
    final LdapConfig cfg = readConfig();
    final LdapConnector ldapConnector = new LdapConnector(cfg);
    // new LdapTemplate(ldapConnector) {
    // @Override
    // protected Object call() throws NameNotFoundException, NamingException
    // {
    // // Read supportedSASLMechanisms from root DSE
    // final Attributes attrs = ctx.getAttributes(cfg.getUrl(), new String[] { "supportedSASLMechanisms"});
    //

    final LdapOrganizationalUnitDao odao = new LdapOrganizationalUnitDao();
    odao.ldapConnector = ldapConnector;
    odao.create("pf-test", "Test organizational unit for testing ProjectForge.", "users");

    final LdapUserDao pdao = new LdapUserDao();
    pdao.ldapConnector = ldapConnector;
    final List<LdapUser> list = pdao.findAll();
    log.info("Found " + list.size() + " person entries.");
    final LdapUser person = new LdapUser().setUid("42").setSurname("Meier").setGivenName("Horst")
        .setDescription("Test entry from ProjectForge dev system.").setMail("h.meier@mail.com");
    person.setCommonName("h.meier").setOrganizationalUnit("pf-test", "users");
    pdao.createOrUpdate(person, "password");
    person.setSurname("Changed");
    pdao.update(person);
    pdao.changePassword(person, "hurzel");
    // pdao.delete(person);

    final LdapPersonDao adao = new LdapPersonDao();
    adao.ldapConnector = ldapConnector;
    final AddressDO adr = new AddressDO().setFirstName("Kai").setName("Reinhard").setOrganization("Micromata GmbH")
        .setEmail("k.reinhard@micromata.de").setPrivateEmail("k.reinhard@me.com").setBusinessPhone("+49 561 316793-0")
        .setMobilePhone("+49 170 1891142").setPrivatePhone("+49 561 00000");
    adr.setId(2);
    LdapPerson ldapAddress = new LdapPerson(adr);
    ldapAddress.setOrganizationalUnit("contacts");
    adao.createOrUpdate(ldapAddress);
    ldapAddress = adao.findByUid(LdapPerson.UID_PREFIX + "2", "contacts");
    log.info("Found address with id=" + LdapPerson.UID_PREFIX + "2: " + ldapAddress);

    final LdapGroupDao gdao = new LdapGroupDao();
    gdao.ldapConnector = ldapConnector;
    final LdapGroup group = new LdapGroup().setDescription("Test by ProjectForge");
    group.setCommonName("ProjectForge-test").setOrganizationalUnit("groups");
    group.addMember(ldapAddress);
    group.addMember(person);
    gdao.createOrUpdate(group);
  }

  private LdapConfig readConfig()
  {
    final File configFile = new File(CONFIG_FILE);
    if (configFile.canRead() == false) {
      throw new IllegalArgumentException("Cannot read from config file: '" + CONFIG_FILE + "'.");
    }
    final XmlObjectReader reader = new XmlObjectReader();
    final AliasMap aliasMap = new AliasMap();
    aliasMap.put(LdapConfig.class, "ldapConfig");
    reader.setAliasMap(aliasMap);
    // reader.initialize(ConfigXml.class);
    AccountingConfig.registerXmlObjects(reader, aliasMap);
    String xml = null;
    try {
      xml = FileUtils.readFileToString(configFile, "UTF-8");
    } catch (final IOException ex) {
      ex.printStackTrace(System.err);
      throw new IllegalArgumentException("Cannot read config file '" + CONFIG_FILE + "' properly : " + ex.getMessage(), ex);
    }
    if (xml == null) {
      throw new IllegalArgumentException("Cannot read from config file: '" + CONFIG_FILE + "'.");
    }
    try {
      final LdapConfig cfg = (LdapConfig) reader.read(xml);
      final String warnings = reader.getWarnings();
      if (StringUtils.isNotBlank(warnings) == true) {
        System.err.println(warnings);
      }
      return cfg;
    } catch (final Throwable ex) {
      throw new IllegalArgumentException("Cannot read config file '" + CONFIG_FILE + "' properly : " + ex.getMessage(), ex);
    }
  }
}