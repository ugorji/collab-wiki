/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions;

import java.util.HashMap;
import java.util.Map;
import net.ugorji.oxygen.util.BSFPlugin;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiLocal;

public class BSFPreinitWikiPlugin extends BSFPlugin {
  public BSFPreinitWikiPlugin() throws Exception {
    super();
    pre = true;
    WikiEngine we = WikiLocal.getWikiEngine();
    if (we != null) {
      lang = we.getProperty(WikiConstants.BSF_LANGUAGE_KEY);
    }
  }

  protected Map getBeansToDeclare() {
    HashMap m = null;
    WikiEngine we = WikiLocal.getWikiEngine();
    if (we != null) {
      m = new HashMap();
      m.put("wikiEngine", we);
    }
    return m;
  }
}
