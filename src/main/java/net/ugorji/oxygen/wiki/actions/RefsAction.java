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
 * Shows the references screen - orphan'ed pages (not referenced by anyone) - non-existent pages
 * (referenced, but do not exist)
 *
 * @author ugorji
 */
public class RefsAction extends GenericWikiWebAction {
  public void includeView() throws UnsupportedOperationException, Exception {
    includeJSPView("refs.jsp");
  }

  public int render() throws Exception {
    showJSPView("refs.jsp");
    return RENDER_COMPLETED;
  }
}
