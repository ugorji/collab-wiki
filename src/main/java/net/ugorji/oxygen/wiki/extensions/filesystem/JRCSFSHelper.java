/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions.filesystem;

/*
 * Currently, this is not implemented.
 * We keep it here, just so we can store the original code for this in a comment
 */
public abstract class JRCSFSHelper implements FSHelper {}

  /*

  //////////////////////////////////////////////////
  ////////// FROM JRCSPageProvider //////////
  //////////////////////////////////////////////////

  //public static final int VERSION_MIN_VALID_NUMBER = 1;
  public static final String PAGENAME_BASELINE = "PAGE.TXT,v";

  public void savePage(String pagerep, String text, Properties attributes)  throws Exception {
    VirtualFile _f = getBaselineFile(pagerep);

    String[] arr = ToString.stringToArray(text);
    String logmsg = StringUtils.propsToString(attributes);

    Archive archive = null;
    if(_f != null && _f.exists()) {
      archive = new Archive(_f.getName(), _f.getInputStream());
      archive.addRevision(arr, logmsg);
    } else {
      _f = getFile(pagerep);
      if(_f != null && _f.exists()) {
        InputStreamReader isr = new InputStreamReader(_f.getInputStream(), encoding);
        String filecontents = OxygenUtils.getTextContents(isr, true);
        String[] filecontentsArr = ToString.stringToArray(filecontents);
        archive = new Archive(filecontentsArr, "initial revision from stored file", "1.1");
        archive.addRevision(arr, logmsg);
      } else {
        archive = new Archive(arr, logmsg, "1.1");
      }
    }

    _f = getBaselineFile(pagerep);
    _f.getParent().mkdirs();
    archive.save(_f.getOutputStream());

    _f = getFile(pagerep);
    OutputStreamWriter osw = new OutputStreamWriter(_f.getOutputStream(), encoding);
    OxygenUtils.writeTextContents(osw, text, true);
  }

  public void deletePage(String pagerep, Properties atts) throws Exception {
    VirtualFile _f = getFile(pagerep);
    if(_f != null && _f.exists()) {
      _f.delete();
    }
    _f = getBaselineFile(pagerep);
    Archive archive = null;
    if(_f != null && _f.exists()) {
      String logmsg = StringUtils.propsToString(atts);
      //System.out.println("atts: " + atts + " ... logmsg: " + logmsg);
      String[] arr = new String[]{""}; //ToString.stringToArray("");
      archive = new Archive(_f.getName(), _f.getInputStream());
      archive.addRevision(arr, logmsg);
      archive.save(_f.getOutputStream());
    }
  }

  public WikiProvidedObject[] getPageVersionHistory(String pagerep)  throws Exception{
    VirtualFile _f = getBaselineFile(pagerep);
    WikiProvidedObject[] wps = null;
    if(_f != null && _f.exists()) {
      Archive archive = new Archive(_f.getName(), _f.getInputStream());
      int latestver = archive.getRevisionVersion().last();
      Node[] changes = archive.changeLog();
      wps = new WikiProvidedObject[changes.length];
      for(int i = (wps.length - 1), j = 0; i >= 0; i--, j++) {
        long fsize = -1;
        wps[j] = getWikiPageFromNodeChange(pagerep, changes[i], latestver, fsize);
      }
    } else {
      wps = new WikiProvidedObject[1];
      wps[0] = getPage(pagerep, 1);
    }
    return wps;
  }

  public WikiRevision getPageRevision(String pagerep, int r1, int r2) throws Exception {
    VirtualFile _f = getBaselineFile(pagerep);
    Archive archive = new Archive(_f.getName(), _f.getInputStream());
    Object[] arr1 = archive.getRevision("1." + r1);
    Object[] arr2 = archive.getRevision("1." + r2);
    return WikiUtils.getDiff(arr1, arr2);
  }

  protected WikiProvidedObject getPageFromRepository(String pagerep, int version)  throws Exception {
    VirtualFile _f = getBaselineFile(pagerep);
    Archive archive = new Archive(_f.getName(), _f.getInputStream());
    int latestver = archive.getRevisionVersion().last();
    if(version < VERSION_MIN_VALID_NUMBER) {
      version = latestver;
    }
    Node change = null;
    if(version >= VERSION_MIN_VALID_NUMBER) {
      change = archive.findNode(archive.getRevisionVersion("1." + version));
    } else {
      change = archive.findNode(archive.getRevisionVersion());
    }

    long fsize = -1;
    WikiProvidedObject wp = getWikiPageFromNodeChange(pagerep, change, latestver, fsize);
    return wp;
  }

  protected BufferedReader getPageReaderFromRepository(WikiProvidedObject wp) throws Exception {
    VirtualFile _f = getBaselineFile(wp.getName());
    Archive archive = new Archive(_f.getName(), _f.getInputStream());
    Object[] rev = null;
    if(wp.getVersion() >= VERSION_MIN_VALID_NUMBER) {
      rev = archive.getRevision("1." + wp.getVersion(), false);
    } else {
      rev = archive.getRevision();
    }
    String str = ToString.arrayToString(rev);
    BufferedReader br = new BufferedReader(new StringReader(str));
    return br;
  }

  protected boolean isRepositoryInfoAvailable(String pagerep) throws Exception {
    VirtualFile _f = getBaselineFile(pagerep);
    return (_f != null && _f.exists());
  }

  private WikiProvidedObject getWikiPageFromNodeChange
    (String pagerep, Node change, int latestver, long fsize) throws Exception {
    WikiProvidedObject wp = new WikiProvidedObject(pagerep, change.getDate(),
                               fsize, change.getVersion().last());
    String log = change.getLog();
    //WikiUtils.debug("DefFSPageProv: change.getLog(): " + change.getLog());
    Properties atts = new Properties();
    atts.load(new ByteArrayInputStream(log.getBytes()));
    WikiUtils.debug("DefFSPageProv: wp atts from changelog: " + atts);
    wp.setAttributes(atts);
    return wp;
  }

  protected VirtualFile getBaselineFile(String pagerep) throws Exception {
    return basefile.getChild(pagerep + "/" + PAGENAME_BASELINE);
  }

  //////////////////////////////////////////////////
  ////////// FROM JRCSPageReviewProvider //////////
  //////////////////////////////////////////////////

  public void savePageReview(String pagerep, String text, Properties attributes) throws Exception {
    long currtime = System.currentTimeMillis();
    String reviewname = String.valueOf(currtime);
    String[] arr = ToString.stringToArray(text);
    String logmsg = StringUtils.propsToString(attributes);
    Archive archive = new Archive(arr, logmsg, "1.1");

    VirtualFile _f = getBaselineFile(pagerep, reviewname);
    _f.getParent().mkdirs();
    archive.save(_f.getOutputStream());

    _f = getFile(pagerep, reviewname);
    OutputStreamWriter osw = new OutputStreamWriter(_f.getOutputStream(), encoding);
    OxygenUtils.writeTextContents(osw, text, true);
  }

  protected VirtualFile getBaselineFile(String pagerep, String reviewname) throws Exception {
    return basefile.getChild(pagerep + "/" + PAGEREVIEWNAME_PREFIX +
                             reviewname + ".TXT,v");
  }

  protected void setPageReviewAttributes(String pagerep, WikiProvidedObject wpr) throws Exception {
    VirtualFile _f = getBaselineFile(pagerep, wpr.getName());
    if(_f != null && _f.exists()) {
      Archive archive = new Archive(_f.getName(), _f.getInputStream());
      String log = archive.getLog("1.1");
      Properties atts = new Properties();
      atts.load(new ByteArrayInputStream(log.getBytes()));
      wpr.setAttributes(atts);
    }
  }

  public void deletePageReview(String pagerep, String reviewname, Properties atts) throws Exception {
    VirtualFile _f = getFile(pagerep, reviewname);
    if(_f != null && _f.exists()) {
      _f.delete();
    }

    _f = getBaselineFile(pagerep, reviewname);
    if(_f != null && _f.exists()) {
      _f.delete();
    }
  }

  */
