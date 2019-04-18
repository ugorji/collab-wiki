/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions;

import net.ugorji.oxygen.util.SetURLConnectionTimeoutPlugin;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiLocal;

public class SetURLConnectionTimeoutWikiPlugin extends SetURLConnectionTimeoutPlugin {
  protected long getReadTimeout() {
    WikiEngine engine = WikiLocal.getWikiEngine();
    return Long.parseLong(engine.getProperty(WikiConstants.READ_TIMEOUT_KEY, null));
  }

  protected long getConnectTimeout() {
    WikiEngine engine = WikiLocal.getWikiEngine();
    return Long.parseLong(engine.getProperty(WikiConstants.CONNECT_TIMEOUT_KEY, null));
  }
}
