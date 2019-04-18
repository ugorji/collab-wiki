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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import net.ugorji.oxygen.io.VirtualFile;
import net.ugorji.oxygen.io.VirtualFileFilter;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.OxygenIntRange;
import net.ugorji.oxygen.util.OxygenProxy;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiAttachmentProvider;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiException;
import net.ugorji.oxygen.wiki.WikiProvidedObject;

/** Attachments lie in the same directory as the page */
public class FSAttachmentProvider implements WikiAttachmentProvider {

  protected WikiCategoryEngine engine;
  protected VirtualFile basefile;
  protected Pattern notAttachmentPattern = null;
  protected FSHelper helper;
  private VirtualFileFilter anAttachmentFileFilter = null;

  public void prepare(WikiCategoryEngine wce) throws Exception {
    engine = wce;
    basefile = FSUtils.getBaseFile(wce);
    helper = FSUtils.retrieveFSHelper(engine);

    notAttachmentPattern =
        FSUtils.getNotProvidedObjectPattern(
            helper.getMandatoryNonAttachmentPatternRegex(),
            wce.getProperty(WikiConstants.PROVIDER_FILESYSTEM_NOT_ATTACHMENT_CONFIGURED_REGEX_KEY));
    anAttachmentFileFilter =
        new VirtualFileFilter() {
          public boolean accept(VirtualFile f) {
            return isAttachment(f);
          }
        };
  }

  public void close() {
    CloseUtils.close(basefile);
    basefile = null;
    CloseUtils.close(helper);
    helper = null;
  }

  public int getInitialVersion() {
    return helper.getInitialVersion();
  }

  public boolean supportsAttachmentVersions() {
    return true;
  }

  public boolean attachmentExists(String pagerep, String attachment) throws Exception {
    VirtualFile _f = getFile(pagerep, attachment);
    return (_f != null && _f.exists());
  }

  public WikiProvidedObject getAttachment(String pagerep, String attachment, int version)
      throws Exception {
    WikiProvidedObject wp = null;
    VirtualFile _f = getFile(pagerep, attachment);
    if (version == WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY) {
      wp =
          new WikiProvidedObject(
              attachment,
              new Date(_f.lastModified()),
              _f.size(),
              WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY);
    } else if (!(helper.isRepositoryInfoAvailable(_f))) {
      wp =
          new WikiProvidedObject(
              attachment, new Date(_f.lastModified()), _f.size(), helper.getInitialVersion());
    } else {
      wp = getAttachmentFromRepository(pagerep, attachment, version);
    }
    return wp;
  }

  public WikiProvidedObject[] getAttachmentVersionHistory(String pagerep, String attachment)
      throws Exception {
    VirtualFile _f = getFile(pagerep, attachment);
    List files = helper.getEntryLog(_f);
    int lsize = files.size();
    WikiProvidedObject[] wps = new WikiProvidedObject[lsize];
    for (int i = 0; i < lsize; i++) {
      FSFileInfo p4file = (FSFileInfo) files.get(i);
      OxygenUtils.debug(
          "P4FileInfo: pagename: "
              + pagerep
              + " getHeadRev(): "
              + p4file.rev
              + " getHeadTime(): "
              + p4file.date);
      wps[i] = new WikiProvidedObject(attachment, p4file.date, p4file.size, p4file.rev);
      FSUtils.setPageAttributes(wps[i], p4file.description);
    }
    return wps;
  }

  public InputStream getAttachmentInputStream(String pagename, WikiProvidedObject wpattach)
      throws Exception {
    BufferedInputStream br = null;
    VirtualFile _f = getFile(pagename, wpattach.getName());
    // if a specific version is given, then get from repository. Else get from filesystem.
    if (wpattach.getVersion() < helper.getInitialVersion()
        || !(helper.isRepositoryInfoAvailable(_f))) {
      // WikiUtils.info("Getting attachment inputstream from disk");
      if (_f != null && _f.exists()) {
        br = new BufferedInputStream(_f.getInputStream());
      } else {
        throw new WikiException(
            WebLocal.getI18n()
                .str("providers.file.attachment_not_exist", pagename, wpattach.getName()));
      }
    } else {
      br = getAttachmentInputStreamFromRepository(pagename, wpattach);
    }
    return br;
  }

  public void saveAttachment(String pagerep, String attachment, File f, Properties attributes)
      throws Exception {
    FSFileInfo info = null;
    try {
      testAttachmentFileName(f.getName());
      VirtualWritableFile _f = (VirtualWritableFile) getFile(pagerep, attachment);
      String logmsg = FSUtils.propertiesToChange(attributes, pagerep, attachment);
      info = helper.makeChange(logmsg, _f);
      helper.addOrEdit(info, f);
      helper.submit(info);
    } finally {
      if (info != null) helper.baseState(info);
    }
  }

  public void deleteAttachment(String pagerep, String attachment, Properties atts)
      throws Exception {
    FSFileInfo info = null;
    try {
      VirtualWritableFile _f = (VirtualWritableFile) getFile(pagerep, attachment);
      if (_f != null && _f.exists()) {
        String logmsg = FSUtils.propertiesToChange(atts, pagerep, attachment);
        info = helper.makeChange(logmsg, _f);
        helper.delete(info);
        helper.submit(info);
      }
    } finally {
      if (info != null) helper.baseState(info);
    }
  }

  public void deleteAttachmentVersions(
      String pagerep, String attachment, Properties atts, OxygenIntRange versions)
      throws Exception {
    try {
      VirtualWritableFile _f = (VirtualWritableFile) getFile(pagerep, attachment);
      if (_f != null && _f.exists()) {
        String logmsg = FSUtils.propertiesToChange(atts, pagerep, attachment);
        helper.deleteVersions(_f, logmsg, versions);
      }
    } finally {
    }
  }

  public String[] getAttachmentNames(String pagerep, boolean deleted) throws Exception {
    String[] sa = new String[0];
    HashSet hs = new HashSet();
    if (deleted) {
      sa = helper.lookupNames(getFile(pagerep), 1, true);
      for (int i = 0; i < sa.length; i++) {
        if (notAttachmentPattern == null || !(notAttachmentPattern.matcher(sa[i]).matches())) {
          hs.add(sa[i]);
        }
      }
    } else {
      hs.addAll(getAttachmentNamesFromDisk(pagerep));
    }

    sa = (String[]) hs.toArray(new String[0]);
    Arrays.sort(sa);
    return sa;
  }

  public Map getAllAttachmentNames(String parentPageRep, int maxdepth, boolean deleted)
      throws Exception {
    Map m = null;
    FSPageProvider fspageprov =
        (FSPageProvider) OxygenProxy.getTargetGivenProxy(engine.getPageProvider());
    if (deleted) {
      m = new HashMap();
      String prefix =
          (StringUtils.isBlank(parentPageRep) ? "" : (StringUtils.trim(parentPageRep, '/') + '/'));
      String[] sa = helper.lookupNames(basefile.getChild(parentPageRep), maxdepth, true);
      // separate the pagename, from the non-page name
      // if the pagename is an attachment, then add it to the list ... same way
      for (int i = 0; i < sa.length; i++) {
        int j = sa[i].lastIndexOf('/');
        String a = sa[i];
        String s = prefix;
        if (j != -1) {
          s = prefix + sa[i].substring(0, j);
          a = sa[i].substring(j + 1).trim();
        }
        if (!(StringUtils.isBlank(s))
            && (fspageprov == null || fspageprov.isPageName(s, false))
            && isAttachmentName(a)) {
          List list = (List) m.get(s);
          if (list == null) {
            list = new ArrayList();
            m.put(s, list);
          }
          list.add(a);
        }
      }
    } else {
      m = getAllAttachmentNamesFromDisk(parentPageRep, maxdepth, fspageprov);
    }
    return m;
  }

  protected VirtualFile getFile(String pagerep, String attachment) throws Exception {
    return basefile.getChild(pagerep + "/" + attachment);
  }

  protected VirtualFile getFile(String pagerep) throws Exception {
    return basefile.getChild(pagerep);
  }

  protected void testAttachmentFileName(String fName) throws Exception {
    if (notAttachmentPattern != null && notAttachmentPattern.matcher(fName).matches()) {
      throw new WikiException(
          WebLocal.getI18n()
              .str("providers.file.not_attachment_pattern_error", notAttachmentPattern.pattern()));
    }
  }

  protected Properties getAttributes(String pagerep, String attachment) throws Exception {
    VirtualFile vf = getFile(pagerep, attachment);
    FSFileInfo info = helper.getEntryInfo(vf, FSHelper.IMPOSSIBLE_NEGATIVE_VERSION);
    Properties p = new Properties();
    if (vf != null && vf.exists()) {
      FSUtils.changeToProperties(info.description, p);
    }
    return p;
  }

  boolean isAttachment(VirtualFile f) {
    try {
      if (!(f.isDirectory())) {
        return isAttachmentName(f.getName());
      }
    } catch (Exception exc) {
      OxygenUtils.error(exc);
    }
    return false;
  }

  boolean isAttachmentName(String fName) throws Exception {
    return (notAttachmentPattern == null || !(notAttachmentPattern.matcher(fName).matches()));
  }

  protected BufferedInputStream getAttachmentInputStreamFromRepository(
      String pagename, WikiProvidedObject wpattach) throws Exception {
    VirtualFile _f = getFile(pagename, wpattach.getName());
    return helper.getFileContentsAsStream(_f, wpattach.getVersion());
  }

  private WikiProvidedObject getAttachmentFromRepository(
      String pagerep, String attachment, int version) throws Exception {
    VirtualFile _f = getFile(pagerep, attachment);
    FSFileInfo p4file = helper.getEntryInfo(_f, version);
    OxygenUtils.debug("P4FileInfo: pagename: " + pagerep + " getHeadTime(): " + p4file.date);
    WikiProvidedObject wp =
        new WikiProvidedObject(attachment, p4file.date, p4file.size, p4file.rev);
    FSUtils.setPageAttributes(wp, p4file.description);
    return wp;
  }

  private List getAttachmentNamesFromDisk(String pagerep) throws Exception {
    VirtualFile _dir = getFile(pagerep);
    VirtualFile[] _files = null;
    if (_dir == null || !_dir.exists() || (_files = _dir.list(anAttachmentFileFilter, 1)) == null) {
      return new ArrayList(0);
    }
    OxygenUtils.debug("WikiAttachments: " + Arrays.asList(_files));
    List wps = new ArrayList();
    for (int i = 0; i < _files.length; i++) {
      wps.add(_files[i].getName());
    }
    return wps;
  }

  private Map getAllAttachmentNamesFromDisk(
      String parentpagerep, int maxdepth, FSPageProvider fspageprov) throws Exception {
    // returns a map of string(page name) to List(attachment names)
    VirtualFile _dir = getFile(parentpagerep);
    VirtualFile[] _files = null;
    if (_dir == null
        || !_dir.exists()
        || (_files = _dir.list(anAttachmentFileFilter, maxdepth)) == null) {
      return new HashMap(0);
    }
    Map m = new HashMap();
    Arrays.sort(_files);
    for (int i = 0; i < _files.length; i++) {
      String s =
          FSUtils.extractPagename(_files[i].getParent().getPath().replace('\\', '/'), basefile);
      String a = _files[i].getName();
      // System.out.println("s, a: " + s + ", " + a + " || " + fspageprov.isPagename(s, true));
      if (!(StringUtils.isBlank(s)) && (fspageprov == null || fspageprov.isPageName(s, true))) {
        List list = (List) m.get(s);
        if (list == null) {
          list = new ArrayList();
          m.put(s, list);
        }
        list.add(a);
      }
    }
    return m;
  }
}

/*
  public WikiProvidedObject[] getAttachments(String pagerep, boolean detailsNecessary) throws Exception {
    VirtualFile _dir = getFile(pagerep);
    if(_dir == null || !_dir.exists()) {
      return new WikiProvidedObject[0];
    }
    VirtualFileFilter ffilter = new VirtualFileFilter() {
        public boolean accept(VirtualFile f) {
          return isAttachment(f);
        }
      };
    VirtualFile[] _files = _dir.list(ffilter);
    if(_files == null) {
      _files = new VirtualFile[0];
    }
    Arrays.sort(_files);

    WikiProvidedObject[] wps = new WikiProvidedObject[_files.length];
    WikiUtils.debug("WikiAttachments: " + Arrays.asList(_files));

    int attversion = WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY;
    if(detailsNecessary) {
      attversion = WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY;
    }
    for(int i = 0; i < _files.length; i++) {
      wps[i] = getAttachment(pagerep, _files[i].getName(), attversion);
    }
    return wps;
  }

*/
