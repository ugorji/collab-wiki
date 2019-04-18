/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

public class TestAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
  }

  public int render() throws Exception {
    showJSPView("test.jsp");
    return RENDER_COMPLETED;
  }
}
