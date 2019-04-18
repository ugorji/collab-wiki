/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions.filesystem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import net.ugorji.oxygen.io.VirtualFile;
import net.ugorji.oxygen.io.VirtualFileFilter;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.OxygenIntRange;
import net.ugorji.oxygen.util.OxygenRevision;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiException;
import net.ugorji.oxygen.wiki.WikiPageNotFoundException;
import net.ugorji.oxygen.wiki.WikiPageProvider;
import net.ugorji.oxygen.wiki.WikiProvidedObject;

/**
 * Both the latest version, and the baseline are typically there Latest version is always there.
 * Baseline may or may not be there
 */
public class FSPageProvider implements WikiPageProvider {

  public static final String PAGENAME = "PAGE.TXT";

  protected WikiCategoryEngine engine;
  protected VirtualFile basefile;
  protected Pattern notPagePattern = null;
  protected String encoding = null;
  protected FSHelper helper;
  private VirtualFileFilter aPageFileFilter = null;

  public void prepare(WikiCategoryEngine wce) throws Exception {
    engine = wce;
    encoding = engine.getCharacterEncoding();
    basefile = FSUtils.getBaseFile(wce);

    helper = FSUtils.retrieveFSHelper(engine);

    OxygenUtils.debug("In DefaultFileSystemPageProvider: using VirtualFile");
    notPagePattern =
        FSUtils.getNotProvidedObjectPattern(
            helper.getMandatoryNonPagePatternRegex(),
            wce.getProperty(WikiConstants.PROVIDER_FILESYSTEM_NOT_PAGE_CONFIGURED_REGEX_KEY));
    aPageFileFilter =
        new VirtualFileFilter() {
          public boolean accept(VirtualFile f) {
            return isPage(f);
          }
        };
  }

  public void close() {
    CloseUtils.close(basefile);
    basefile = null;
    CloseUtils.close(helper);
    helper = null;
  }

  public boolean supportsPageVersions() {
    return true;
  }

  public boolean pageExists(String pagerep) throws Exception {
    VirtualFile vf = getFile(pagerep);
    return (vf != null && vf.exists());
  }

  public WikiProvidedObject getPage(String pagerep, int version) throws Exception {
    // Thread.dumpStack();
    // System.out.println("getPage called with version: " + version);
    WikiProvidedObject wp = null;
    VirtualFile _f = getFile(pagerep);
    // note that, the PAGE.TXT must always exist (else we say the page does not exist)
    // DO NOT DO THIS FOR NOW - always check (even if page does not exist)
    // if(!pageExists(pagerep)) {
    //  throw new WikiPageNotFoundException(WebLocal.getI18n().str("providers.file.page_not_exist",
    // pagerep), pagerep, engine.getName());
    // }
    if (version == WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY) {
      wp =
          new WikiProvidedObject(
              pagerep,
              new Date(_f.lastModified()),
              _f.size(),
              WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY);
    } else if (!(helper.isRepositoryInfoAvailable(_f))) {
      wp =
          new WikiProvidedObject(
              pagerep, new Date(_f.lastModified()), _f.size(), helper.getInitialVersion());
    } else {
      wp = getPageFromRepository(pagerep, version);
    }
    return wp;
  }

  public Reader getPageReader(WikiProvidedObject wp) throws Exception {
    BufferedReader br = null;
    VirtualFile _f = getFile(wp.getName());
    // if a specific version is given, then get from repository. Else get from filesystem.
    if (wp.getVersion() < helper.getInitialVersion() || !(helper.isRepositoryInfoAvailable(_f))) {
      // System.out.println("helper.isRepositoryInfoAvailable: " + wp.getVersion() + ": " +
      // _f.getPath() + ": " + helper.isRepositoryInfoAvailable(_f));
      if (_f != null && _f.exists()) {
        br = new BufferedReader(new InputStreamReader(_f.getInputStream(), encoding));
      } else {
        throw new WikiPageNotFoundException(
            WebLocal.getI18n().str("providers.file.page_not_exist", wp.getName()),
            wp.getName(),
            engine.getName());
      }
    } else {
      br = getPageReaderFromRepository(wp);
    }
    return br;
  }

  public void savePage(String pagerep, String text, Properties attributes) throws Exception {
    FSFileInfo info = null;
    try {
      testPageName(pagerep);
      VirtualWritableFile _f = (VirtualWritableFile) getFile(pagerep);
      // System.out.println("savePage: attributes: " + attributes);
      String logmsg = FSUtils.propertiesToChange(attributes, pagerep, null);
      // System.out.println("savePage: logmsg: " + logmsg);
      info = helper.makeChange(logmsg, _f);
      helper.addOrEdit(info, text);
      helper.submit(info);
    } finally {
      if (info != null) helper.baseState(info);
    }
  }

  /** This implementation deletes a page */
  public void deletePage(String pagerep, Properties atts) throws Exception {
    FSFileInfo info = null;
    try {
      VirtualWritableFile _f = (VirtualWritableFile) getFile(pagerep);
      if (_f != null && _f.exists()) {
        String logmsg = FSUtils.propertiesToChange(atts, pagerep, null);
        info = helper.makeChange(logmsg, _f);
        helper.delete(info);
        helper.submit(info);
      }
    } finally {
      if (info != null) helper.baseState(info);
    }
  }

  // This is really an admin action. makeChange and submit have no business here.
  public void deletePageVersions(String pagerep, Properties atts, OxygenIntRange versions)
      throws Exception {
    try {
      VirtualWritableFile _f = (VirtualWritableFile) getFile(pagerep);
      if (_f != null && _f.exists()) {
        String logmsg = FSUtils.propertiesToChange(atts, pagerep, null);
        helper.deleteVersions(_f, logmsg, versions);
      }
    } finally {
    }
  }

  public WikiProvidedObject[] getPageVersionHistory(String pagerep) throws Exception {
    try {
      VirtualFile _f = getFile(pagerep);
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
        wps[i] = new WikiProvidedObject(pagerep, p4file.date, p4file.size, p4file.rev);
        FSUtils.setPageAttributes(wps[i], p4file.description);
      }
      return wps;
    } finally {
    }
  }

  public OxygenRevision getPageRevision(String pagerep, int r1, int r2) throws Exception {
    VirtualFile _f = null;
    try {
      // System.out.println("+ getPageRevision: " + pagerep + " " + r1 + " " + r2);
      WikiProvidedObject wp = null;
      if (r1 < getInitialVersion() && r2 < getInitialVersion()) {
        wp = getPage(pagerep, WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);
        r2 = wp.getVersion();
        r1 = Math.max(getInitialVersion(), r2 - 1);
      } else if (r1 < getInitialVersion()) {
        wp = getPage(pagerep, WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);
        r1 = wp.getVersion();
      } else if (r2 < getInitialVersion()) {
        wp = getPage(pagerep, WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);
        r2 = wp.getVersion();
      }
      // System.out.println("- getPageRevision: " + pagerep + " " + r1 + " " + r2);
      OxygenRevision wrev = null;
      if (r1 == r2) {
        wrev = new OxygenRevision(r1, r2, WebLocal.getI18n());
      } else {
        // Note: There's a chance that r1 or r2 might not exist, and if so, we show nothing
        _f = getFile(pagerep);
        InputStream is1 = helper.getFileContentsAsStream(_f, r1);
        InputStream is2 = helper.getFileContentsAsStream(_f, r2);
        if (is1 == null || is2 == null) {
          wrev = new OxygenRevision(r1, r2, WebLocal.getI18n());
        } else {
          String s1 = OxygenUtils.getTextContents(new InputStreamReader(is1), true);
          String s2 = OxygenUtils.getTextContents(new InputStreamReader(is2), true);
          wrev = OxygenRevision.getDiff(s1, s2, WebLocal.getI18n());
          wrev.setOriginalVersion(r1);
          wrev.setRevisedVersion(r2);
        }
      }
      return wrev;
      // } catch(Exception exc) {
      // System.out.println("---- Exception: " + engine.getName() + " - " + _f.getPath() + " - " +
      // r1 + " - " + r2 + " ---- " + exc);
      // throw exc;
    } finally {
    }
  }

  public String[] getPageNames(String parentPageRep, int maxdepth, boolean deleted)
      throws Exception {
    String[] sa = new String[0];
    HashSet hs = new HashSet();
    if (deleted) {
      sa = helper.lookupNames(basefile.getChild(parentPageRep), maxdepth, true);
      // System.out.println("FSPageProvider: maxdepth: " + maxdepth + " parentPageRep: " +
      // parentPageRep + " sa: " + Arrays.asList(sa));
      int pagetxtlen = PAGENAME.length() + 1; // +1 handles the /
      String prefix =
          (StringUtils.isBlank(parentPageRep) ? "" : (StringUtils.trim(parentPageRep, '/') + '/'));
      for (int i = 0; i < sa.length; i++) {
        if (sa[i].endsWith("/" + PAGENAME)) {
          if (notPagePattern == null || !(notPagePattern.matcher(sa[i]).matches())) {
            hs.add(prefix + (sa[i].substring(0, sa[i].length() - pagetxtlen)));
          }
        }
      }
    } else {
      hs.addAll(getPageNamesFromDisk(parentPageRep, maxdepth));
    }

    sa = (String[]) hs.toArray(new String[0]);
    Arrays.sort(sa);
    return sa;
  }

  public int getInitialVersion() {
    return helper.getInitialVersion();
  }

  protected WikiProvidedObject getPageFromRepository(String pagerep, int version) throws Exception {
    try {
      VirtualFile _f = getFile(pagerep);
      FSFileInfo p4file = helper.getEntryInfo(_f, version);
      OxygenUtils.debug("P4FileInfo: pagename: " + pagerep + " getHeadTime(): " + p4file.date);
      WikiProvidedObject wp = new WikiProvidedObject(pagerep, p4file.date, p4file.size, p4file.rev);
      FSUtils.setPageAttributes(wp, p4file.description);
      return wp;
    } finally {
    }
  }

  protected BufferedReader getPageReaderFromRepository(WikiProvidedObject wp) throws Exception {
    try {
      VirtualFile _f = getFile(wp.getName());
      return (new BufferedReader(
          new InputStreamReader(helper.getFileContentsAsStream(_f, wp.getVersion()))));
    } finally {
    }
  }

  protected VirtualFile getFile(String pagerep) throws Exception {
    // if(basefile == null || pagerep == null) {
    //  //System.out.println("basefile: " + basefile + " | pagerep: " + pagerep);
    //  Thread.dumpStack();
    // }
    pagerep = (StringUtils.isBlank(pagerep) ? PAGENAME : (pagerep + "/" + PAGENAME));
    return basefile.getChild(pagerep);
  }

  protected void testPageName(String pagerep) throws Exception {
    if (notPagePattern != null && notPagePattern.matcher(pagerep).matches()) {
      throw new WikiException(
          WebLocal.getI18n()
              .str("providers.file.not_page_pattern_error", notPagePattern.pattern()));
    }
  }

  boolean isPage(VirtualFile f) {
    try {
      if (f.isDirectory()) {
        // String fName = f.getName();
        return isPageName(FSUtils.extractPagename(f.getPath(), basefile), f, true);
      }
    } catch (Exception exc) {
      OxygenUtils.error(exc);
    }
    return false;
  }

  boolean isPageName(String fNameFromPath, boolean checkPageNotDeleted) throws Exception {
    return isPageName(fNameFromPath, basefile.getChild(fNameFromPath), checkPageNotDeleted);
  }

  boolean isPageName(String fNameFromPath, VirtualFile f, boolean checkPageNotDeleted)
      throws Exception {
    // System.out.println("fPath: " + fNameFromPath);
    // only check PAGE.TXT (since that files existence means that we have a match)
    boolean b = (notPagePattern == null || !(notPagePattern.matcher(fNameFromPath).matches()));
    if (b && checkPageNotDeleted) {
      // && pattern.matcher(fNameFromPath).matches()
      VirtualFile nonbaselinefile = f.getChild(PAGENAME);
      b = (nonbaselinefile != null && nonbaselinefile.exists());
    }
    return b;
  }

  private List getPageNamesFromDisk(String parentPageRep0, int maxdepth) throws Exception {
    final String parentPageRep = StringUtils.trim(parentPageRep0, '/');

    VirtualFile newbasefile = getFile(parentPageRep).getParent();
    VirtualFile[] files = newbasefile.list(aPageFileFilter, maxdepth);
    // System.out.println("newbasefile: " + newbasefile + ", files: " + Arrays.asList(files));
    List wps = new ArrayList();
    String basefilepath = newbasefile.getPath().replace('\\', '/');
    int basefilepathlen = basefilepath.length();
    for (int i = 0; i < files.length; i++) {
      String wikipagename =
          FSUtils.extractPagename(files[i].getPath(), basefilepath, basefilepathlen);
      wps.add(wikipagename);
    }
    return wps;
  }
}
