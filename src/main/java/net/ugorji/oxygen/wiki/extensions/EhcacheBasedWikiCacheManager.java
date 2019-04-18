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
import java.io.FileWriter;
import java.util.Hashtable;
import net.sf.ehcache.CacheManager;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.EhcacheOxygenCacheManager;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiLocal;

public class EhcacheBasedWikiCacheManager extends EhcacheOxygenCacheManager {

  public void prepare() throws Exception {
    WikiEngine we = WikiLocal.getWikiEngine();
    File runtimeDir = we.getRuntimeDirectory();
    File f = new File(runtimeDir, "ehcache_data_cache");
    f.mkdirs();

    Hashtable model = new Hashtable();
    model.put("DiskPath", f.getAbsolutePath().replace('\\', '/'));

    f = new File(runtimeDir, "ehcache.xml");
    FileWriter fw = new FileWriter(f);

    WikiLocal.getWikiEngine().getWikiTemplateFilesHandler().write("ehcache.xml", model, fw);
    CloseUtils.close(fw);

    cm = new CacheManager(f.getAbsolutePath());
    // do this. See if it helps fix the "CacheManager is not alive" issues.
    cm.shutdown();
    cm = new CacheManager(f.getAbsolutePath());
  }
}
