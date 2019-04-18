/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions.filesystem;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.List;
import net.ugorji.oxygen.io.VirtualFile;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.util.Closeable;
import net.ugorji.oxygen.util.OxygenEngine;
import net.ugorji.oxygen.util.OxygenIntRange;

/** Implementations of these actually handle the backend repository */
public interface FSHelper extends Closeable {
  int IMPOSSIBLE_POSITIVE_VERSION = ((Integer.MAX_VALUE / 2) - 9);
  int IMPOSSIBLE_NEGATIVE_VERSION = (-1 * IMPOSSIBLE_POSITIVE_VERSION);

  String getMandatoryNonPagePatternRegex();

  String getMandatoryNonAttachmentPatternRegex();

  void init(OxygenEngine iwe, VirtualFile basefile) throws Exception;

  void delete(FSFileInfo info) throws Exception;

  void deleteVersions(VirtualWritableFile vf, String logmsg, OxygenIntRange versions)
      throws Exception;
  /** newContent is either a file, or a String */
  void addOrEdit(FSFileInfo info, File newContent) throws Exception;

  void addOrEdit(FSFileInfo info, String newContent) throws Exception;

  void submit(FSFileInfo info) throws Exception;

  FSFileInfo makeChange(String logmsg, VirtualWritableFile _f) throws Exception;

  FSFileInfo getEntryInfo(VirtualFile f, int version) throws Exception;
  /**
   * Returns a list of FSFileInfo in natural order. Ensure that all fields are filled, especially
   * date
   */
  List getEntryLog(VirtualFile f) throws Exception;

  BufferedInputStream getFileContentsAsStream(VirtualFile _f, int rev) throws Exception;

  boolean isRepositoryInfoAvailable(VirtualFile f) throws Exception;

  void baseState(FSFileInfo info);

  int getInitialVersion();

  String[] lookupNames(VirtualFile f, int maxdepth, boolean deleted) throws Exception;
}
