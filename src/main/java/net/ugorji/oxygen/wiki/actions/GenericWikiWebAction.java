/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.web.GenericWebAction;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiWebAction;

public class GenericWikiWebAction extends GenericWebAction implements WikiWebAction {

  static void showJSPView(String jsppage) throws Exception {
    WikiLocal.getWikiTemplateHandler().showView(jsppage);
  }

  static void includeJSPView(String jsppage) throws Exception {
    WikiLocal.getWikiTemplateHandler().includeView(jsppage);
  }
}
