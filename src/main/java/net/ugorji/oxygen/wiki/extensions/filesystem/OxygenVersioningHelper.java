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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import net.ugorji.oxygen.io.VirtualFile;
import net.ugorji.oxygen.io.VirtualFileFilter;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.OxygenIntRange;
import net.ugorji.oxygen.util.OxygenVersioningArchive;
import net.ugorji.oxygen.util.OxygenVersioningArchiveVirtualFileImpl;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.wiki.WikiConstants;

public class OxygenVersioningHelper extends BaseFSHelper {

  private static final String VER_FILE_SUFFIX = ".admin.txt.gz";
  private static final int VER_FILE_SUFFIX_LEN = VER_FILE_SUFFIX.length();
  private static final Pattern verFilePathPattern =
      Pattern.compile(
          "(.*?/)" + OxygenVersioningArchive.METADATA_DIRECTORY + "/(.*?)\\.admin\\.txt\\.gz");

  public OxygenVersioningHelper() {
    METADATA_DIRECTORY = OxygenVersioningArchive.METADATA_DIRECTORY;
  }

  public BufferedInputStream getFileContentsAsStream(VirtualFile _f, int rev) throws Exception {
    rev = getUsableVersion(_f, rev, true, true);
    OxygenVersioningArchive va = getVersioningArchive(_f);
    InputStream is = va.getInputStream(rev);
    BufferedInputStream br = (is == null ? null : new BufferedInputStream(is));
    return br;
  }

  public boolean isRepositoryInfoAvailable(VirtualFile _f) throws Exception {
    return getVersioningArchive(_f).exists();
  }

  public String[] lookupNames(final VirtualFile f, int maxdepth, final boolean deleted)
      throws Exception {
    // System.out.println("lookupNames: " + f.getPath());
    VirtualFileFilter vff =
        new VirtualFileFilter() {
          public boolean accept(VirtualFile vf) {
            try {
              String vfname = vf.getName();
              boolean isInRepo =
                  (vf.getParent().getName().equals(METADATA_DIRECTORY)
                      && !vf.getParent().getParent().getName().equals(METADATA_DIRECTORY)
                      && vfname.endsWith(VER_FILE_SUFFIX));
              boolean corrFileExists = false;
              if (isInRepo) {
                vfname = vfname.substring(0, vfname.length() - VER_FILE_SUFFIX_LEN);
                corrFileExists = vf.getParent().getParent().getChild(vfname).exists();
              }
              return (deleted ? (isInRepo && !corrFileExists) : (isInRepo && corrFileExists));
            } catch (Exception exc) {
              return false;
            }
          }
        };
    return doLookupNames(vff, verFilePathPattern, f, maxdepth);
  }

  protected void doDeleteVersions(VirtualWritableFile vf, String logmsg, OxygenIntRange versions)
      throws Exception {
    OxygenVersioningArchive va = getVersioningArchive(vf);
    va.deleteVersions(versions);
  }

  protected void doAddOrEdit(VirtualWritableFile _vf, Object newContent) throws Exception {
    ((VirtualWritableFile) _vf.getParent()).mkdirs();
    FSUtils.preSaveOverwrite(_vf, newContent, getEncoding(), false);
  }

  protected void doSubmit(FSFileInfo info) throws Exception {
    VirtualWritableFile _f = (VirtualWritableFile) info.file;
    OxygenVersioningArchive va = getVersioningArchive(_f);
    InputStream is = _f.getInputStream();
    try {
      va.addNewVersion(is, info.description);
    } finally {
      CloseUtils.close(is);
    }
  }

  protected FSFileInfo doGetEntryInfo(VirtualFile _f, int version) throws Exception {
    Map map = preloadFsInfo(_f, (version < getInitialVersion()), version);
    return (FSFileInfo) map.get(new Integer(version));
  }

  protected List doGetEntryLog(VirtualFile _f) throws Exception {
    Map map = preloadFsInfo(_f, false, IMPOSSIBLE_POSITIVE_VERSION);
    FSFileInfo[] versions = (FSFileInfo[]) map.values().toArray(new FSFileInfo[0]);
    Arrays.sort(versions);
    return Arrays.asList(versions);
  }

  private OxygenVersioningArchive getVersioningArchive(VirtualFile _f) throws Exception {
    return new OxygenVersioningArchiveVirtualFileImpl(_f, getEncoding());
  }

  private Map preloadFsInfo(VirtualFile _f, boolean latestVersionOnly, int versionToStorelatestAs)
      throws Exception {
    int maxHeadRev = getInitialVersion() - 1;
    String s = null;
    OxygenVersioningArchive va = getVersioningArchive(_f);
    Map map = new HashMap();
    Map descriptions = va.getDescriptions();
    // System.out.println("va.getDescriptions(): " + descriptions);
    for (Iterator itr = descriptions.entrySet().iterator(); itr.hasNext(); ) {
      Map.Entry entry = (Map.Entry) itr.next();
      Integer key = (Integer) entry.getKey();
      String desc = (String) entry.getValue();
      FSFileInfo info = new FSFileInfo();
      info.file = _f;
      info.rev = key.intValue();
      info.description = desc;
      Properties pp = StringUtils.stringToProps(info.description);
      if (!StringUtils.isBlank(s = pp.getProperty(WikiConstants.ATTRIBUTE_SIZE))) {
        info.size = Long.parseLong(s);
      }
      if (!StringUtils.isBlank(s = pp.getProperty(WikiConstants.ATTRIBUTE_TIMESTAMP))) {
        info.date = new Date(Long.parseLong(s));
      } else {
        info.date = va.getDate(info.rev);
      }
      maxHeadRev = Math.max(maxHeadRev, info.rev);
      map.put(new Integer(info.rev), info);
    }
    if (maxHeadRev >= getInitialVersion()) {
      FSFileInfo info = (FSFileInfo) map.get(new Integer(maxHeadRev));
      if (latestVersionOnly) {
        map.put(new Integer(versionToStorelatestAs), info);
      }
    }

    return map;
  }
}
