/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.poll.event;

import java.util.Collection;

import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.EventSource;
import net.ftlines.wicket.fullcalendar.callback.ClickedEvent;
import net.ftlines.wicket.fullcalendar.callback.DroppedEvent;
import net.ftlines.wicket.fullcalendar.callback.ResizedEvent;
import net.ftlines.wicket.fullcalendar.callback.SelectedRange;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.poll.PollDO;
import org.projectforge.plugins.poll.attendee.PollAttendeePage;
import org.projectforge.web.calendar.MyFullCalendar;
import org.projectforge.web.calendar.MyFullCalendarConfig;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.components.SingleButtonPanel;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class PollEventEditPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = 2988767055605267801L;

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PollEventEditPage.class);

  private MyFullCalendarConfig config;

  private MyFullCalendar calendar;

  private final IModel<PollDO> pollDoModel;

  private Collection<PollEventDO> events;

  private RepeatingView eventEntries;

  public PollEventEditPage(final PageParameters parameters, IModel<PollDO> pollDoModel)
  {
    super(parameters);
    this.pollDoModel = pollDoModel;
  }

  public PollEventEditPage(final PageParameters parameters, IModel<PollDO> pollDoModel, Collection<PollEventDO> events)
  {
    super(parameters);
    this.pollDoModel = pollDoModel;
    this.events = events;
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    final Form<Void> form = new Form<Void>("form");
    body.add(form);

    form.add(new Label("title", pollDoModel.getObject().getTitle()));
    form.add(new Label("location", pollDoModel.getObject().getLocation()));
    eventEntries = new RepeatingView("eventEntries");
    eventEntries.setOutputMarkupId(true);
    form.add(eventEntries);

    final PollEventEventsProvider eventProvider = new PollEventEventsProvider(this, pollDoModel);
    if (events != null) {
      if (events.isEmpty() == false) {
        for (PollEventDO event : events) {
          eventProvider.addEvent(new SelectedRange(event.getStartDate(), event.getEndDate(), false), null);
        }
      }
    }
    config = new MyFullCalendarConfig(this);
    config.setSelectable(true);
    config.setEditable(true);
    config.setSelectHelper(true);
    config.setDefaultView("agendaWeek");
    config.getHeader().setRight("");
    config.setEnableContextMenu(false);
    config.setLoading("function(bool) { if (bool) $(\"#loading\").show(); else $(\"#loading\").hide(); }");
    calendar = new MyFullCalendar("cal", config) {
      private static final long serialVersionUID = -6819899072933690316L;

      /**
       * @see net.ftlines.wicket.fullcalendar.FullCalendar#onDateRangeSelected(net.ftlines.wicket.fullcalendar.callback.SelectedRange,
       *      net.ftlines.wicket.fullcalendar.CalendarResponse)
       */
      @Override
      protected void onDateRangeSelected(final SelectedRange range, final CalendarResponse response)
      {
        eventProvider.addEvent(range, response);
        IModel<SelectedRange> model = new IModel<SelectedRange>() {

          @Override
          public void detach()
          {
          }

          @Override
          public void setObject(SelectedRange object)
          {
          }

          @Override
          public SelectedRange getObject()
          {
            return range;
          }
        };
        PollEventEntryPanel entry = new PollEventEntryPanel(eventEntries.newChildId(), model);
        eventEntries.add(entry);
        AjaxRequestTarget.get().add(form);
      }

      /**
       * @see net.ftlines.wicket.fullcalendar.FullCalendar#onEventResized(net.ftlines.wicket.fullcalendar.callback.ResizedEvent,
       *      net.ftlines.wicket.fullcalendar.CalendarResponse)
       */
      @Override
      protected boolean onEventResized(final ResizedEvent event, final CalendarResponse response)
      {
        return eventProvider.resizeEvent(event, response);
      }

      /**
       * @see net.ftlines.wicket.fullcalendar.FullCalendar#onEventDropped(net.ftlines.wicket.fullcalendar.callback.DroppedEvent,
       *      net.ftlines.wicket.fullcalendar.CalendarResponse)
       */
      @Override
      protected boolean onEventDropped(final DroppedEvent event, final CalendarResponse response)
      {
        return eventProvider.dropEvent(event, response);
      }

      /**
       * @see net.ftlines.wicket.fullcalendar.FullCalendar#onEventClicked(net.ftlines.wicket.fullcalendar.callback.ClickedEvent,
       *      net.ftlines.wicket.fullcalendar.CalendarResponse)
       */
      @Override
      protected void onEventClicked(final ClickedEvent event, final CalendarResponse response)
      {
        eventProvider.eventClicked(event, response);
      }
    };
    calendar.setMarkupId("calendar");
    final EventSource eventSource = new EventSource();
    eventSource.setEventsProvider(eventProvider);
    config.add(eventSource);
    form.add(calendar);
    final Button nextButton = new Button(SingleButtonPanel.WICKET_ID) {
      private static final long serialVersionUID = -7779593314951993472L;

      @Override
      public final void onSubmit()
      {
        onNextButtonClick(pollDoModel.getObject(), eventProvider.getAllEvents());
      }
    };
    nextButton.setDefaultFormProcessing(false);
    final SingleButtonPanel nextButtonPanel = new SingleButtonPanel("continueButton", nextButton, getString("next"),
        SingleButtonPanel.DEFAULT_SUBMIT);
    form.add(nextButtonPanel);
  }

  /**
   * @param allEvents
   */
  protected void onNextButtonClick(final PollDO pollDo, final Collection<PollEventDO> allEvents)
  {
    setResponsePage(new PollAttendeePage(getPageParameters(), pollDo, allEvents));
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.poll.title");
  }

}