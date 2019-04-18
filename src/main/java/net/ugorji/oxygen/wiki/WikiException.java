/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import net.ugorji.oxygen.web.OxygenWebException;

/**
 * Generic WikiException object
 *
 * @author ugorji
 */
public class WikiException extends OxygenWebException {

  protected WikiException() {
    super("");
  }

  public WikiException(String msg) {
    super(msg);
  }

  public WikiException(String msg, Throwable thr) {
    super(msg, thr);
  }
}
