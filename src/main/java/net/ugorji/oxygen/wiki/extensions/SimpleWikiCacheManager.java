/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions;

import java.io.File;
import net.ugorji.oxygen.util.SimpleOxygenCacheManager;
import net.ugorji.oxygen.wiki.WikiLocal;

public class SimpleWikiCacheManager extends SimpleOxygenCacheManager {
  public SimpleWikiCacheManager() throws Exception {
    super(new File(WikiLocal.getWikiEngine().getRuntimeDirectory(), "simple_data_cache"));
  }
}
