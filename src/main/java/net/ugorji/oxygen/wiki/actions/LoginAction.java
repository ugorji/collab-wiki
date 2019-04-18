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
 * Shows the login screen
 *
 * @author ugorji
 */
public class LoginAction extends GenericWikiWebAction {
  public void includeView() throws UnsupportedOperationException, Exception {
    includeJSPView("login.jsp");
  }

  public int render() throws Exception {
    showJSPView("login.jsp");
    return RENDER_COMPLETED;
  }
}
