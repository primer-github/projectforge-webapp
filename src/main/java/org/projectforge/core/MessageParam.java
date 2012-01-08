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

package org.projectforge.core;

/**
 * I18n params are params for localized message which will be localized itself, if paramType == VALUE (default).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MessageParam
{
  private Object value;

  private MessageParamType paramType;

  /**
   */
  public MessageParam(Object value)
  {
    this.value = value;
    this.paramType = MessageParamType.VALUE;
  }

  /**
   * Will be interpreted as value.
   */
  public MessageParam(String value)
  {
    this.value = value;
    this.paramType = MessageParamType.VALUE;
  }

  /**
   * @value Value or i18n key, if paramType = I18N_KEY
   * @paramType
   */
  public MessageParam(String value, MessageParamType paramType)
  {
    this.value = value;
    this.paramType = paramType;
  }

  /**
   * @return The key for the localized message.
   * @throws IllegalArgumentException if paramType is not I18N_KEY or the value is not an instance of java.lang.String.
   */
  public String getI18nKey()
  {
    if (isI18nKey() == true) {
      return (String) value;
    }
    throw new IllegalArgumentException("getI18nKey is called, but paramType is not I18N_KEY or value is not an instance of java.lang.String");
  }

  /**
   * @return The value for the localized message.
   */
  public Object getValue()
  {
    return value;
  }
  
  /**
   * @return True, if paramType is I18N_KEY and the value is an instance of java.lang.String
   */
  public boolean isI18nKey() {
    return paramType == MessageParamType.I18N_KEY && value instanceof String;
  }
  
  public String toString()
  {
    return String.valueOf(value);
  }
}
