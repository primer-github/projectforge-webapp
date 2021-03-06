/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.memo;

import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * Your plugin initialization. Register all your components such as i18n files, data-access object etc.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MemoPlugin extends AbstractPlugin
{
  public static final String ID = "memo";

  public static final String RESOURCE_BUNDLE_NAME = MemoPlugin.class.getPackage().getName() + ".MemoI18nResources";

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { MemoDO.class};

  private MemoDao memoDao;

  @Override
  public Class< ? >[] getPersistentEntities()
  {
    return PERSISTENT_ENTITIES;
  }

  @Override
  protected void initialize()
  {
    // DatabaseUpdateDao is needed by the updater:
    MemoPluginUpdates.dao = getDatabaseUpdateDao();
    // Register it:
    register(ID, MemoDao.class, memoDao, "plugins.memo");

    // Register the web part:
    registerWeb(ID, MemoListPage.class, MemoEditPage.class);

    // Register the menu entry as sub menu entry of the misc menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);
    registerMenuItem(new MenuItemDef(parentMenu, ID, 10, "plugins.memo.menu", MemoListPage.class));

    // Define the access management:
    registerRight(new MemoRight());

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  public void setMemoDao(final MemoDao memoDao)
  {
    this.memoDao = memoDao;
  }

  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return MemoPluginUpdates.getInitializationUpdateEntry();
  }
}
