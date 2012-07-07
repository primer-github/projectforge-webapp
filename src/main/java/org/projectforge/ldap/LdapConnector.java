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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang.StringUtils;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.ConfigurationListener;

/**
 * Should be initialized on start-up and will be called every time if config.xml is reread. This class is needed for initialization of the
 * spring beans with properties configured in config.xml.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapConnector implements ConfigurationListener
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapConnector.class);

  private LdapConfig ldapConfig;

  /** Don't call this constructor unless you really know what you're doing. This LdapHelper is a singleton and is available via IOC. */
  public LdapConnector()
  {
    ConfigXml.getInstance().register(this);
  }

  public DirContext createContext()
  {
    // Set up the environment for creating the initial context
    final Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, ldapConfig.getCompleteUrl());
    final String authentication = ldapConfig.getAuthentication();
    if (StringUtils.isNotBlank(authentication) == true) {
      env.put(Context.SECURITY_AUTHENTICATION, ldapConfig.getAuthentication());
      if ("none".equals(authentication) == false) {
        env.put(Context.SECURITY_PRINCIPAL, ldapConfig.getAdminUser());
        env.put(Context.SECURITY_CREDENTIALS, ldapConfig.getAdminPassword());
      }
    }
    log.info("Trying to connect the LDAP server: url=["
        + ldapConfig.getCompleteUrl()
        + "], authentication=["
        + ldapConfig.getAuthentication()
        + "], principal=["
        + ldapConfig.getAdminUser()
        + "]");
    // Create the initial context
    try {
      final DirContext ctx = new InitialDirContext(env);
      return ctx;
    } catch (final NamingException ex) {
      log.error("While trying to connect LDAP initally: " + ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
  }

  /**
   * Used by test class.
   * @param ldapConfig
   */
  LdapConnector(final LdapConfig ldapConfig)
  {
    this.ldapConfig = ldapConfig;
  }

  /**
   * @see org.projectforge.core.ConfigurationListener#afterRead()
   */
  @Override
  public void afterRead()
  {
    this.ldapConfig = ConfigXml.getInstance().getLdapConfig();
  }
}