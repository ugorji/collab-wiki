/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

/** This action should only be called AJAX'Y */
public class ListPagesForTagAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    // System.out.println("ListPagesForTagAction");
    includeJSPView("listpagesfortag.jsp");
    return RENDER_COMPLETED;
  }
}
