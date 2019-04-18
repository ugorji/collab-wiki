/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

/**
 * Shows the sysinfo screen - all available categories - how many pages they have, and last update
 * time
 *
 * @author ugorji
 */
public class SysInfoAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    includeJSPView("sysinfo.jsp");
  }

  public int render() throws Exception {
    showJSPView("sysinfo.jsp");
    return RENDER_COMPLETED;
  }
}
