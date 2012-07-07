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

package org.projectforge.plugins.teamcal;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TeamCalPlugin extends AbstractPlugin
{
  public static final String ID = "teamCalendars";

  public static final String RESOURCE_BUNDLE_NAME = TeamCalPlugin.class.getPackage().getName() + ".TeamCalI18nResources";

  static UserPrefArea USER_PREF_AREA;

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { CalendarDO.class, EventDO.class};

  /**
   * This dao should be defined in pluginContext.xml (as resources) for proper initialization.
   */
  private CalendarDao calendarDao;

  @Override
  public Class< ? >[] getPersistentEntities()
  {
    return PERSISTENT_ENTITIES;
  }

  /**
   * @see org.projectforge.plugins.core.AbstractPlugin#initialize()
   */
  @Override
  protected void initialize()
  {
    // DatabaseUpdateDao is needed by the updater:
    TeamCalPluginUpdates.dao = databaseUpdateDao;
    final RegistryEntry entry = new RegistryEntry(ID, CalendarDao.class, calendarDao, "plugins.teamcal");
    // The CalendarDao is automatically available by the scripting engine!
    register(entry);

    // Register the web part:
    //registerWeb(ID, CalendarListPage.class, CalendarEditPage.class);

    // Register the menu entry as sub menu entry of the misc menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);
    //registerMenuItem(new MenuItemDef(parentMenu, ID, 10, "plugins.teamcal.menu", CalendarListPage.class));
    // .setMobileMenu(ToDoMobileListPage.class, 10));

    // Define the access management:
    registerRight(new CalendarRight());

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  /**
   * @param calendarDao the calendarDao to set
   * @return this for chaining.
   */
  public void setCalendarDao(final CalendarDao calendarDao)
  {
    this.calendarDao = calendarDao;
  }

  /**
   * @see org.projectforge.plugins.core.AbstractPlugin#getInitializationUpdateEntry()
   */
  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return TeamCalPluginUpdates.getInitializationUpdateEntry();
  }
}