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
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ugorji.oxygen.io.VirtualFile;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.OxygenCacheManager;
import net.ugorji.oxygen.util.OxygenEngine;
import net.ugorji.oxygen.util.OxygenIntRange;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.ProcessHandler;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.OxygenWebException;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiException;
import net.ugorji.oxygen.wiki.WikiLocal;

/**
 * This class will also: put stuff in the cache - we will store info about a change's description, -
 * and fstat for a file - for each submit/delete, add the fstat for affected files and the change's
 * desc - this will allow us faster access add a TimerTask to timeBasedMgr that, - for each p4
 * changes which have been pending for more than 2 hours - removes empty ones - submits ones with
 * files in them The following must be in the properties object - p4.executable, p4.user,
 * p4.password, p4.host, p4.client
 *
 * <p>p4.pattern.client.from = [cC]:/perforcedepotonclient/(.*) p4.pattern.depot.to = //depot/$1
 *
 * <p>p4.pattern.depot.from = //depot/(.*) p4.pattern.client.to = C:/perforcedepotonclient/$1
 *
 * <p>Note: This class will handle when import's are done also.
 */
public class P4FSHelperViaCommandLineExec implements FSHelper {

  private static String lsep = StringUtils.LINE_SEP;
  private static Pattern changeRenamedPattern =
      Pattern.compile(".*?Change (\\d+) renamed change (\\d+) and submitted.*", Pattern.DOTALL);
  private static Pattern filelogInfoLinePattern =
      Pattern.compile("\\.\\.\\. #(\\d+) change (\\d+) .+? on (.+?) by .+?", Pattern.DOTALL);
  private static Pattern changeCreatedLinePattern =
      Pattern.compile("Change (\\d+) created.*", Pattern.DOTALL);
  private static final String ENV_KEY_PREFIX = "net.ugorji.oxygen.wiki.provider.filesystem.p4.env.";
  private static SimpleDateFormat chgDateFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  private static String CACHE_GROUP_FNAMEVERINFO =
      P4FSHelperViaCommandLineExec.class.getName() + ".fnameverinfo";
  private static String CACHE_GROUP_FNAMEVERINFO_VER1 =
      P4FSHelperViaCommandLineExec.class.getName() + ".fnameverinfo.ver1";
  // private static int VERSION_MIN_VALID_NUMBER = 1;

  private Pattern clientPathPatternFrom = null;
  private Pattern depotPathPatternFrom = null;
  private String clientPathPatternTo = null;
  private String depotPathPatternTo = null;

  private Pattern p4filesPattern = null;

  private List initCmdLineArgsWrite;
  private OxygenEngine iwe;
  private String encoding = null;
  private String[] envp = null;

  private VirtualWritableFile basefile = null;

  public P4FSHelperViaCommandLineExec() {}

  public void init(OxygenEngine iwe0, VirtualFile basefile0) throws Exception {
    if (basefile0 != null && !(basefile0 instanceof VirtualWritableFile)) {
      throw new WikiException(WebLocal.getI18n().str("providers.p4.archive_not_supported"));
    }
    basefile = (VirtualWritableFile) basefile0;

    iwe = iwe0;
    Properties props = iwe.getProperties();
    ensureAllPropertiesExist(props);

    encoding = props.getProperty(WikiConstants.ENCODING_KEY);

    clientPathPatternFrom = Pattern.compile(props.getProperty("p4.pattern.client.from"));
    depotPathPatternFrom = Pattern.compile(props.getProperty("p4.pattern.depot.from"));
    depotPathPatternTo = props.getProperty("p4.pattern.depot.to");
    clientPathPatternTo = props.getProperty("p4.pattern.client.to");
    // System.out.println("clientFilePattern: " + clientFilePattern + " -- " +
    // "depotPathReplacement: " + depotPathReplacement);

    p4filesPattern = Pattern.compile("(.*?)#(\\d+) - (\\w+) .+");

    initCmdLineArgsWrite = new ArrayList();
    initCmdLineArgsWrite.add(props.getProperty("p4.executable"));
    initCmdLineArgsWrite.add("-u");
    initCmdLineArgsWrite.add(props.getProperty("p4.user"));
    if (!StringUtils.isBlank(props.getProperty("p4.password"))) {
      initCmdLineArgsWrite.add("-P");
      initCmdLineArgsWrite.add(props.getProperty("p4.password"));
    }
    initCmdLineArgsWrite.add("-H");
    initCmdLineArgsWrite.add(props.getProperty("p4.host"));
    initCmdLineArgsWrite.add("-p");
    initCmdLineArgsWrite.add(props.getProperty("p4.port"));
    initCmdLineArgsWrite.add("-c");
    initCmdLineArgsWrite.add(props.getProperty("p4.client"));

    // System.out.println("initCmdLineArgsWrite: " + initCmdLineArgsWrite);
    Properties pp = new Properties();
    OxygenUtils.extractProps(props, pp, ENV_KEY_PREFIX, true);
    // System.out.println("pp: " + pp);
    if (!pp.isEmpty()) {
      List list = new ArrayList();
      for (Enumeration enum0 = pp.propertyNames(); enum0.hasMoreElements(); ) {
        String k = (String) enum0.nextElement();
        String v = pp.getProperty(k);
        list.add(k + "=" + v);
      }
      envp = (String[]) list.toArray(new String[0]);
    }

    // preload the cache
    preloadCache();
  }

  private void ensureAllPropertiesExist(Properties p) throws Exception {
    if (p.getProperty("p4.pattern.client.from") == null
        || p.getProperty("p4.pattern.depot.to") == null
        || p.getProperty("p4.pattern.depot.from") == null
        || p.getProperty("p4.pattern.client.to") == null
        || p.getProperty("p4.executable") == null
        || p.getProperty("p4.user") == null
        || p.getProperty("p4.host") == null
        || p.getProperty("p4.port") == null
        || p.getProperty("p4.client") == null) {
      throw new Exception("All p4.* properties are not available in the initialization");
    }
  }

  public String getMandatoryNonPagePatternRegex() {
    return null;
  }

  public String getMandatoryNonAttachmentPatternRegex() {
    return FSPageProvider.PAGENAME + "|" + "REVIEW\\.[0-9]+\\.TXT" + "|" + ".*?#.*";
  }

  public void close() {
    // for some reason, this close is being ca
    // iwe = null;
    // initCmdLineArgsWrite = null;
  }

  public int getInitialVersion() {
    return 1;
  }

  public void delete(FSFileInfo info) throws Exception {
    VirtualWritableFile _vf = (VirtualWritableFile) info.file;
    File _f = _vf.getFile();
    if (_f == null || !(_f.exists())) {
      return;
    }
    String[] args = {"delete", "-c", String.valueOf(info.change), _f.getAbsolutePath()};
    FSUtils.CmdResultInfo p4res =
        FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null, true, false);
    if (p4res.errstr != null && p4res.errstr.indexOf(" not on client") != -1) {
      _vf.delete();
    } else {
      replaceInCache(info.file);
    }
  }

  public void addOrEdit(FSFileInfo info, File newContent) throws Exception {
    addOrEdit0(info, newContent);
  }

  public void addOrEdit(FSFileInfo info, String newContent) throws Exception {
    addOrEdit0(info, newContent);
  }

  public void submit(FSFileInfo info) throws Exception {
    String[] args = {"submit", "-c", String.valueOf(info.change)};
    FSUtils.CmdResultInfo p4res =
        FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null, false, false);
    if (p4res.errstr != null && p4res.errstr.indexOf("No files to submit") != -1) {
      args = new String[] {"change", "-d", String.valueOf(info.change)};
      p4res = FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null, true, true);
    } else {
      // int chgnum = info.change;
      // check if the change was renumbered.
      // System.out.println("Submit outstr: " + p4res.outstr);
      Matcher m = changeRenamedPattern.matcher(p4res.outstr);
      if (m.matches()) {
        info.change = Integer.parseInt(m.group(2));
        // System.out.println("Change from: " + info.change + " to " + chgnum);
      }
      // System.out.println("On submit, change info: " + chginfo);
      replaceInCache(info.file);
    }
  }

  public FSFileInfo makeChange(String logmsg, VirtualWritableFile _f) throws Exception {
    String s = null;
    // prepend tab to each line of logmsg
    StringBuffer newlogmsgbuf = new StringBuffer(logmsg.length());
    BufferedReader br = new BufferedReader(new StringReader(logmsg));
    while ((s = br.readLine()) != null) {
      newlogmsgbuf.append("\t").append(s).append(lsep);
    }
    CloseUtils.close(br);
    logmsg = newlogmsgbuf.toString();
    s =
        "Change: new"
            + lsep
            + "Client: "
            + iwe.getProperty("p4.client", null)
            + lsep
            + "User: "
            + iwe.getProperty("p4.user", null)
            + lsep
            + "Description: "
            + lsep
            + logmsg
            + lsep;
    // if windows, append Ctrl-Z to the input
    if (OxygenUtils.isWindows()) {
      s = s + "\032\n\032" + lsep;
    }
    String[] args = {"change", "-i"};
    FSUtils.CmdResultInfo p4res =
        FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, s, true, true);

    FSFileInfo info = new FSFileInfo();
    info.description = logmsg;
    info.file = _f;
    Matcher m = FSUtils.matchPattern(changeCreatedLinePattern, p4res.outstr);
    info.change = Integer.parseInt(m.group(1));
    if (info.change == -1) {
      throw new Exception("[Output]: " + p4res.outstr + "[Error]: " + p4res.errstr);
    }
    // System.out.println("change info: " + info);
    return info;
  }

  public FSFileInfo getEntryInfo(VirtualFile _f, int version) throws Exception {
    File file = ((VirtualWritableFile) _f).getFile();
    FSFileInfo info = null;
    if (version == getInitialVersion()) {
      info = (FSFileInfo) cachemgr().get(CACHE_GROUP_FNAMEVERINFO_VER1, getCacheKey(_f));
    } else if (version < getInitialVersion()) {
      info = (FSFileInfo) cachemgr().get(CACHE_GROUP_FNAMEVERINFO, getCacheKey(_f));
    }
    if (info == null) {
      Map map = preloadFsInfo(_f, version);
      info = (FSFileInfo) map.get(new Integer(version));
    }
    return info;
  }

  public List getEntryLog(VirtualFile f) throws Exception {
    Map map = preloadFsInfo(f, IMPOSSIBLE_POSITIVE_VERSION);
    FSFileInfo[] versions = (FSFileInfo[]) map.values().toArray(new FSFileInfo[0]);
    Arrays.sort(versions);
    return Arrays.asList(versions);
  }

  public BufferedInputStream getFileContentsAsStream(VirtualFile _f, int rev) throws Exception {
    String fname = _f.getPath().replace('\\', '/');
    fname = getDepotPath(fname);
    fname = (rev < getInitialVersion()) ? fname : (fname + "#" + rev);

    // sanity check - to ensure that we can see the file
    String[] args = new String[] {"fstat", fname};
    FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null, true, true);

    // now the real deal. Everything is fine ... this exists
    args = new String[] {"print", "-q", fname};

    Process p = FSUtils.cmdexecAndReturnProcess(initCmdLineArgsWrite, args, envp, null);
    return new BufferedInputStream((new ProcessHandler(p)).manageInputStream(0, null));
  }

  public boolean isRepositoryInfoAvailable(VirtualFile _f) throws Exception {
    try {
      // if there's no fstat, then return false
      // else if latest action is delete, then return true
      if (_f != null) {
        FSFileInfo info = getEntryInfo(_f, IMPOSSIBLE_NEGATIVE_VERSION);
        return (info != null);
      }
    } catch (Exception exc) {
      // exc.printStackTrace();
      OxygenUtils.error(exc);
    }
    return false;
  }

  public void baseState(FSFileInfo info) {
    // MarkupUtils.info(new Exception(">>> thanks for a bogus exception."));
    try {
      String fileName = info.file.getPath();
      String[] args = {"revert", fileName};
      FSUtils.CmdResultInfo p4res =
          FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null, false, false);
    } catch (Exception exc) {
      OxygenUtils.error(exc);
    }
  }

  public String[] lookupNames(final VirtualFile f, int maxdepth, final boolean deleted)
      throws Exception {
    // p4 files ... returns the files we want. pass ... or pass %%1 to get just for this directory,
    // or all through
    // depot-file-location#rev - action change change# (filetype)
    // find all the pages, handle deleted arg,
    // ... extract file name from the depot name, trim out those beyond the maxdepth, return
    String fileName = f.getPath();
    String[] args = {"files", fileName + "/" + (maxdepth <= 1 ? "%%1" : "...")};
    // FSUtils.CmdResultInfo p4res = FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null,
    // true, true);
    // BufferedReader br = new BufferedReader(new StringReader(p4res.outstr));

    Process p = FSUtils.cmdexecAndReturnProcess(initCmdLineArgsWrite, args, envp, null);
    ProcessHandler ph = new ProcessHandler(p);
    StringWriter err = new StringWriter();
    BufferedReader br = new BufferedReader(new InputStreamReader(ph.manageInputStream(0, err)));

    try {
      List list = new ArrayList();
      String s = null;
      while ((s = br.readLine()) != null) {
        Matcher m = FSUtils.matchPattern(p4filesPattern, s);
        String sAction = m.group(3);
        boolean deletedFile = sAction.equals("delete");
        if ((deleted && deletedFile) || (!deleted && !deletedFile)) {
          String sPath = getClientPath(m.group(1)).replace('\\', '/');
          sPath = sPath.substring(f.getPath().replace('\\', '/').length());
          sPath = StringUtils.trim(sPath, '/');
          if (countNumberOfSlashes(sPath) < maxdepth) {
            list.add(sPath);
          }
        }
      }
      ph.waitTillDone().check(0, err.toString());
      // System.out.println("list: " + list);
      return (String[]) list.toArray(new String[0]);
    } finally {
      CloseUtils.close(br);
    }
  }

  public void deleteVersions(VirtualWritableFile vf, String logmsg, OxygenIntRange versions)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  public synchronized void preloadCache() throws Exception {
    // if basefile is null OR does not exist OR is empty, do nothing
    if (basefile == null || !basefile.exists() || basefile.list(null, 1).length == 0) {
      return;
    }
    long lcurr = System.currentTimeMillis();
    // possible that security manager prevents running a process from any arbitrary directory.
    String depotpath = getDepotPath(basefile.getPath());
    OxygenUtils.info("Preloading P4 cache for initial and head versions: " + basefile.getPath());
    String[] args = new String[] {"filelog", "-l", "-t", "-m1", depotpath + "/..."};
    preloadCache(args, CACHE_GROUP_FNAMEVERINFO);
    args = new String[] {"filelog", "-l", "-t", "-m1", depotpath + "/...#" + getInitialVersion()};
    preloadCache(args, CACHE_GROUP_FNAMEVERINFO_VER1);
    OxygenUtils.info(
        "Done Preloading P4 cache in: " + (System.currentTimeMillis() - lcurr) + " ms");
  }

  private void addOrEdit0(FSFileInfo info, Object newContent) throws Exception {
    VirtualWritableFile _vf = (VirtualWritableFile) info.file;
    File _f = _vf.getFile();
    String fileName = _f.getAbsolutePath();
    // if the file is not available on disk, then p4 add it.
    // if(isRepositoryInfoAvailable(_vf)) {
    if (_vf.exists()) {
      String[] args = {"edit", "-c", String.valueOf(info.change), fileName};
      FSUtils.CmdResultInfo p4res =
          FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null, true, true);
      FSUtils.preSaveOverwrite(_vf, newContent, encoding, false);
    } else {
      _f.getParentFile().mkdirs();
      FSUtils.makeFileWritable(_f, envp, null);
      FSUtils.preSaveOverwrite(_vf, newContent, encoding, false);
      String[] args = {"add", "-c", String.valueOf(info.change), fileName};
      FSUtils.CmdResultInfo p4res =
          FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null, true, true);
    }
  }

  private String getDepotPath(VirtualFile _f) {
    String s = _f.getPath().replace('\\', '/');
    return getDepotPath(s);
  }

  private String getDepotPath(String clientPath) {
    String depotPath = clientPath;
    Matcher m = clientPathPatternFrom.matcher(clientPath);
    if (m.matches()) {
      // System.out.println("Client path matched ... will be replaced");
      depotPath = m.replaceAll(depotPathPatternTo);
    }
    // System.out.println("clientPath: " + clientPath + " -- " + "depotPath: " + depotPath);
    return depotPath;
  }

  private String getClientPath(String depotPath) {
    String clientPath = depotPath;
    Matcher m = depotPathPatternFrom.matcher(clientPath);
    if (m.matches()) {
      // System.out.println("Client path matched ... will be replaced");
      clientPath = m.replaceAll(clientPathPatternTo);
    }
    // System.out.println("clientPath: " + clientPath + " -- " + "depotPath: " + depotPath);
    return clientPath;
  }

  private int countNumberOfSlashes(String s) {
    s = StringUtils.trim(s, '/');
    int numSlashes = 0;
    int slen = s.length();
    for (int i = 0; i < slen; i++) {
      if (s.charAt(i) == '/') {
        numSlashes++;
      }
    }
    return numSlashes;
  }

  private void replaceInCache(VirtualFile _f) throws Exception {
    String fname = getCacheKey(_f);
    cachemgr().remove(CACHE_GROUP_FNAMEVERINFO, fname);
    getEntryInfo(_f, IMPOSSIBLE_NEGATIVE_VERSION);
  }

  private String getCacheKey(VirtualFile _f) throws Exception {
    return getCacheKey(_f.getPath());
    // fname = getDepotPath(fname);
  }

  private String getCacheKeyGivenDepotPath(String depotPath) throws Exception {
    return getCacheKey(getClientPath(depotPath));
  }

  /**
   * On windows, always store keys in the cache in lowercase This way, drive letters et al will not
   * cause issues Also, always store the client path in the cache, since the same depotPath might
   * point to different files (assuming diff sections have different depots)
   */
  private String getCacheKey(String fname) {
    fname = fname.replace('\\', '/');
    if (OxygenUtils.isWindows()) {
      fname = fname.toLowerCase();
    }
    return fname;
  }

  private OxygenCacheManager cachemgr() {
    return WikiLocal.getWikiEngine().getCacheManager();
  }

  /*
   * version is one of IMPOSSIBLE_POSITIVE_VERSION (meaning get all versions),
   *                   x < getInitialVersion() (meaning get latest version)
   *                   x >= getInitialVersion() (meaning get this specific version
   */
  private Map preloadFsInfo(VirtualFile _f, int version) throws Exception {
    // System.out.println("preloadFsInfo: " + _f.getPath() + " : " + version);
    Map map = new HashMap();
    String depotPath = getDepotPath(_f);
    String[] args = null;
    if (version == IMPOSSIBLE_POSITIVE_VERSION) {
      args = new String[] {"filelog", "-l", "-t", depotPath};
    } else if (version < getInitialVersion()) {
      args = new String[] {"filelog", "-l", "-t", "-m1", depotPath};
    } else {
      args = new String[] {"filelog", "-l", "-t", "-m1", depotPath + "#" + version};
    }
    // System.out.println("P4: " + Arrays.asList(args));
    int maxHeadRev = getInitialVersion() - 1;

    // FSUtils.CmdResultInfo p4res = FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null,
    // true, true);
    // BufferedReader br = new BufferedReader(new StringReader(p4res.outstr));

    Process p = FSUtils.cmdexecAndReturnProcess(initCmdLineArgsWrite, args, envp, null);
    ProcessHandler ph = new ProcessHandler(p);
    StringWriter err = new StringWriter();
    BufferedReader br = new BufferedReader(new InputStreamReader(ph.manageInputStream(0, err)));

    try {
      String s = null;
      while ((s = br.readLine()) != null) {
        if (s.startsWith("//")) {
          break;
        }
      }
      while ((s = br.readLine()) != null) {
        while (s != null && s.startsWith("... ...")) {
          s = br.readLine();
        }
        if (s == null) {
          break;
        } else {
          FSFileInfo info = readAInfo(br, s);
          info.file = _f;
          if (info.date != null) {
            maxHeadRev = Math.max(maxHeadRev, info.rev);
            map.put(new Integer(info.rev), info);
          }
        }
      }
      ph.waitTillDone().check(0, err.toString());
    } finally {
      CloseUtils.close(br);
    }
    // if get latest version, and a latest version exists
    if (version != IMPOSSIBLE_POSITIVE_VERSION
        && version < getInitialVersion()
        && maxHeadRev >= getInitialVersion()) {
      FSFileInfo info = (FSFileInfo) map.get(new Integer(maxHeadRev));
      // System.out.println("Putting in cache: " + depotPath);
      cachemgr().put(CACHE_GROUP_FNAMEVERINFO, getCacheKey(_f), info);
      map.put(new Integer(version), info);
    }
    return map;
  }

  // hope it works fine
  private synchronized void preloadCache(String[] args, String cacheGroupName) throws Exception {
    // System.out.println("P4: " + Arrays.asList(args));
    OxygenCacheManager cachemgr = cachemgr();
    // System.out.println("basefile.getFile(): " + basefile.getFile());

    // FSUtils.CmdResultInfo p4res = FSUtils.cmdexecHandleProcess(p, null, true, true, true);
    // BufferedReader br = new BufferedReader(new FileReader(p4res.outfile));

    Process p =
        FSUtils.cmdexecAndReturnProcess(initCmdLineArgsWrite, args, envp, basefile.getFile());
    ProcessHandler ph = new ProcessHandler(p);
    StringWriter err = new StringWriter();
    BufferedReader br = new BufferedReader(new InputStreamReader(ph.manageInputStream(0, err)));

    try {
      String s = null;
      while ((s = br.readLine()) != null) {
        while (s != null && s.startsWith("... ...")) {
          s = br.readLine();
        }
        if (s == null) {
          break;
        } else {
          String depotPath = s.trim();
          s = br.readLine();
          FSFileInfo info = readAInfo(br, s);
          if (info.date != null) {
            // System.out.println("--- cache: " + cacheGroupName + ": " +
            // getCacheKeyGivenDepotPath(depotPath));
            cachemgr.put(cacheGroupName, getCacheKeyGivenDepotPath(depotPath), info);
          }
        }
      }
      ph.waitTillDone().check(0, err.toString());
    } finally {
      CloseUtils.close(br);
      // p4res.outfile.delete();
    }
  }

  private FSFileInfo readAInfo(BufferedReader br, String s) throws Exception {
    FSFileInfo info = new FSFileInfo();
    Matcher m = filelogInfoLinePattern.matcher(s);
    if (!m.matches()) {
      throw new OxygenWebException(
          "Unable to match within Helper: Pattern: " + filelogInfoLinePattern + " && String: " + s);
    }
    info.rev = Integer.parseInt(m.group(1));
    info.change = Integer.parseInt(m.group(2));
    info.date = chgDateFmt.parse(m.group(3));

    s = br.readLine();

    StringBuffer logmsgbuf = new StringBuffer();
    // while(!StringUtils.isBlank(s = br.readLine())) {
    // do it this way, (using while loop designation below) in case some imports and transfers were
    // done (like done for BEA)
    while ((s = br.readLine()) != null && s.startsWith("\t")) {
      s = s.trim();
      if (s.startsWith(WikiConstants.ATTRIBUTE_SIZE)) {
        info.size = Long.parseLong(s.substring(s.indexOf("=") + 1, s.length()));
      }
      logmsgbuf.append(s).append(FSUtils.linesep);
    }
    info.description = logmsgbuf.toString();
    return info;
  }
}

/*

  public FSFileInfo getEntryInfo(VirtualFile f, int version) throws Exception {
    String s = null;
    Map fstat = getFStat(f.getPath(), version, false);
    FSFileInfo info = new FSFileInfo();
    info.file = f;
    info.date = new Date(Long.parseLong((String)fstat.get("headTime")) * 1000l);
    info.rev = Integer.parseInt((String)fstat.get("headRev"));
    info.change = Integer.parseInt((String)fstat.get("headChange"));
    if(!StringUtils.isBlank(s = (String)fstat.get("fileSize"))) {
      info.size = Long.parseLong(s);
    }
    FSFileInfo chginfo = getChangeInfo(info.change);
    info.description = chginfo.description;
    if(chginfo.date != null) {
      info.date = chginfo.date;
    }
    return info;
  }

  public List getEntryLog(VirtualFile f) throws Exception {
    Map fstat = getFStat(f.getPath(), IMPOSSIBLE_VERSION, false);
    int headrev = Integer.parseInt((String)fstat.get("headRev"));
    List files = new ArrayList(headrev);
    for(int i = headrev; i >= getInitialVersion(); i--) {
      FSFileInfo info = getEntryInfo(f, i);
      files.add(info);
    }
    return files;
  }

  public boolean isRepositoryInfoAvailable(VirtualFile _f) throws Exception {
    try {
      //if there's no fstat, then return false
      //else if latest action is delete, then return true
      if(_f != null) {
        getFStat(_f.getPath(), IMPOSSIBLE_VERSION, false);
        return true;
      }
    } catch(Exception exc) {
      //exc.printStackTrace();
      OxygenUtils.error(exc);
    }
    return false;
  }

  private FSFileInfo getChangeInfo(int changenum) throws Exception {
    //System.out.println("cachemgr: " + cachemgr);
    OxygenCacheManager cachemgr = WikiLocal.getWikiEngine().getWikiCacheManager();
    FSFileInfo chginfo = (FSFileInfo)cachemgr.get(CACHE_GROUP_CHANGE, String.valueOf(changenum));
    if(chginfo == null) {
      chginfo = new FSFileInfo();
      chginfo.change = changenum;
      String[] args = {"describe", "-s", String.valueOf(changenum)};
      FSUtils.CmdResultInfo p4res = FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null, true, true);
      //if(p4res.errstr != null && p4res.errstr.indexOf("no such changelist") != -1) {

      StringBuffer sb = null;
      BufferedReader br = new BufferedReader(new StringReader(p4res.outstr));
      String s = null;
      int idx = 0;
      boolean descAdding = false;
      boolean affectedFiles = false;
      while((s = br.readLine()) != null) {
        if(s.startsWith("Jobs fixed ") || s.startsWith("\tJobs fixed ")) {
          break;
        }
        if(s.startsWith("Affected file") || s.startsWith("\tAffected file")) {
          affectedFiles = true;
          descAdding = false;
        }
        if(s.startsWith("Change ") || s.startsWith("\tChange ")) {
          descAdding = true;
          idx = s.indexOf(" on ");
          if(idx != -1) {
            s = s.substring(idx + 4).trim();
            chginfo.date = chgDateFmt.parse(s);
          }
          sb = new StringBuffer();
          continue;
        }
        if(descAdding) {
          sb.append(s).append(lsep);
        }
        if(affectedFiles) {
          Matcher m = affectedFilesPattern.matcher(s);
          if(m.matches()) {
            chginfo.file = new VirtualPlainFile(new File(getClientPath(m.group(1))));
          }
        }
      }
      chginfo.description = sb.toString();
      cachemgr.put(CACHE_GROUP_CHANGE, String.valueOf(changenum), chginfo);
    }
    //System.out.println("chginfo: " + chginfo);
    return chginfo;
  }

  ///*
  // * Note that, we only want to get the latest version if an IMPOSSIBLE_VERSION was given
  // * We shouldn't always look for all the versions
  //
  private Map getFStat(String fname, int version, boolean replaceInCache) throws Exception {
    fname = fname.replace('\\', '/');
    String depotPath = getDepotPath(fname);
    fname = depotPath;
    fname = (version < getInitialVersion()) ? fname : (fname + "#" + version);

    OxygenCacheManager cachemgr = WikiLocal.getWikiEngine().getWikiCacheManager();
    Map fstat = (Map)cachemgr.get(CACHE_GROUP_FSTAT, fname);
    if(fstat != null && replaceInCache) {
      cachemgr.remove(CACHE_GROUP_FSTAT, fname);
      fstat = null;
    }

    if(fstat == null) {
      preloadFStatsInCache(cachemgr, depotPath, (version < getInitialVersion()));
      fstat = (Map)cachemgr.get(CACHE_GROUP_FSTAT, fname);
    }
    //System.out.println("fstat: " + fstat);
    return fstat;
  }

  private void preloadFStatsInCache(OxygenCacheManager cachemgr, String depotPath, boolean latestVersionOnly) throws Exception {
    String[] args = (latestVersionOnly
                     ? new String[] {"fstat", "-Ol", depotPath}
                     : new String[] {"fstat", "-Of", "-Ol", depotPath});
    FSUtils.CmdResultInfo p4res = FSUtils.cmdexec(initCmdLineArgsWrite, args, envp, null, null, true, true);

    BufferedReader br = new BufferedReader(new StringReader(p4res.outstr));
    String s = null;
    HashMap fstat = new HashMap();
    int maxHeadRev = getInitialVersion() - 1;
    while((s = br.readLine()) != null) {
      fstat = new HashMap();
      while(!StringUtils.isBlank(s = br.readLine())) {
        //System.out.println("s: " + s);
        int pos1 = -1;
        int pos2 = -1;
        pos1 = s.indexOf(' ');
        if(pos1 != -1) {
          pos2 = s.indexOf(' ', pos1+1);
        }
        if(pos2 != -1) {
          fstat.put(s.substring(pos1+1, pos2), s.substring(pos2+1));
        }
      }
      if(!StringUtils.isBlank(s = (String)fstat.get("headRev"))) {
        int headRev = Integer.parseInt(s);
        maxHeadRev = Math.max(maxHeadRev, headRev);
        cachemgr.put(CACHE_GROUP_FSTAT, depotPath + "#" + s, fstat);
      }
    }
    if(maxHeadRev >= getInitialVersion()) {
      cachemgr.put(CACHE_GROUP_FSTAT, depotPath, cachemgr.get(CACHE_GROUP_FSTAT, depotPath + "#" + maxHeadRev));
    }
  }

*/
