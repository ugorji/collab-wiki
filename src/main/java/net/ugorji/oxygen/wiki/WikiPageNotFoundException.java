/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import net.ugorji.oxygen.web.WebResourceNotFoundException;

public class WikiPageNotFoundException extends WebResourceNotFoundException {
  private String pagename;
  private String category;

  public WikiPageNotFoundException(String msg, String pagename0, String category0) {
    this(msg, null, pagename0, category0);
  }

  public WikiPageNotFoundException(String msg, Throwable thr, String pagename0, String category0) {
    super(msg, thr);
    pagename = pagename0;
    category = category0;
  }

  public String getPagename() {
    return pagename;
  }

  public String getCategory() {
    return category;
  }
}
