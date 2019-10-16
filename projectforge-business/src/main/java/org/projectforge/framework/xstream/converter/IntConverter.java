/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2019 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.framework.xstream.converter;

import org.apache.commons.lang3.StringUtils;
import org.projectforge.framework.xstream.XmlConstants;

public class IntConverter extends AbstractValueConverter<Integer>
{
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IntConverter.class);

  @Override
  public Integer fromString(String str)
  {
    try {
      if (StringUtils.isEmpty(str) || XmlConstants.NULL_IDENTIFIER.equals(str)) {
        return null;
      }
      return new Integer(str);
    } catch (final NumberFormatException ex) {
      log.warn("Can't convert value '" + str + "' to int.");
      return 0;
    }
  }
}
