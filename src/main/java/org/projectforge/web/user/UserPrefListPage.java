/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.UserPrefArea;
import org.projectforge.user.UserPrefAreaRegistry;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserPrefDao;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = UserPrefEditPage.class)
public class UserPrefListPage extends AbstractListPage<UserPrefListForm, UserPrefDao, UserPrefDO>
{
  private static final long serialVersionUID = 6121734373079865758L;

  @SpringBean(name = "userPrefDao")
  private UserPrefDao userPrefDao;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  public static BookmarkablePageLink<Void> createLink(final String id, final UserPrefArea area)
  {
    final PageParameters params = new PageParameters();
    params.add("area", area.getId());
    final BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(id, UserPrefListPage.class, params);
    return link;
  }

  public UserPrefListPage(final PageParameters parameters)
  {
    super(parameters, "user.pref");
    final String area = parameters.getString("area");
    if (area != null) {
      final UserPrefArea userPrefArea = UserPrefAreaRegistry.instance().getEntry(area);
      form.getSearchFilter().setArea(userPrefArea);
    }
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    final List<IColumn<UserPrefDO>> columns = new ArrayList<IColumn<UserPrefDO>>();

    final CellItemListener<UserPrefDO> cellItemListener = new CellItemListener<UserPrefDO>() {
      public void populateItem(final Item<ICellPopulator<UserPrefDO>> item, final String componentId, final IModel<UserPrefDO> rowModel)
      {
      }
    };
    columns.add(new CellItemListenerPropertyColumn<UserPrefDO>(new Model<String>(getString("user.pref.area")), "area", "area",
        cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final UserPrefDO userPref = (UserPrefDO) rowModel.getObject();
        final String label;
        if (userPref.getArea() != null) {
          label = getString(userPref.getArea().getI18nKey());
        } else {
          label = "";
        }
        item.add(new ListSelectActionPanel(componentId, rowModel, UserPrefEditPage.class, userPref.getId(), UserPrefListPage.this, label));
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<UserPrefDO>(new Model<String>(getString("user.pref.name")), "name", "name",
        cellItemListener));
    columns.add(new UserPropertyColumn<UserPrefDO>(getString("user"), "user.fullname", "user", cellItemListener)
        .withUserFormatter(userFormatter));
    columns.add(new CellItemListenerPropertyColumn<UserPrefDO>(new Model<String>(getString("filter.lastModified")), "lastUpdate",
        "lastUpdate", cellItemListener));
    dataTable = createDataTable(columns, null, false);
    form.add(dataTable);
  }

  /**
   * Gets the current area and preset this area for the edit page.
   * @see org.projectforge.web.wicket.AbstractListPage#onNewEntryClick(org.apache.wicket.PageParameters)
   */
  @Override
  protected AbstractEditPage< ? , ? , ? > redirectToEditPage(PageParameters params)
  {
    if (params == null) {
      params = new PageParameters();
    }
    final UserPrefArea area = form.getSearchFilter().getArea();
    if (area != null) {
      params.add(UserPrefEditPage.PARAMETER_AREA, area.getId());
    }
    return super.redirectToEditPage(params);
  }

  @Override
  protected UserPrefListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new UserPrefListForm(this);
  }

  @Override
  protected UserPrefDao getBaseDao()
  {
    return userPrefDao;
  }

  @Override
  protected IModel<UserPrefDO> getModel(final UserPrefDO object)
  {
    return new DetachableDOModel<UserPrefDO, UserPrefDao>(object, getBaseDao());
  }
}
