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
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ugorji.oxygen.io.VirtualFile;
import net.ugorji.oxygen.io.VirtualFileFilter;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.util.*;
import net.ugorji.oxygen.web.OxygenWebException;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLocal;

public abstract class BaseFSHelper implements FSHelper {

  // protected String encoding = null;
  protected OxygenEngine iwe;
  protected String METADATA_DIRECTORY;
  protected VirtualFile basefile;

  public void init(OxygenEngine iwe0, VirtualFile basefile0) throws Exception {
    // if(Boolean.TRUE == OxyLocal.get(Boolean.class)) Thread.dumpStack();
    iwe = iwe0;
    basefile = basefile0;
  }

  public String getMandatoryNonPagePatternRegex() {
    String s = METADATA_DIRECTORY;
    if (s.startsWith(".")) {
      s = "\\" + s;
    }
    s = ".*?/" + s + "(/.+)?";
    // s = ".*?/" + s + ".*";
    // System.out.println("getMandatoryNonPagePatternRegex: " + s);
    return s;
  }

  public String getMandatoryNonAttachmentPatternRegex() {
    return FSPageProvider.PAGENAME + "|" + "REVIEW\\.[0-9]+\\.TXT" + "|" + ".*?#.*" + "|" + ".*,v";
  }

  public void close() {
    // if(Boolean.TRUE == OxyLocal.get(Boolean.class)) Thread.dumpStack();
  }

  public void delete(FSFileInfo info) throws Exception {
    ensureValidVirtualFile(info.file);
    File ftemp = File.createTempFile("oxywiki", null);
    addOrEdit(info, ftemp);
    ftemp.delete();
    info.deleteFileOnSubmit = true;
  }

  public FSFileInfo getEntryInfo(VirtualFile _f, int version) throws Exception {
    if (isRepositoryInfoAvailable(_f)) {
      return doGetEntryInfo(_f, version);
    } else {
      FSFileInfo info = new FSFileInfo();
      info.file = _f;
      info.date = new Date(_f.lastModified());
      info.rev = getInitialVersion();
      return info;
    }
  }

  public List getEntryLog(VirtualFile _f) throws Exception {
    List list = null;
    if (isRepositoryInfoAvailable(_f)) {
      list = doGetEntryLog(_f);
    } else {
      FSFileInfo info = new FSFileInfo();
      info.file = _f;
      info.date = new Date(_f.lastModified());
      info.rev = getInitialVersion();
      list = new ArrayList();
      list.add(info);
    }
    return list;
  }

  public void addOrEdit(FSFileInfo info, File newContent) throws Exception {
    ensureValidVirtualFile(info.file);
    VirtualWritableFile _f = (VirtualWritableFile) info.file;
    doAddOrEdit(_f, newContent);
  }

  public void addOrEdit(FSFileInfo info, String newContent) throws Exception {
    ensureValidVirtualFile(info.file);
    VirtualWritableFile _f = (VirtualWritableFile) info.file;
    doAddOrEdit(_f, newContent);
  }

  public void submit(FSFileInfo info) throws Exception {
    ensureValidVirtualFile(info.file);
    doSubmit(info);
    if (info.deleteFileOnSubmit && info.file != null && info.file.exists()) {
      ((VirtualWritableFile) info.file).delete();
    }
  }

  public FSFileInfo makeChange(String logmsg, VirtualWritableFile _f) throws Exception {
    ensureValidVirtualFile(_f);
    if (_f.exists() && !isRepositoryInfoAvailable(_f)) {
      FSFileInfo info2 = doMakeChange("comments=initial info", _f);
      // System.out.println("_f.getFile(): " + _f.getFile());
      doAddOrEdit(
          _f, OxygenUtils.getTextContents(new InputStreamReader(_f.getInputStream()), true));
      doSubmit(info2);
    }
    return doMakeChange(logmsg, _f);
  }

  public void baseState(FSFileInfo info) {}

  public int getInitialVersion() {
    return 1;
  }

  public void deleteVersions(VirtualWritableFile vf, String logmsg, OxygenIntRange versions)
      throws Exception {
    ensureValidVirtualFile(vf);
    doDeleteVersions(vf, logmsg, versions);
  }

  protected String getEncoding() {
    return iwe.getProperty(WikiConstants.ENCODING_KEY, null);
  }

  protected OxygenCacheManager cachemgr() {
    return WikiLocal.getWikiEngine().getCacheManager();
  }

  protected VirtualFile getMetadataDir(VirtualFile _f) throws Exception {
    VirtualFile _f2 = _f.getParent().getChild(METADATA_DIRECTORY);
    if (_f2 != null && _f2 instanceof VirtualWritableFile && !_f2.exists()) {
      ((VirtualWritableFile) _f2).mkdirs();
    }
    return _f2;
  }

  protected int getUsableVersion(
      VirtualFile _f, int version, boolean onlyCheckRepository, boolean throwExceptionOnError)
      throws Exception {
    if (version < getInitialVersion() && isRepositoryInfoAvailable(_f)) {
      FSFileInfo info = doGetEntryInfo(_f, FSHelper.IMPOSSIBLE_NEGATIVE_VERSION);
      version = info.rev;
    }
    if (!onlyCheckRepository && version < getInitialVersion() && _f.exists()) {
      version = getInitialVersion();
    }
    if (throwExceptionOnError && version < getInitialVersion()) {
      throw new OxygenWebException("Unable to get Usable version for VirtualFile: " + _f.getPath());
    }
    return version;
  }

  protected String getExtension(VirtualFile _f) {
    return StringUtils.getFileNameExtension(_f.getName(), "");
  }

  protected void ensureValidVirtualFile(VirtualFile _f) throws Exception {
    if (_f == null || !(_f instanceof VirtualWritableFile)) {
      throw new Exception("Null or non-writable files (e.g. from archives) cannot be manipulated");
    }
  }

  protected String[] doLookupNames(
      VirtualFileFilter vff, Pattern pattern, VirtualFile f, int maxdepth) throws Exception {
    maxdepth = Math.min(maxdepth, Integer.MAX_VALUE - 20);
    VirtualFile[] vfs = f.list(vff, maxdepth + 1); // the +1 covers the *METADATA* directory
    HashSet hs = new HashSet();
    int fpathlen = f.getPath().length() + 1;
    for (int i = 0; i < vfs.length; i++) {
      String saa = vfs[i].getPath();
      Matcher m = pattern.matcher(saa);
      if (!m.matches()) {
        throw new OxygenWebException(
            "Unable to match within Helper: Pattern: " + pattern + " && String: " + saa);
      }
      saa = (m.group(1) + m.group(2)).substring(fpathlen);
      hs.add(saa);
    }
    String[] sa = (String[]) hs.toArray(new String[0]);
    Arrays.sort(sa);
    return sa;
  }

  protected FSFileInfo doMakeChange(String logmsg, VirtualWritableFile _f) throws Exception {
    FSFileInfo info = new FSFileInfo();
    // info.description = NL_PATTERN.matcher(logmsg).replaceAll("\\\\n");
    info.description = logmsg;
    info.file = _f;
    return info;
  }

  protected void doDeleteVersions(VirtualWritableFile vf, String logmsg, OxygenIntRange versions)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  protected abstract List doGetEntryLog(VirtualFile f) throws Exception;

  protected abstract void doAddOrEdit(VirtualWritableFile _f, Object newContentStringOrFile)
      throws Exception;

  protected abstract void doSubmit(FSFileInfo _f) throws Exception;

  protected abstract FSFileInfo doGetEntryInfo(VirtualFile _f, int version) throws Exception;
}
