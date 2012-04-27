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

package org.projectforge.web.task;

import java.util.List;
import java.util.ListIterator;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.Hibernate;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskFavorite;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.FavoritesChoicePanel;
import org.projectforge.web.wicket.components.TooltipImage;
import org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel;

/**
 * Panel for showing and selecting one task.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TaskSelectPanel extends AbstractSelectPanel<TaskDO> implements ComponentWrapperPanel
{
  private static final long serialVersionUID = -7231190025292695850L;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  private boolean showPath = true;

  private final WebMarkupContainer divContainer;

  private RepeatingView ancestorRepeater;

  private Integer currentTaskId;

  public TaskSelectPanel(final String id, final IModel<TaskDO> model, final ISelectCallerPage caller, final String selectProperty)
  {
    super(id, model, caller, selectProperty);
    TaskDO task = model.getObject();
    if (Hibernate.isInitialized(task) == false) {
      task = taskTree.getTaskById(task.getId());
      model.setObject(task);
    }
    divContainer = new WebMarkupContainer("div");
    add(divContainer);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractSelectPanel#onBeforeRender()
   */
  @SuppressWarnings("serial")
  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    final TaskDO task = getModelObject();
    final Integer taskId = task != null ? task.getId() : null;
    if (currentTaskId == taskId) {
      return;
    }
    currentTaskId = taskId;
    if (showPath == true && task != null) {
      ancestorRepeater.removeAll();
      final TaskNode taskNode = taskTree.getTaskNodeById(task.getId());
      final List<Integer> ancestorIds = taskNode.getAncestorIds();
      final ListIterator<Integer> it = ancestorIds.listIterator(ancestorIds.size());
      while (it.hasPrevious() == true) {
        final Integer ancestorId = it.previous();
        final TaskDO ancestorTask = taskTree.getTaskById(ancestorId);
        if (ancestorTask.getParentTask() == null) {
          // Don't show root node:
          continue;
        }
        final WebMarkupContainer cont = new WebMarkupContainer(ancestorRepeater.newChildId());
        ancestorRepeater.add(cont);
        final SubmitLink selectTaskLink = new SubmitLink("ancestorTaskLink") {
          @Override
          public void onSubmit()
          {
            caller.select(selectProperty, ancestorTask.getId());
          }
        };
        selectTaskLink.setDefaultFormProcessing(false);
        cont.add(selectTaskLink);
        WicketUtils.addTooltip(selectTaskLink, getString("task.selectPanel.selectAncestorTask.tooltip"));
        selectTaskLink.add(new Label("name", ancestorTask.getTitle()));
      }
      ancestorRepeater.setVisible(true);
    } else {
      ancestorRepeater.setVisible(false);
    }
  }

  @Override
  @SuppressWarnings("serial")
  public TaskSelectPanel init()
  {
    super.init();
    ancestorRepeater = new RepeatingView("ancestorTasks");
    divContainer.add(ancestorRepeater);
    final SubmitLink taskLink = new SubmitLink("taskLink") {
      @Override
      public void onSubmit()
      {
        final TaskDO task = getModelObject();
        if (task == null) {
          return;
        }
        final PageParameters pageParams = new PageParameters();
        pageParams.add(AbstractEditPage.PARAMETER_KEY_ID, String.valueOf(task.getId()));
        final TaskEditPage editPage = new TaskEditPage(pageParams);
        editPage.setReturnToPage((AbstractSecuredPage) getPage());
        setResponsePage(editPage);
      }
    };
    taskLink.setDefaultFormProcessing(false);
    divContainer.add(taskLink);
    WicketUtils.addTooltip(taskLink, getString("task.selectPanel.displayTask.tooltip"));
    taskLink.add(new Label("name", new Model<String>() {
      /**
       * @see org.apache.wicket.model.Model#getObject()
       */
      @Override
      public String getObject()
      {
        final TaskDO task = getModelObject();
        return task != null ? task.getTitle() : "";
      }
    }));

    final SubmitLink selectButton = new SubmitLink("select") {
      @Override
      public void onSubmit()
      {
        final TaskTreePage taskTreePage = new TaskTreePage(caller, selectProperty);
        if (getModelObject() != null) {
          taskTreePage.setEventNode(getModelObject().getId()); // Preselect node for highlighting.
        }
        setResponsePage(taskTreePage);
      };
    };
    selectButton.setDefaultFormProcessing(false);
    divContainer.add(selectButton);
    selectButton.add(new TooltipImage("selectHelp", getResponse(), WebConstants.IMAGE_TASK_SELECT, getString("tooltip.selectTask")));
    final SubmitLink unselectButton = new SubmitLink("unselect") {
      @Override
      public void onSubmit()
      {
        caller.unselect(selectProperty);
      }

      @Override
      public boolean isVisible()
      {
        return isRequired() == false && getModelObject() != null;
      }
    };
    unselectButton.setDefaultFormProcessing(false);
    divContainer.add(unselectButton);
    unselectButton
    .add(new TooltipImage("unselectHelp", getResponse(), WebConstants.IMAGE_TASK_UNSELECT, getString("tooltip.unselectTask")));
    // DropDownChoice favorites
    final FavoritesChoicePanel<TaskDO, TaskFavorite> favoritesPanel = new FavoritesChoicePanel<TaskDO, TaskFavorite>("favorites",
        UserPrefArea.TASK_FAVORITE, tabIndex, "full text") {
      @Override
      protected void select(final TaskFavorite favorite)
      {
        if (favorite.getTask() != null) {
          TaskSelectPanel.this.selectTask(favorite.getTask());
        }
      }

      @Override
      protected TaskDO getCurrentObject()
      {
        return TaskSelectPanel.this.getModelObject();
      }

      @Override
      protected TaskFavorite newFavoriteInstance(final TaskDO currentObject)
      {
        final TaskFavorite favorite = new TaskFavorite();
        favorite.setTask(currentObject);
        return favorite;
      }
    };
    divContainer.add(favoritesPanel);
    favoritesPanel.init();
    if (showFavorites == false) {
      favoritesPanel.setVisible(false);
    }
    return this;
  }

  /**
   * Will be called if the user has chosen an entry of the task favorites drop down choice.
   * @param task
   */
  protected void selectTask(final TaskDO task)
  {
    setModelObject(task);
    caller.select(selectProperty, task.getId());
  }

  @Override
  public Component getClassModifierComponent()
  {
    return divContainer;
  }

  @Override
  protected void convertInput()
  {
    setConvertedInput(getModelObject());
  }

  /**
   * If true (default) then the path from the root task to the currently selected will be shown, otherwise only the name of the task is
   * displayed.
   * @param showPath
   */
  public void setShowPath(final boolean showPath)
  {
    this.showPath = showPath;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel#getComponentOutputId()
   */
  @Override
  public String getComponentOutputId()
  {
    divContainer.setOutputMarkupId(true);
    return divContainer.getMarkupId();
  }
}
