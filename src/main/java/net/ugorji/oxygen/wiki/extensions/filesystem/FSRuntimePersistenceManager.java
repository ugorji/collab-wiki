/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions.filesystem;

import java.io.File;
import java.util.Properties;
import net.ugorji.oxygen.io.VirtualPlainFile;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiRuntimePersistenceManager;

public class FSRuntimePersistenceManager implements WikiRuntimePersistenceManager {
  protected FSHelper helper;

  public void prepare() throws Exception {
    WikiEngine engine = WikiLocal.getWikiEngine();
    helper = FSUtils.retrieveFSHelper(engine);
  }

  public String load(String fName) throws Exception {
    String s = null;
    File f = new File(WikiLocal.getWikiEngine().getConfigDirectory(), fName);
    if (f.exists()) {
      s = OxygenUtils.getFileContents(f);
    }
    return s;
  }

  public void store(String fName, String text, Properties attributes) throws Exception {
    FSFileInfo info = null;
    try {
      File f = new File(WikiLocal.getWikiEngine().getConfigDirectory(), fName);
      VirtualWritableFile _f = new VirtualPlainFile(f);
      String logmsg = FSUtils.propertiesToChange(attributes, null, fName);
      info = helper.makeChange(logmsg, _f);
      helper.addOrEdit(info, text);
      OxygenUtils.writeFileContents(f, text);
      helper.submit(info);
    } finally {
      if (info != null) helper.baseState(info);
    }
  }

  public void close() {
    CloseUtils.close(helper);
    helper = null;
  }
}
