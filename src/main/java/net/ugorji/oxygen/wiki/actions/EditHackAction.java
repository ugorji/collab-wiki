/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

/*
 * Hacked action for testing. Please don't use
 */
public class EditHackAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    EditPostAction.handlePostEditableSavePersist();
    // System.out.println("-- EditHackAction: got here --");
    // WikiLocal.getWikiLinkHolder().getExtraparams().clear();
    return ACTION_PROCESSING_COMPLETED;
  }
}
