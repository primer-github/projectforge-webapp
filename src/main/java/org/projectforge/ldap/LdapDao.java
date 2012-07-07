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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class LdapDao<T extends LdapObject>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapDao.class);

  LdapConnector ldapConnector;

  protected abstract String getObjectClass();

  public void create(final T obj, final Object... args)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final String dn = buildDn(obj);
        log.info("Create " + getObjectClass() + ": " + dn + ": " + getLogInfo(obj));
        final Attributes attrs = getAttributesToBind(obj);
        LdapUtils.putAttribute(attrs, "cn", obj.getCommonName());
        onBeforeBind(dn, attrs, args);
        ctx.bind(dn, null, attrs);
        return null;
      }
    }.excecute();
  }

  protected void onBeforeBind(final String dn, final Attributes attrs, final Object... args)
  {
    // Do nothing at default.
  }

  /**
   * Please do not use this method for bulk updates, use {@link #createOrUpdate(Set, Object, Object...)} instead! Calls
   * {@link #getSetOfAllObjects()} before creation or update.
   * @param obj
   * @see #createOrUpdate(Set, Object, Object...)
   */
  public void createOrUpdate(final T obj, final Object... args)
  {
    createOrUpdate(getSetOfAllObjects(), obj, args);
  }

  /**
   * Calls {@link #create(Object)} if the object isn't part of the given set, otherwise {@link #update(Object)}.
   * @param setOfAllLdapObjects List generated before via {@link #getSetOfAllObjects()}.
   * @param obj
   */
  public void createOrUpdate(final Set<String> setOfAllLdapObjects, final T obj, final Object... args)
  {
    final String dn = buildDn(obj);
    if (setOfAllLdapObjects.contains(dn) == true) {
      update(obj, args);
    } else {
      create(obj, args);
    }
  }

  public void update(final T obj, final Object... objs)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final String dn = buildDn(obj);
        log.info("Update " + getObjectClass() + ": " + dn + ": " + getLogInfo(obj));
        final Attributes attrs = getAttributesToBind(obj);
        LdapUtils.putAttribute(attrs, "cn", obj.getCommonName());
        onBeforeRebind(dn, attrs, objs);
        ctx.rebind(dn, DirContext.ADD_ATTRIBUTE, attrs);
        return null;
      }
    }.excecute();
  }

  public void modify(final T obj, final ModificationItem... modificationItems)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final String dn = buildDn(obj);
        log.info("Modify attributes of " + getObjectClass() + ": " + dn + ": " + getLogInfo(obj));
        ctx.modifyAttributes(dn, modificationItems);
        return null;
      }
    }.excecute();
  }

  protected String getLogInfo(final T obj)
  {
    return String.valueOf(obj);
  }

  protected void onBeforeRebind(final String dn, final Attributes attrs, final Object... objs)
  {
    // Do nothing at default;
  }

  public void delete(final T obj)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final String dn = buildDn(obj);
        log.info("Delete " + getObjectClass() + ": " + dn + ": " + getLogInfo(obj));
        ctx.unbind(dn);
        return null;
      }
    }.excecute();
  }

  @SuppressWarnings("unchecked")
  public List<T> findAll(final String... organizationalUnit)
  {
    return (List<T>) new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final LinkedList<T> list = new LinkedList<T>();
        NamingEnumeration< ? > results = null;
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        final String searchBase = LdapUtils.getOu(organizationalUnit);
        results = ctx.search(searchBase, "(objectclass=" + getObjectClass() + ")", controls);
        while (results.hasMore()) {
          final SearchResult searchResult = (SearchResult) results.next();
          final String dn = searchResult.getName();
          final Attributes attributes = searchResult.getAttributes();
          list.add(mapToObject(dn, searchBase, attributes));
        }
        return list;
      }
    }.excecute();
  }

  @SuppressWarnings("unchecked")
  public T findByUid(final String uid, final String... organizationalUnit)
  {
    return (T) new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        NamingEnumeration< ? > results = null;
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        final String searchBase = LdapUtils.getOu(organizationalUnit);
        results = ctx.search(searchBase, "(&(objectClass=" + getObjectClass() + ")(uid=" + uid + "))", controls);
        if (results.hasMore() == false) {
          return null;
        }
        final SearchResult searchResult = (SearchResult) results.next();
        final String dn = searchResult.getName();
        final Attributes attributes = searchResult.getAttributes();
        if (results.hasMore() == true) {
          log.error("Oups, found entries with multiple uids: " + getObjectClass() + "." + uid);
        }
        return mapToObject(dn, searchBase, attributes);
      }
    }.excecute();
  }

  /**
   * Set of all objects (the string is built from the method {@link #buildDn(Object)}).
   */
  public Set<String> getSetOfAllObjects()
  {
    final List<T> all = findAll();
    final Set<String> set = new HashSet<String>();
    for (final T obj : all) {
      if (log.isDebugEnabled() == true) {
        log.debug("Adding: " + obj.getDn());
      }
      set.add(obj.getDn());
    }
    return set;
  }

  protected String buildDn(final T obj)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("cn=").append(obj.getCommonName());
    if (obj.getOrganizationalUnit() != null) {
      buf.append(',');
    }
    LdapUtils.buildOu(buf, obj.getOrganizationalUnit());
    obj.setDn(buf.toString());
    return obj.getDn();
  }

  protected abstract Attributes getAttributesToBind(final T obj);

  protected T mapToObject(final String dn, final String searchBase, final Attributes attributes) throws NamingException
  {
    final T obj = mapToObject(attributes);
    if (StringUtils.isNotBlank(searchBase) == true) {
      obj.setDn(dn + "," + searchBase);
    } else {
      obj.setDn(dn);
    }
    obj.setOrganizationalUnit(LdapUtils.getOrganizationalUnit(dn, searchBase));
    obj.setCommonName(LdapUtils.getAttribute(attributes, "cn"));
    return obj;
  }

  /**
   * 
   * @param dn
   * @param attributes
   * @return
   * @throws NamingException
   */
  protected abstract T mapToObject(final Attributes attributes) throws NamingException;

  public void setLdapConnector(final LdapConnector ldapConnector)
  {
    this.ldapConnector = ldapConnector;
  }
}