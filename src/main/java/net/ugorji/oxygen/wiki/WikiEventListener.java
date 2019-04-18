/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import net.ugorji.oxygen.util.Closeable;

/** Implementations of these receive notifications when a WikiEvent is generated */
public interface WikiEventListener extends Closeable {
  void handleWikiEvent(WikiEvent we) throws Exception;

  void prepare(WikiCategoryEngine _wce) throws Exception;
}
