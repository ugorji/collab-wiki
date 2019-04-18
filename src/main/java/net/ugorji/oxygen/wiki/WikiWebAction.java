/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.web.WebAction;

public interface WikiWebAction extends WebAction {
  int FLAG_REQUIRES_EXTRAINFO = OxygenUtils.makeFlag(4);
  int FLAG_HONORS_VERSION = OxygenUtils.makeFlag(5);
  int FLAG_REQUIRES_PAGENAME = OxygenUtils.makeFlag(6);
  int FLAG_NOT_HANDLED_BY_PORTLET = OxygenUtils.makeFlag(7);
  int FLAG_MAKE_SHORTHAND = OxygenUtils.makeFlag(8);
}
