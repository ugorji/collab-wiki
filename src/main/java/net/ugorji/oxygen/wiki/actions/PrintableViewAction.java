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
 * Shows a printable view (with no borders)
 *
 * @author ugorji
 */
public class PrintableViewAction extends ViewAction {
  public PrintableViewAction() {
    super();
    showBorders = false;
    setFlag(FLAG_NOT_HANDLED_BY_PORTLET);
  }
}
