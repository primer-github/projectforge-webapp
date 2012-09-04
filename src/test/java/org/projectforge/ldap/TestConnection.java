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

import java.util.List;

import org.projectforge.address.AddressDO;
import org.projectforge.user.PFUserDO;

/**
 * @author kai
 * 
 */
public class TestConnection
{
  // ToDo: username als nur durch Admins änderbar konfigurierbar
  // Config für LDAP nur read only/schreibbar
  // Password im LDAP neu setzen (alle über Zwangs-Login schicken).

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TestConnection.class);

  public static void main(final String[] args) throws Exception
  {
    new TestConnection().run();
  }

  private void run() throws Exception
  {
    final LdapConfig cfg = LdapRealTestBase.readConfig();
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
    odao.createIfNotExist("pf-test", "Test organizational unit for testing ProjectForge.", "users");

    final LdapUserDao udao = new LdapUserDao();
    udao.ldapConnector = ldapConnector;
    final List<LdapPerson> list = udao.findAll("pf-test", "users");
    log.info("Found " + list.size() + " person entries.");
    final PFUserDO pfUser = new PFUserDO().setLastname("Meier").setFirstname("Horst").setUsername("h.meier")
        .setDescription("Test entry from ProjectForge dev system.").setEmail("h.meier@mail.com");
    pfUser.setId(42);
    final LdapPerson user = PFUserDOConverter.convert(pfUser);
    user.setOrganizationalUnit("pf-test", "users");
    udao.createOrUpdate(user);
    udao.changePassword(user, null, "test");
    udao.authenticate("h.meier", "test", "pf-test", "users");
    udao.changePassword(user, null, "hurzel");
    if (udao.authenticate("h.meier", "test", "pf-test", "users") == true) {
      throw new RuntimeException("Login is possible but it shouldn't!");
    }
    user.setSurname("Changed");
    user.setMail("h.meier@micromata.de");
    udao.update(user);
    user.setSurname("Meier");
    udao.update(user);
    if (udao.authenticate("h.meier", "hurzel", "pf-test", "users") == false) {
      throw new RuntimeException("Login should be possible");
    }
    // udao.deactivateUser(user);
    // if (udao.authenticate("h.meier", "hurzel", "pf-test", "users") == true) {
    // throw new RuntimeException("Login is possible but it shouldn't!");
    // }
    // if (udao.authenticate("h.meier", "", "pf-test", "users") == true) {
    // throw new RuntimeException("Login is possible with empty password but it shouldn't!");
    // }

    odao.createIfNotExist("pf-test", "Test organizational unit for testing ProjectForge.", "contacts");
    final LdapPersonDao adao = new LdapPersonDao();
    adao.ldapConnector = ldapConnector;
    final AddressDO pfAddress = new AddressDO().setFirstName("Kai").setName("Reinhard").setOrganization("Micromata GmbH")
        .setEmail("k.reinhard@micromata.de").setPrivateEmail("k.reinhard@me.com").setBusinessPhone("+49 561 316793-0")
        .setMobilePhone("+49 170 1891142").setPrivatePhone("+49 561 00000");
    pfAddress.setId(2);
    LdapPerson contact = AddressDOConverter.convert(pfAddress);
    contact.setOrganizationalUnit("pf-test", "contacts");
    adao.createOrUpdate(contact);
    contact = adao.findById(AddressDOConverter.UID_PREFIX + "2", "pf-test", "contacts");
    log.info("Found address with id=" + AddressDOConverter.UID_PREFIX + "2: " + contact);

    odao.createIfNotExist("pf-test", "Test organizational unit for testing ProjectForge.", "groups");
    final LdapGroupDao gdao = new LdapGroupDao();
    gdao.ldapConnector = ldapConnector;
    final LdapGroup group = new LdapGroup().setGidNumber(42).setDescription("Test by ProjectForge").setOrganization("www.projectforge.org");
    group.setCommonName("ProjectForge-test").setOrganizationalUnit("pf-test", "groups");
    group.addMember(contact);
    group.addMember(user);
    gdao.createOrUpdate(group);
  }
}
