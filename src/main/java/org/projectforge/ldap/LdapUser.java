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

package org.projectforge.ldap;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapUser extends LdapPerson
{
  private Integer uidNumber, gidNumber;

  private String loginShell, homeDirectory;

  /**
   * @return The uid number of object class posixAccount.
   */
  public Integer getUidNumber()
  {
    return uidNumber;
  }

  public LdapUser setUidNumber(final Integer uidNumber)
  {
    this.uidNumber = uidNumber;
    return this;
  }

  /**
   * @return The gid number of object class posixAccount.
   */
  public Integer getGidNumber()
  {
    return gidNumber;
  }

  public LdapUser setGidNumber(final Integer gidNumber)
  {
    this.gidNumber = gidNumber;
    return this;
  }

  /**
   * @return The login shell of object class posixAccount.
   */
  public String getLoginShell()
  {
    return loginShell;
  }

  public LdapUser setLoginShell(final String loginShell)
  {
    this.loginShell = loginShell;
    return this;
  }

  /**
   * @return The home directory of object class posixAccount.
   */
  public String getHomeDirectory()
  {
    return homeDirectory;
  }

  public LdapUser setHomeDirectory(final String homeDirectory)
  {
    this.homeDirectory = homeDirectory;
    return this;
  }
}