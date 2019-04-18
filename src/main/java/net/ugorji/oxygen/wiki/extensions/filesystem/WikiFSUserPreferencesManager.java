/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions.filesystem;

import java.util.Properties;
import net.ugorji.oxygen.io.VirtualPlainFile;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.manager.FSUserPreferencesManager;
import net.ugorji.oxygen.wiki.WikiLocal;

public class WikiFSUserPreferencesManager extends FSUserPreferencesManager {
  protected FSHelper helper;

  public void init(Properties p) throws Exception {
    helper = FSUtils.retrieveFSHelper(WikiLocal.getWikiEngine());
    super.init(p);
  }

  public void save(Properties metadata) throws Exception {
    FSFileInfo info = null;
    try {
      VirtualWritableFile _f = new VirtualPlainFile(file);
      String logmsg = FSUtils.propertiesToChange(metadata, null, file.getName());
      info = helper.makeChange(logmsg, _f);
      helper.addOrEdit(info, "");
      super.save(metadata);
      helper.submit(info);
    } finally {
      if (info != null) helper.baseState(info);
    }
  }
}
