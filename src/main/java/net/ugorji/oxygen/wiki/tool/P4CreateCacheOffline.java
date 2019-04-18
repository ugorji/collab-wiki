/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.tool;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import net.ugorji.oxygen.io.VirtualPlainFile;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.OxygenCacheManager;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.extensions.filesystem.FSFileInfo;
import net.ugorji.oxygen.wiki.extensions.filesystem.P4FSHelperViaCommandLineExec;

/**
 * This will create/populate the cache for all the info in here. This only works if using EhCache.
 * Make this private in constructor and main method, so no one uses it (since it is not yet
 * finalized)
 *
 * @author ugorjid
 */
/* make it package private for now, so no-one can call it. It's not to be used ... Does Not WORK*/
class P4CreateCacheOffline {
  private File runtimeDir;
  private Properties props;
  private File basedir;

  private P4CreateCacheOffline() {}

  private void execute() throws Exception {
    if (runtimeDir == null || basedir == null || props == null) {
      throw new Exception("runtimeDir, basedir and props must not be null");
    }
    WikiEngine we = new WikiEngine(props);
    WikiLocal.setWikiEngine(we);
    OxygenCacheManager cachemgr = we.getCacheManager();
    P4FSHelperViaCommandLineExec p4helper = new P4FSHelperViaCommandLineExec();
    p4helper.init(we, null);
    List files = new LinkedList();
    FileFilter ff =
        new FileFilter() {
          public boolean accept(File f) {
            return f.isFile();
          }
        };
    OxygenUtils.listFiles(basedir, Integer.MAX_VALUE, files, ff);
    Collections.sort(files);

    for (Iterator itr = files.iterator(); itr.hasNext(); ) {
      File f = (File) itr.next();
      VirtualPlainFile vf = new VirtualPlainFile(f);
      List p4entries = p4helper.getEntryLog(vf);
      for (Iterator itr2 = p4entries.iterator(); itr2.hasNext(); ) {
        FSFileInfo p4info = (FSFileInfo) itr2.next();
        String desc = p4info.description;
      }
    }
  }

  private static void main(String[] args) throws Exception {
    P4CreateCacheOffline p4cco = new P4CreateCacheOffline();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-props")) {
        p4cco.props = new Properties();
        InputStream is = new FileInputStream(args[++i]);
        p4cco.props.load(is);
        CloseUtils.close(is);

      } else if (args[i].equals("-runtimedir")) {
        p4cco.runtimeDir = new File(args[++i]);
      } else if (args[i].equals("-basedir")) {
        p4cco.basedir = new File(args[++i]);
      }
    }
    p4cco.execute();
  }
}
