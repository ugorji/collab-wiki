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
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ugorji.oxygen.io.VirtualFile;
import net.ugorji.oxygen.io.VirtualFileFilter;
import net.ugorji.oxygen.io.VirtualPlainFile;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.OxygenEngine;
import net.ugorji.oxygen.util.OxygenIntRange;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.ProcessHandler;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.wiki.WikiConstants;

public class RCSFSHelperViaCommandLineExec extends BaseFSHelper {

  private static final String PATHPREFIX_KEY = "net.ugorji.oxygen.wiki.provider.filesystem.rcs.pathprefix";
  private static final String ENV_KEY_PREFIX = "net.ugorji.oxygen.wiki.provider.filesystem.rcs.env.";
  private static String CACHE_GROUP_FNAMEVERINFO =
      RCSFSHelperViaCommandLineExec.class.getName() + ".fnameverinfo";
  private static String CACHE_GROUP_FNAMEVERINFO_VER1 =
      RCSFSHelperViaCommandLineExec.class.getName() + ".fnameverinfo.ver1";
  private static Pattern NL_PATTERN = Pattern.compile("\r\n|\n|\r");
  private static Pattern rcsFilePathPattern =
      Pattern.compile("(.*?/)" + "RCS" + "/(.*?)" + "(,v)?");
  private static Pattern adminRCSFileLinePattern = Pattern.compile("RCS file\\: RCS/(.+)");
  private static Pattern adminWorkingFileLinePattern = Pattern.compile("Working file\\: (.+)");
  // private static int VERSION_MIN_VALID_NUMBER = 1;
  private static SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

  private String basePathToRCSCommands = "";
  private String[] envp = null;

  public RCSFSHelperViaCommandLineExec() {
    METADATA_DIRECTORY = "RCS";
  }

  public void init(OxygenEngine iwe, VirtualFile basefile) throws Exception {
    super.init(iwe, basefile);
    basePathToRCSCommands = iwe.getProperty(PATHPREFIX_KEY, null);
    if (StringUtils.isBlank(basePathToRCSCommands)) {
      basePathToRCSCommands = "";
    }
    Properties pp = new Properties();
    OxygenUtils.extractProps(iwe.getProperties(), pp, ENV_KEY_PREFIX, true);
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

  public void delete(FSFileInfo info) throws Exception {
    super.delete(info);
    replaceInCache(info.file);
  }

  protected void doDeleteVersions(VirtualWritableFile vf, String logmsg, OxygenIntRange versions)
      throws Exception {
    // rcs -o only takes ranges, so I have to specify a range. Consequently, I have to go one by
    // one.
    File f = vf.getFile();
    int[] ranges = versions.getRanges();
    for (int i = 0; i < ranges.length; i++) {
      StringBuffer buf = new StringBuffer(16);
      buf.append("-o");
      int r0 = ranges[i];
      int r1 = ranges[++i];
      buf.append("1.").append(r0);
      if (r1 > r0) {
        buf.append(":1.").append(r1);
      }
      String rcsremovestr = buf.toString();
      // System.out.println("rcsremovestr: " + rcsremovestr);
      // String[] args = new String[]{basePathToRCSCommands + "rcs", "-q", "-u", "-sExp", "-t-none",
      // "-m" + info.description, _f.getName()};
      String[] args = new String[] {basePathToRCSCommands + "rcs", "-q", rcsremovestr, f.getName()};
      FSUtils.CmdResultInfo p4res =
          FSUtils.cmdexec(null, args, envp, f.getParentFile(), null, true, false);
    }
  }

  public BufferedInputStream getFileContentsAsStream(VirtualFile _f, int rev) throws Exception {
    File file = ((VirtualWritableFile) _f).getFile();
    String revstr = (rev < getInitialVersion()) ? "-r" : "-r1." + rev;
    String[] args = {basePathToRCSCommands + "co", "-kb", "-p", revstr, file.getName()};
    Process p = FSUtils.cmdexecAndReturnProcess(null, args, envp, file.getParentFile());
    return new BufferedInputStream((new ProcessHandler(p)).manageInputStream(0, null));

    // BufferedInputStream bis = new
    // BufferedInputStream(OxygenUtils.getDisconnectedInputStream(p.getInputStream()));
    // hdlr.getRunThread().join();
    // p.waitFor();
  }

  public boolean isRepositoryInfoAvailable(VirtualFile _f) throws Exception {
    if (_f != null) {
      VirtualFile _f2 = _f.getParent().getChild(METADATA_DIRECTORY);
      if (_f2 != null && _f2.exists()) {
        VirtualFile _f3 = _f2.getChild(_f.getName());
        VirtualFile _f4 = _f2.getChild(_f.getName() + ",v");
        return ((_f3 != null && _f3.exists()) || (_f4 != null && _f4.exists()));
      }
    }
    return false;
  }

  public String[] lookupNames(final VirtualFile f, int maxdepth, final boolean deleted)
      throws Exception {
    VirtualFileFilter vff =
        new VirtualFileFilter() {
          public boolean accept(VirtualFile vf) {
            try {
              String vfname = vf.getName();
              boolean isInRepo =
                  (vf.getParent().getName().equals(METADATA_DIRECTORY)
                      && !vf.getParent().getParent().getName().equals(METADATA_DIRECTORY));
              boolean corrFileExists = false;
              if (isInRepo) {
                // if not deleted, remove those without corresponding files on disk (or where latest
                // version is not empty)
                if (vfname.endsWith(",v")) {
                  vfname = vfname.substring(0, vfname.length() - 2);
                }
                corrFileExists = vf.getParent().getParent().getChild(vfname).exists();
              }
              return (deleted ? (isInRepo && !corrFileExists) : (isInRepo && corrFileExists));
            } catch (Exception exc) {
              return false;
            }
          }
        };
    return doLookupNames(vff, rcsFilePathPattern, f, maxdepth);
  }

  protected void doAddOrEdit(VirtualWritableFile _vf, Object newContent) throws Exception {
    File file = _vf.getFile();
    String[] args = null;
    FSUtils.CmdResultInfo p4res = null;

    ((VirtualWritableFile) _vf.getParent()).mkdirs();

    if (!isRepositoryInfoAvailable(_vf)) {
      File file2 = new File(file.getParentFile(), METADATA_DIRECTORY);
      file2.mkdirs();
      args = new String[] {basePathToRCSCommands + "rcs", "-i", "-kb", "-t-none", _vf.getName()};
      p4res = FSUtils.cmdexec(null, args, envp, file.getParentFile(), null, true, false);
    } else {
      args = new String[] {basePathToRCSCommands + "co", "-kb", "-l", _vf.getName()};
      p4res = FSUtils.cmdexec(null, args, envp, file.getParentFile(), null, true, false);
    }

    FSUtils.preSaveOverwrite(_vf, newContent, getEncoding(), false);
  }

  protected void doSubmit(FSFileInfo info) throws Exception {
    VirtualWritableFile _f = (VirtualWritableFile) info.file;

    // System.out.println(System.getenv("TZ"));

    String[] args =
        new String[] {
          basePathToRCSCommands + "ci",
          "-q",
          "-u",
          "-sExp",
          "-t-none",
          "-m" + info.description,
          _f.getName()
        };
    // String[] args = {basePathToRCSCommands + "ci", "-q", "-l", "-sExp", "-t-none", "-mhmm",
    // _f.getName()};
    FSUtils.CmdResultInfo p4res =
        FSUtils.cmdexec(null, args, envp, _f.getFile().getParentFile(), null, true, false);

    replaceInCache(_f);
  }

  // get the output of rlog, parse it, and return the info required
  protected FSFileInfo doGetEntryInfo(VirtualFile _f, int version) throws Exception {
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

  protected List doGetEntryLog(VirtualFile _f) throws Exception {
    Map map = preloadFsInfo(_f, IMPOSSIBLE_POSITIVE_VERSION);
    FSFileInfo[] versions = (FSFileInfo[]) map.values().toArray(new FSFileInfo[0]);
    Arrays.sort(versions);
    return Arrays.asList(versions);
  }

  /*
   * version is one of IMPOSSIBLE_POSITIVE_VERSION (meaning get all versions),
   *                   x < getInitialVersion() (meaning get latest version)
   *                   x >= getInitialVersion() (meaning get this specific version
   */
  private Map preloadFsInfo(VirtualFile _f, int version) throws Exception {
    Map map = new HashMap();
    String fpath = _f.getPath().replace('\\', '/');
    int maxHeadRev = getInitialVersion() - 1;
    File file = ((VirtualWritableFile) _f).getFile();
    String[] args = null;
    if (version == IMPOSSIBLE_POSITIVE_VERSION) {
      args = new String[] {basePathToRCSCommands + "rlog", "-zLT", file.getName()};
    } else if (version < getInitialVersion()) {
      args = new String[] {basePathToRCSCommands + "rlog", "-zLT", "-r", file.getName()};
    } else {
      args =
          new String[] {basePathToRCSCommands + "rlog", "-zLT", "-r1." + version, file.getName()};
    }
    File wdir = file.getParentFile();

    Process p = FSUtils.cmdexecAndReturnProcess(null, args, envp, wdir);
    ProcessHandler ph = new ProcessHandler(p);
    StringWriter err = new StringWriter();
    BufferedReader br = new BufferedReader(new InputStreamReader(ph.manageInputStream(0, err)));

    // FSUtils.CmdResultInfo p4res = FSUtils.cmdexec(null, args, envp, file.getParentFile(), null,
    // true, false);
    // BufferedReader br = new BufferedReader(new StringReader(p4res.outstr));
    try {
      List list = null;
      while ((list = readSomeInfo(wdir, br)) != null) {
        for (Iterator itr = list.iterator(); itr.hasNext(); ) {
          FSFileInfo info = (FSFileInfo) itr.next();
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
      cachemgr().put(CACHE_GROUP_FNAMEVERINFO, getCacheKey(_f), info);
      map.put(new Integer(version), info);
    }
    return map;
  }

  private List readSomeInfo(File dir, BufferedReader br) throws Exception {
    List list = new ArrayList();
    String s = br.readLine();
    if (s == null) {
      return null;
    }

    File f = null;
    while (!(s = br.readLine()).startsWith("-----------------------")) {
      if (f == null) {
        Matcher m = null;
        if ((m = adminRCSFileLinePattern.matcher(s)).matches()
            || (m = adminWorkingFileLinePattern.matcher(s)).matches()) {
          f = new File(dir, m.group(1));
        }
      }
    }

    readAllVersions:
    while ((s = br.readLine()) != null) {
      FSFileInfo info = new FSFileInfo();
      info.file = new VirtualPlainFile(f);

      int idx = s.indexOf(".");
      int idx2 = s.indexOf("\t", idx);
      if (idx2 == -1) {
        s = s.substring(idx + 1).trim();
      } else {
        s = s.substring(idx + 1, idx2).trim();
      }
      info.rev = Integer.parseInt(s);

      s = br.readLine();
      s = s.substring(s.indexOf(":") + 1, s.indexOf(";")).trim() + "00";
      info.date = datefmt.parse(s);

      StringBuffer logmsgbuf = new StringBuffer();
      while ((s = br.readLine().trim()) != null) {
        if (s.startsWith("-----------------------")) {
          info.description = logmsgbuf.toString();
          list.add(info);
          break;
        } else if (s.startsWith("=======================")) {
          info.description = logmsgbuf.toString();
          list.add(info);
          break readAllVersions;
        } else {
          if (s.startsWith(WikiConstants.ATTRIBUTE_SIZE)) {
            info.size = Long.parseLong(s.substring(s.indexOf("=") + 1, s.length()));
          }
          logmsgbuf.append(s).append(FSUtils.linesep);
        }
      }
    }
    // System.out.println("readSomeInfo: list: " + list);
    return list;
  }

  private void preloadCache() throws Exception {
    // if basefile is null OR does not exist OR is empty, do nothing
    if (basefile == null || !basefile.exists() || basefile.list(null, 1).length == 0) {
      return;
    }
    long lcurr = System.currentTimeMillis();
    OxygenUtils.info("Preloading RCS cache for initial and head versions: " + basefile.getPath());

    FileFilter ff =
        new FileFilter() {
          public boolean accept(File f) {
            return (f.isDirectory() && f.getName().equals("RCS"));
          }
        };
    File file = ((VirtualWritableFile) basefile).getFile();
    List list = new ArrayList();
    OxygenUtils.listFiles(file, Integer.MAX_VALUE, list, ff);
    for (Iterator itr = list.iterator(); itr.hasNext(); ) {
      preloadCache((File) itr.next());
    }
    OxygenUtils.info(
        "Done Preloading RCS cache in: " + (System.currentTimeMillis() - lcurr) + " ms");
  }

  private void preloadCache(File rcsdir) throws Exception {
    File wdir = rcsdir.getParentFile();
    String[] fnames = rcsdir.list();
    List largs = new ArrayList();
    largs.add(basePathToRCSCommands + "rlog");
    largs.add("-zLT");
    largs.add("-r1.1,");
    largs.addAll(Arrays.asList(fnames));
    String[] args = (String[]) largs.toArray(new String[0]);

    Process p = FSUtils.cmdexecAndReturnProcess(null, args, envp, wdir);
    ProcessHandler ph = new ProcessHandler(p);
    StringWriter err = new StringWriter();
    BufferedReader br = new BufferedReader(new InputStreamReader(ph.manageInputStream(0, err)));

    try {
      List list = null;
      while ((list = readSomeInfo(wdir, br)) != null) {
        FSFileInfo[] infos = (FSFileInfo[]) list.toArray(new FSFileInfo[0]);
        for (int i = 0; i < infos.length; i++) {
          if (infos[i].rev == getInitialVersion()) {
            cachemgr().put(CACHE_GROUP_FNAMEVERINFO_VER1, getCacheKey(infos[i].file), infos[i]);
          } else {
            cachemgr().put(CACHE_GROUP_FNAMEVERINFO, getCacheKey(infos[i].file), infos[i]);
          }
        }
        if (infos.length == 1) {
          cachemgr().put(CACHE_GROUP_FNAMEVERINFO, getCacheKey(infos[0].file), infos[0]);
        }
      }
      ph.waitTillDone().check(0, err.toString());
    } finally {
      CloseUtils.close(br);
    }
  }

  private void replaceInCache(VirtualFile _f) throws Exception {
    String fname = getCacheKey(_f);
    cachemgr().remove(CACHE_GROUP_FNAMEVERINFO, fname);
    doGetEntryInfo(_f, IMPOSSIBLE_NEGATIVE_VERSION);
  }

  private String getCacheKey(VirtualFile _f) throws Exception {
    String fname = _f.getPath().replace('\\', '/');
    return fname;
  }
}
