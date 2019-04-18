/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions.filesystem;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.ugorji.oxygen.io.VirtualFile;
import net.ugorji.oxygen.io.VirtualPlainFile;
import net.ugorji.oxygen.io.VirtualWritableFile;
import net.ugorji.oxygen.io.VirtualZipFile;
import net.ugorji.oxygen.util.*;
import net.ugorji.oxygen.web.OxygenWebException;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiException;
import net.ugorji.oxygen.wiki.WikiProvidedObject;

public class FSUtils {
  // public static final int VERSION_MIN_VALID_NUMBER = 1;
  public static final String GZIP_SUFFIX = ".gz";
  public static final String linesep = StringUtils.LINE_SEP;
  private static final Pattern slashNPattern = Pattern.compile("\\\\n");

  public static FSHelper retrieveFSHelper(OxygenEngine iwe) throws Exception {
    FSHelper helper = (FSHelper) iwe.getAttribute(FSHelper.class.getName());
    if (helper == null) {
      String s = iwe.getProperty(WikiConstants.PROVIDER_FILESYSTEM_HELPER_CLASS_KEY, null);
      helper = (FSHelper) Class.forName(s).newInstance();
      VirtualFile basefile = null;
      if (iwe instanceof WikiCategoryEngine) {
        basefile = getBaseFile((WikiCategoryEngine) iwe);
      }
      helper.init(iwe, basefile);
      iwe.setAttribute(FSHelper.class.getName(), helper);
    }
    return helper;
  }

  public static VirtualFile getBaseFile(WikiCategoryEngine wce) throws Exception {
    String base = wce.getProperty(WikiConstants.PROVIDER_FILESYSTEM_LOCATION_BASE_KEY, null);
    if (base == null) {
      // basedir = WikiConstants.PROVIDER_FILESYSTEM_DEFAULT_BASEDIR_PREFIX + "/" + wce.getName();
      throw new WikiException(WebLocal.getI18n().str("providers.file.base_location_not_defined"));
    }
    base = StringUtils.replacePropertyReferencesInString(base, wce.getProperties());

    VirtualFile vf = null;

    File f = new File(base);
    File fjar = new File(f.getParentFile(), f.getName() + ".jar");

    if (fjar.getName().equals("help.jar") && fjar.exists() && fjar.isFile()) {
      // System.out.println("fjar: " + fjar);
      vf = new VirtualZipFile(fjar);
    } else if (f.exists()) {
      if (f.isDirectory()) {
        vf = new VirtualPlainFile(f);
      } else if (base.endsWith(".jar") || base.endsWith(".zip")) {
        vf = new VirtualZipFile(f);
        OxygenUtils.debug("In DefFSPageProv: Using VirtualZipFile");
      } else {
        throw new WikiException(
            WebLocal.getI18n().str("providers.file.base_location_invalid", f.toString()));
      }
    } else {
      if (f.mkdirs()) {
        vf = new VirtualPlainFile(f);
      } else {
        throw new WikiException(
            WebLocal.getI18n().str("providers.file.base_directory_not_created", f.toString()));
      }
    }
    return vf;
  }

  public static void changeToProperties(String log, Properties atts) {
    if (atts == null || log == null) {
      return;
    }
    // System.out.println("changeToProperties: log: " + log);
    log = slashNPattern.matcher(log).replaceAll("\n");
    // log = log.replace(
    try {
      atts.load(new ByteArrayInputStream(log.getBytes()));
      atts.remove("Change");
    } catch (Exception exc) {
      if (atts.getProperty(WikiConstants.ATTRIBUTE_COMMENTS) == null) {
        atts.setProperty(WikiConstants.ATTRIBUTE_COMMENTS, log);
      }
    }
    // System.out.println("changeToProperties: atts: " + atts);
  }

  public static String propertiesToChange(Properties atts, String pagerep, String attachment)
      throws Exception {
    if (atts == null) {
      return null;
    }
    StringBuffer buf = new StringBuffer().append("#");
    Object author = atts.get("author");
    if (author != null) {
      buf.append(String.valueOf(author)).append("|");
    }
    if (pagerep != null) {
      buf.append(pagerep).append("|");
    }
    if (attachment != null) {
      buf.append(attachment).append("|");
    }
    buf.append(new Date());
    return StringUtils.propsToString(atts, buf.toString());
  }

  public static void preSaveOverwrite(
      VirtualWritableFile _f, Object newContent, String encoding, boolean supportGzip)
      throws Exception {
    if (newContent == null) {
      return;
    }
    if (newContent instanceof File) {
      OutputStream os = (supportGzip ? gzipOutputStream(_f) : _f.getOutputStream());
      FileInputStream fis = new FileInputStream((File) newContent);
      OxygenUtils.copyStreams(fis, os, true);
    } else if (newContent instanceof String) {
      OutputStream os = (supportGzip ? gzipOutputStream(_f) : _f.getOutputStream());
      OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
      OxygenUtils.writeTextContents(osw, (String) newContent, true);
    } else {
      throw new Exception("newContent must be a File or String");
    }
  }

  public static void setPageAttributes(WikiProvidedObject wp, String log) {
    Properties atts = new Properties();
    FSUtils.changeToProperties(log, atts);
    wp.setAttributes(atts);
  }

  public static GZIPInputStream gzipInputStream(VirtualFile vf) throws Exception {
    return new GZIPInputStream(vf.getInputStream());
  }

  public static GZIPOutputStream gzipOutputStream(VirtualWritableFile vf) throws Exception {
    return new GZIPOutputStream(vf.getOutputStream());
  }

  public static Process cmdexecAndReturnProcess(
      List initCmdLineArgs, String[] args1, String[] envp, File directory) throws Exception {
    OxygenUtils.debug(
        "FSUtils cmd args: "
            + Arrays.asList(args1)
            + " DIR: "
            + (directory == null ? "" : directory.getPath()));
    // System.out.println("cmd args: " + Arrays.asList(args1) + " DIR: " + (directory == null ? "" :
    // directory.getPath()));
    // Thread.dumpStack();
    List args = new ArrayList();
    if (initCmdLineArgs != null) {
      args.addAll(initCmdLineArgs);
    }
    if (args1 != null) {
      args.addAll(Arrays.asList(args1));
    }
    // System.out.println("dir: |" + directory + "| cmd args: " + args);
    String[] cmdargs = (String[]) args.toArray(new String[0]);
    Process p = Runtime.getRuntime().exec(cmdargs, envp, directory);
    return p;
  }

  public static CmdResultInfo cmdexec(
      List initCmdLineArgs,
      String[] args1,
      String[] envp,
      File directory,
      String input,
      boolean failOnExitCodeNonZero,
      boolean failOnOutputToStdErr)
      throws Exception {
    Process p = cmdexecAndReturnProcess(initCmdLineArgs, args1, envp, directory);
    return cmdexecHandleProcess(p, input, failOnExitCodeNonZero, failOnOutputToStdErr);
  }

  public static CmdResultInfo cmdexecHandleProcess(
      Process p, String input, boolean failOnExitCodeNonZero, boolean failOnOutputToStdErr)
      throws Exception {
    return cmdexecHandleProcess(p, input, false, failOnExitCodeNonZero, failOnOutputToStdErr);
  }

  public static CmdResultInfo cmdexecHandleProcess(
      Process p,
      String input,
      boolean sendOutputToFile,
      boolean failOnExitCodeNonZero,
      boolean failOnOutputToStdErr)
      throws Exception {
    File tmpFile = null;
    Writer outstw, outerrstw = null;
    if (sendOutputToFile) {
      outstw = new FileWriter(tmpFile = File.createTempFile("fsutils.", ".tmp"));
      tmpFile.deleteOnExit();
    } else {
      outstw = new StringWriter();
    }
    StringWriter errstw = new StringWriter();

    Reader streader = (input == null ? NullReader.SINGLETON : ((Reader) new StringReader(input)));
    ProcessHandler.handle(p, outstw, errstw, streader, true);
    CloseUtils.close(outstw);

    CmdResultInfo p4res = new CmdResultInfo();
    p4res.outfile = tmpFile;
    p4res.exitcode = p.exitValue();
    p4res.outstr = (outstw instanceof StringWriter ? ((StringWriter) outstw).toString() : "");
    p4res.errstr = errstw.toString();
    // System.out.println("p4res: " + p4res);
    String errmsg = ((p4res.errstr == null) ? "" : p4res.errstr);
    if (failOnExitCodeNonZero && p4res.exitcode != 0) {
      throw new Exception("[Exit Code: " + p4res.exitcode + "] Error Msg: " + errmsg);
    }
    if (failOnOutputToStdErr && p4res.errstr != null && p4res.errstr.trim().length() > 0) {
      throw new Exception(errmsg);
    }
    return p4res;
  }

  public static void makeFileWritable(File f, String[] envp, File directory) {
    try {
      if (f != null && f.exists()) {
        String[] args = {"chmod", "u+w", f.getAbsolutePath()};
        Process p = Runtime.getRuntime().exec(args, envp, directory);
        FSUtils.CmdResultInfo p4res = FSUtils.cmdexecHandleProcess(p, null, true, true);
      }
    } catch (Exception exc) {
      OxygenUtils.error(exc);
    }
  }

  public static Matcher matchPattern(Pattern p, String s) throws Exception {
    Matcher m = p.matcher(s);
    if (!m.matches()) {
      throw new OxygenWebException("Unable to match Pattern: " + p + " for String: " + s);
    }
    return m;
  }

  static String extractPagename(String fPath, VirtualFile basefile) {
    String basefilepath = basefile.getPath().replace('\\', '/');
    return extractPagename(fPath, basefilepath);
  }

  static String extractPagename(String fPath, String basefilepath) {
    return extractPagename(fPath, basefilepath, basefilepath.length());
  }

  static String extractPagename(String fPath, String basefilepath, int basefilepathlen) {
    String wikipagename = fPath.replace('\\', '/');
    if (basefilepathlen > 0 && wikipagename.startsWith(basefilepath)) {
      wikipagename = wikipagename.substring(basefilepathlen);
    }
    return StringUtils.trim(wikipagename, '/');
  }

  protected static Pattern getNotProvidedObjectPattern(String regex0, String configuredRegex) {
    String regex = null;
    if (regex0 != null && regex0.trim().length() > 0) {
      regex = regex0;
    }
    if (configuredRegex != null && configuredRegex.trim().length() > 0) {
      if (regex == null) {
        regex = "";
      }
      regex = regex + "|" + configuredRegex.trim();
    }
    if (regex != null) {
      return Pattern.compile(regex);
    }
    return null;
  }

  public static class CmdResultInfo {
    public File outfile;
    public String outstr;
    public String errstr;
    public int exitcode;

    public String toString() {
      return " ---- exit code: ---- \n"
          + exitcode
          + "\n"
          + " ---- outstr: ---- \n"
          + outstr
          + "\n"
          + " ---- errstr: ---- \n"
          + errstr
          + "\n";
    }
  }

  public static void main(String[] args) throws Exception {
    System.out.println(new Date());
    String s = null;
    Properties pp = new Properties();
    String[] sa =
        new String[] {
          "A man\\nIs Here\\nTo come", "A man\nIs Here\nTo come", "A man\r\nIs Here\r\nTo come"
        };
    for (int i = 0; i < sa.length; i++) {
      s = sa[i];
      System.out.println(s);
      System.out.println("-----------------");
      s = slashNPattern.matcher(s).replaceAll("\n");
      System.out.println(s);
      System.out.println("=================");
    }
    s = "a=1\nb=2\nc=3";
    pp.clear();
    changeToProperties(s, pp);
    System.out.println("pp: " + pp);
    System.out.println("=================");
    s = "a=1\\nb=2\\nc=3";
    pp.clear();
    changeToProperties(s, pp);
    System.out.println("pp: " + pp);
    System.out.println("=================");
  }

  /*
  private static class EngineFileCombo {
    private OxygenEngine engine;
    private VirtualFile basefile;

    public boolean equals(Object o) {
      EngineFileCombo efc = (EngineFileCombo)o;
      return (engine == efc.engine && basefile == efc.basefile);
    }

    public int hashCode() {
      int i = engine.hashCode();
      if(basefile != null) {
        i = basefile * (31^basefile.hashCode());
      }
      return i;
    }
  }
  */
}
