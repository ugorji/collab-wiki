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
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiPageReviewProvider;
import net.ugorji.oxygen.wiki.WikiProvidedObject;

public class FSPageReviewProvider implements WikiPageReviewProvider {

  public static final String PAGEREVIEWNAME_PREFIX = "REVIEW.";
  public static final Pattern reviewFileNamePattern = Pattern.compile("REVIEW\\.[0-9]+\\.TXT");

  protected WikiCategoryEngine engine;
  protected VirtualFile basefile;
  protected String encoding = null;
  protected FSHelper helper;
  private VirtualFileFilter aReviewFileFilter;

  public void prepare(WikiCategoryEngine wce) throws Exception {
    engine = wce;
    encoding = engine.getCharacterEncoding();
    basefile = FSUtils.getBaseFile(wce);
    helper = FSUtils.retrieveFSHelper(engine);
    aReviewFileFilter =
        new VirtualFileFilter() {
          public boolean accept(VirtualFile vf) {
            return (reviewFileNamePattern.matcher(vf.getName()).matches());
          }
        };
  }

  public void close() {
    CloseUtils.close(basefile);
    basefile = null;
    CloseUtils.close(helper);
    helper = null;
  }

  public Reader getPageReviewReader(String pagerep, WikiProvidedObject wpr) throws Exception {
    VirtualFile _f = getFile(pagerep, wpr.getName());
    BufferedReader br = new BufferedReader(new InputStreamReader(_f.getInputStream(), encoding));
    return br;
  }

  public void savePageReview(String pagerep, String text, Properties attributes) throws Exception {
    FSFileInfo info = null;
    try {
      // System.out.println("attributes: " + attributes);
      long currtime = System.currentTimeMillis();
      String reviewname = String.valueOf(currtime);
      String logmsg = FSUtils.propertiesToChange(attributes, pagerep, null);
      VirtualWritableFile _f = (VirtualWritableFile) getFile(pagerep, reviewname);
      info = helper.makeChange(logmsg, _f);
      helper.addOrEdit(info, text);
      helper.submit(info);
    } finally {
      if (info != null) helper.baseState(info);
    }
  }

  protected VirtualFile getFile(String pagerep, String reviewname) throws Exception {
    return basefile.getChild(pagerep + "/" + PAGEREVIEWNAME_PREFIX + reviewname + ".TXT");
  }

  protected VirtualFile getFile(String pagerep) throws Exception {
    return basefile.getChild(pagerep);
  }

  protected void setPageReviewAttributes(String pagerep, WikiProvidedObject wpr) throws Exception {
    VirtualFile _f = getFile(pagerep, wpr.getName());
    if (_f != null && _f.exists()) {
      FSFileInfo info = helper.getEntryInfo(_f, FSHelper.IMPOSSIBLE_NEGATIVE_VERSION);
      Properties atts = new Properties();
      FSUtils.changeToProperties(info.description, atts);
      wpr.setAttributes(atts);
    }
  }

  public void deletePageReview(String pagerep, String reviewname, Properties atts)
      throws Exception {
    FSFileInfo info = null;
    try {
      VirtualWritableFile _f = (VirtualWritableFile) getFile(pagerep, reviewname);
      if (_f.exists()) {
        String logmsg = FSUtils.propertiesToChange(atts, pagerep, reviewname);
        info = helper.makeChange(logmsg, _f);
        helper.delete(info);
        helper.submit(info);
      }
    } finally {
      if (info != null) helper.baseState(info);
    }
  }

  public WikiProvidedObject getPageReview(String pagerep, String reviewname, int version)
      throws Exception {
    WikiProvidedObject wpr =
        new WikiProvidedObject(reviewname, new Date(Long.parseLong(reviewname)), version);
    if (version == WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY) {
      setPageReviewAttributes(pagerep, wpr);
    }
    return wpr;
  }

  public String[] getPageReviewNames(String pagerep, boolean deleted) throws Exception {
    String[] sa = new String[0];
    HashSet hs = new HashSet();
    if (deleted) {
      sa = helper.lookupNames(getFile(pagerep), 1, true);
      for (int i = 0; i < sa.length; i++) {
        if (reviewFileNamePattern.matcher(sa[i]).matches()) {
          hs.add(sa[i]);
        }
      }
    } else {
      hs.addAll(getPageReviewNamesFromDisk(pagerep));
    }

    sa = (String[]) hs.toArray(new String[0]);
    Arrays.sort(sa);
    return sa;
  }

  private List getPageReviewNamesFromDisk(String pagerep) throws Exception {
    VirtualFile _f = basefile.getChild(pagerep);
    // System.out.println("- Review base file: " + _f.getFile());
    VirtualFile[] _files = _f.list(aReviewFileFilter, 1);
    if (_files == null) {
      _files = new VirtualFile[0];
    }
    Arrays.sort(_files);
    List wps = new ArrayList();
    for (int i = (_files.length - 1); i >= 0; i--) {
      // System.out.println("-- Review file: " + _files[i].getFile());
      try {
        String _name = _files[i].getName();
        int idx = _name.indexOf(".");
        int idx2 = _name.indexOf(".", idx + 1);

        _name = _name.substring(idx + 1, idx2);
        wps.add(_name);
      } catch (Exception exc) {
        OxygenUtils.error(exc);
      }
    }
    // System.out.println("Review Names: " + wps);
    return wps;
  }
}

  /*
  public WikiProvidedObject[] getPageReviews(String pagerep, boolean detailsNecessary) throws Exception {
    VirtualFile _f = basefile.getChild(pagerep);
    VirtualFileFilter _vff = new VirtualFileFilter() {
        public boolean accept(VirtualFile vf) {
          return (reviewFileNamePattern.matcher(vf.getName()).matches());
        }
      };
    VirtualFile[] _files = _f.list(_vff);
    if(_files == null) {
      _files = new VirtualFile[0];
    }
    Arrays.sort(_files);
    List wps = new ArrayList();
    for(int i = (_files.length - 1); i >= 0; i--) {
      try {
        String _name = _files[i].getName();
        int idx = _name.indexOf(".");
        int idx2 = _name.indexOf(".", idx+1);

        _name = _name.substring(idx + 1, idx2);
        WikiProvidedObject wpr = new WikiProvidedObject(_name, new Date(Long.parseLong(_name)), -1);
        if(detailsNecessary) {
          setPageReviewAttributes(pagerep, wpr);
        }
        wps.add(wpr);
      } catch(Exception exc) {
        WikiUtils.info(exc);
      }
    }
    return (WikiProvidedObject[])wps.toArray(new WikiProvidedObject[0]);
  }
  */
