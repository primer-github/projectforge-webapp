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

package org.projectforge.web.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * This class is an helper class for supporting the implementation of gui lists. The user can select and unselect entries. This will be
 * needed e. g. for assigning and unassigning user to one group.<br>
 * Finally, after the user has made his decisions (multiple assigning and/or multiple unassigning), this class will return the elements to
 * (un)assign by comparing with the original assigned values.
 */
public class AssignListHelper<T> implements Serializable
{
  private static final long serialVersionUID = 3522022033150328877L;

  private SortedSet<T> assignedItems;

  private final Collection<T> originalAssignedList;

  private final Collection<T> fullList;

  /**
   * Initializes the lists.
   * @param fullList List of all elements available for (un)assigning.
   * @param assignedKeys List of already assigned elements (by key) or null if no elements assigned.
   */
  public AssignListHelper(final SortedSet<T> fullList, final SortedSet<T> assignedItems)
  {
    this.fullList = fullList;
    this.assignedItems = assignedItems;
    this.originalAssignedList = assignedItems;
  }

  /**
   * @return the assignedItems
   */
  public SortedSet<T> getAssignedItems()
  {
    return assignedItems;
  }

  public AssignListHelper<T> setAssignedItems(final SortedSet<T> assignedItems)
  {
    this.assignedItems = assignedItems;
    return this;
  }

  /**
   * @return the fullList
   */
  public Collection<T> getFullList()
  {
    return fullList;
  }

  public Set<T> getItemsToAssign()
  {
    final Set<T> result = new HashSet<T>();
    if (assignedItems == null) {
      return result;
    }
    for (final T item : assignedItems) {
      if (originalAssignedList == null || originalAssignedList.contains(item) == false) {
        result.add(item);
      }
    }
    return result;
  }

  public Set<T> getItemsToUnassign()
  {
    final Set<T> result = new HashSet<T>();
    if (originalAssignedList == null) {
      return result;
    }
    for (final T item : originalAssignedList) {
      if (assignedItems.contains(item) == false) {
        result.add(item);
      }
    }
    return result;
  }
}