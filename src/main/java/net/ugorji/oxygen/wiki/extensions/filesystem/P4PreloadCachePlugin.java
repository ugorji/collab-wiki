/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions.filesystem;

import net.ugorji.oxygen.util.Plugin;

/**
 * Expects the following: 1. Parameters for configuring the P4 depot are all stored in the
 * oxywiki.properties file, so they are available from the WikiEngine properties 2. The WikiEngine
 * is in the WikiLocal space 3. A value of the base p4 path is stored in the wiki engine properties
 * under the key: WikiConstants.PROVIDER_FILESYSTEM_LOCATION_BASE_KEY
 */
public abstract class P4PreloadCachePlugin implements Plugin {
  /*
  public void start() {
    VirtualFile vf = null;
    try {
      OxygenUtils.info("Starting to preload the p4 cache ...");
      P4FSHelperViaCommandLineExec h = new P4FSHelperViaCommandLineExec();
      WikiEngine we = WikiLocal.getWikiEngine();
      vf = FSUtils.getBaseFile(we);
      h.init(we, vf);
      h.preloadCache();
      OxygenUtils.info("Done preloading the p4 cache");
    } catch(Exception exc) {
      OxygenUtils.error("Exception preloading the p4 cache from: " + vf, exc);
    }
  }

  public void init() { }
  public void close() { }
  */
}
