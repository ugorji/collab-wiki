/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// standalone class, which only depends on the JDK
// It migrates the latest version of file, and latest versions of attachments
// to net.ugorji.oxygen wiki
// works if using the BasicAttachmentProvider and FileSystemProvider (of jspwiki)
// ... .. . .. ...
// for now, always hide macros
public class JspwikiMigrate {
  private static Pattern boldSrcPattern;
  private static Pattern listSrcPattern;
  private static Pattern tableSrcPattern;
  private static Pattern badLinkSrcPattern;
  // private static Pattern linkSrcPattern;
  private static Pattern headerSrcPattern;
  private static Pattern macroSrcPattern;
  private static Pattern wordSrcPattern;
  private static Pattern blogentryOrCommentFileNamePattern;
  private static Pattern linkPotentialAttachmentPattern;

  private static Pattern dollarPattern;

  private static Pattern subscriptPattern;
  private static Pattern superscriptPattern;
  private static Pattern parameterPattern;
  private static Pattern asisOpenPattern;
  private static Pattern asisClosePattern;
  private static Pattern aposXStylePattern;
  private static Pattern strikePattern;

  private static Random rand;
  private static List initialListOfJspwikiPages = new ArrayList();

  static {
    staticInit();
  }

  private String[] matches = new String[0];
  private String[] notmatches = new String[0];
  private LinkedHashMap transforms = new LinkedHashMap();
  private File srcdir;
  private File destdir;
  private File attachdir;
  private boolean defaultExcludes = false;
  private boolean debug = false;

  public JspwikiMigrate() {}

  public void setDefaultExcludes(boolean b) {
    defaultExcludes = b;
  }

  public void setTransforms(LinkedHashMap p) {
    transforms = p;
  }

  public File getDestdir() {
    return destdir;
  }

  public void setDestdir(File destdir) {
    this.destdir = destdir;
  }

  public String[] getMatches() {
    return matches;
  }

  public void setMatches(String[] matches) {
    this.matches = matches;
  }

  public String[] getNotmatches() {
    return notmatches;
  }

  public void setNotmatches(String[] notmatches) {
    this.notmatches = notmatches;
  }

  public File getSrcdir() {
    return srcdir;
  }

  public void setSrcdir(File srcdir) {
    this.srcdir = srcdir;
  }

  public void setAttachdir(File attachdir) {
    this.attachdir = attachdir;
  }

  public void migrate() throws Exception {
    File[] files = getMatchedSrcFiles();
    Map mappings = getMappings(files);
    // log(mappings);
    for (int i = 0; i < files.length; i++) {
      File fsrc = files[i];
      log("... migrating file: " + fsrc);
      String nonMappedDestPagename = getNonMappedDestPagename(fsrc);
      String destpagename = getMapping(mappings, nonMappedDestPagename);
      File fdest = getDestFile(destpagename);

      // copy the attachments
      File srcAttachDir = new File(attachdir, nonMappedDestPagename + "-att");
      copyAttachments(srcAttachDir, fdest.getParentFile());

      // translate file
      String s = getFileContents(fsrc);
      s = getOxyMarkupGivenJspwikiMarkup(s, mappings, fdest.getParentFile());
      writeFile(fdest, s);
    }

    // translate blogs and comments
    translateBlogsAndComments(mappings);
  }

  // translate blogs and comments
  // find all blogs and comments, take first part, find all with it, find latest,
  // take its source, convert it, write it as an attachment (with the timestamp on it)
  private void translateBlogsAndComments(Map mappings) throws Exception {
    Map mmap = new HashMap();
    FileFilter ff =
        new FileFilter() {
          public boolean accept(File f) {
            String fName = f.getName();
            return f.isFile() && blogentryOrCommentFileNamePattern.matcher(fName).matches();
          }
        };
    File[] files = srcdir.listFiles(ff);
    for (int i = 0; i < files.length; i++) {
      Matcher mm = blogentryOrCommentFileNamePattern.matcher(files[i].getName());
      if (mm.matches()) {
        mmap.put(mm.group(1), mm.group(2));
      }
    }
    for (Iterator itr = mmap.keySet().iterator(); itr.hasNext(); ) {
      String s = (String) itr.next();
      // log(" XXXXXX SSSSSS XXXXXX " + s);
      File fsrc0 = null;
      for (int i = 1; i < Integer.MAX_VALUE; i++) {
        File f = new File(srcdir, s + "_" + i + ".txt");
        if (f.exists()) {
          fsrc0 = f;
        } else {
          break;
        }
      }
      if (fsrc0 == null) {
        continue;
      }
      String s2 = getFileContents(fsrc0);
      s2 = getOxyMarkupGivenJspwikiMarkup(s2, mappings, null);
      File fdest = getDestFile((String) mmap.get(s));
      fdest = new File(fdest.getParentFile(), "REVIEW." + fsrc0.lastModified() + ".TXT");
      writeFile(fdest, s2);
    }
  }

  private void writeFile(File fdest, String s) throws Exception {
    Writer fw = new OutputStreamWriter(new FileOutputStream(fdest, false), "UTF-8");
    fw.write(s);
    fw.flush();
    fw.close();
  }

  private String getOxyMarkupGivenJspwikiMarkup(String s, Map mappings, File destDirForPage)
      throws Exception {
    StringBuffer buf = new StringBuffer();
    String s2 = null;
    String s3 = null;
    String s4 = null;
    String srand = null;
    Matcher m = null;
    File f = null;

    // switch headers
    buf = new StringBuffer();
    m = headerSrcPattern.matcher(s);
    while (m.find()) {
      s2 = m.group(2);
      s2 = getReplacementHeaderSubset(s2);
      m.appendReplacement(buf, "$1" + dollarReplace(s2));
    }
    m.appendTail(buf);
    s = buf.toString();

    // switch lists
    buf = new StringBuffer();
    m = listSrcPattern.matcher(s);
    while (m.find()) {
      s2 = m.group(2);
      char[] chars = new char[(s2.length() * 2) - 1];
      Arrays.fill(chars, ' ');
      s2 = new String(chars) + s2.charAt(0) + " ";
      m.appendReplacement(buf, dollarReplace(s2));
    }
    m.appendTail(buf);
    s = buf.toString();

    // switch bold
    buf = new StringBuffer();
    m = boldSrcPattern.matcher(s);
    while (m.find()) {
      m.appendReplacement(buf, "** $1 **");
    }
    m.appendTail(buf);
    s = buf.toString();

    // switch macroSrcPattern, and put in hide blocks
    buf = new StringBuffer();
    m = macroSrcPattern.matcher(s);
    while (m.find()) {
      m.appendReplacement(buf, "{hide} {$1| /} {/hide}");
    }
    m.appendTail(buf);
    s = buf.toString();

    // switch tableSrcPattern
    buf = new StringBuffer();
    m = tableSrcPattern.matcher(s);
    while (m.find()) {
      s2 = m.group(0);
      srand = String.valueOf(Math.abs(rand.nextLong()));
      s2 = s2.replaceAll("\\|\\|", srand);
      s2 = s2.replaceAll("\\|", " || ");
      s2 = s2.replaceAll(srand, " -|| ");
      // log("- s2 -: " + s2);
      try {
        m.appendReplacement(buf, dollarReplace(s2));
      } catch (Exception exc) {
        log("XYZ: " + s2);
        // log("XYZ: " + dollarReplace(s2));
        throw exc;
      }
    }
    m.appendTail(buf);
    s = buf.toString();

    // switch badLinkSrcPattern
    buf = new StringBuffer();
    m = badLinkSrcPattern.matcher(s);
    while (m.find()) {
      m.appendReplacement(buf, "[$1|$2]");
    }
    m.appendTail(buf);
    s = buf.toString();

    // switch linkPotentialAttachmentPattern
    if (destDirForPage != null) {
      buf = new StringBuffer();
      m = linkPotentialAttachmentPattern.matcher(s);
      while (m.find()) {
        f = null;
        s4 = m.group(2).trim();
        // if directory exists, switch s4, and append it
        if (!s4.startsWith("^")) {
          f = new File(destDirForPage, s4);
        }
        if (f != null && f.exists() && f.isFile()) {
          m.appendReplacement(buf, "[$1|^$2]");
        } else {
          m.appendReplacement(buf, "[$1|$2]");
        }
      }
      m.appendTail(buf);
      s = buf.toString();
    }

    // switch wordSrcPattern
    buf = new StringBuffer();
    m = wordSrcPattern.matcher(s);
    while (m.find()) {
      s2 = m.group(0);
      s2 = getMapping(mappings, s2);
      m.appendReplacement(buf, dollarReplace(s2));
    }
    m.appendTail(buf);
    s = buf.toString();

    // switch ,, ^^ ${  (( )) `` (what to do about --- ???)
    s = subscriptPattern.matcher(s).replaceAll(", ,");
    s = superscriptPattern.matcher(s).replaceAll("^ ^");
    // s = parameterPattern.matcher(s).replaceAll("\\$ {");
    s = asisOpenPattern.matcher(s).replaceAll("( (");
    s = asisClosePattern.matcher(s).replaceAll(") )");
    s = aposXStylePattern.matcher(s).replaceAll("` `");
    // s = strikePattern.matcher(s).replaceAll("$1--$2");
    // s = strikePattern.matcher(s).replaceAll("$1--");

    return s;
  }

  private String getFileContents(File fsrc) throws Exception {
    StringWriter bos = new StringWriter();
    Reader fr = getReaderForFile(fsrc);
    char[] buffer = new char[1024];
    int readCount = 0;
    while ((readCount = fr.read(buffer)) > 0) {
      bos.write(buffer, 0, readCount);
    }
    fr.close();
    return bos.toString();
  }

  private String getReplacementHeaderSubset(String s2) {
    s2 = s2.trim();
    String s3 = null;
    int s2len = s2.length();
    if (s2len == 1) {
      s3 = ("==== ");
    } else if (s2len == 2) {
      s3 = ("=== ");
    } else if (s2len == 3) {
      s3 = ("== ");
    } else if (s2len == 4) {
      s3 = ("= ");
    } else {
      s3 = "=";
    }
    return s3;
  }

  private File getDestFile(String destpagename) {
    File fdest = new File(destdir, destpagename);
    fdest.mkdirs();
    fdest = new File(fdest, "PAGE.TXT");
    return fdest;
  }

  private String getNonMappedDestPagename(File fsrc) {
    String fName = fsrc.getName();
    int idx = fName.lastIndexOf(".txt");
    String s = fName.substring(0, idx);
    return s;
  }

  private File[] getMatchedSrcFiles() {
    final Pattern[] matchespatterns = new Pattern[matches.length];
    final Pattern[] notmatchespatterns = new Pattern[notmatches.length];
    for (int i = 0; i < matchespatterns.length; i++) {
      matchespatterns[i] = Pattern.compile(matches[i]);
    }
    for (int i = 0; i < notmatchespatterns.length; i++) {
      notmatchespatterns[i] = Pattern.compile(notmatches[i]);
    }
    FileFilter ff =
        new FileFilter() {
          public boolean accept(File f) {
            String fName = f.getName();
            boolean b = true;
            label_1:
            do {
              if (defaultExcludes && initialListOfJspwikiPages.contains(fName)) {
                b = false;
                break label_1;
              }
              for (int i = 0; i < matchespatterns.length; i++) {
                if (!(matchespatterns[i].matcher(fName).matches())) {
                  b = false;
                  break label_1;
                }
              }
              for (int i = 0; i < notmatchespatterns.length; i++) {
                if (notmatchespatterns[i].matcher(fName).matches()) {
                  b = false;
                  break label_1;
                }
              }
            } while (false);
            return b;
          }
        };
    File[] files = srcdir.listFiles(ff);
    return files;
  }

  private Map getMappings(File[] files) {
    Map mappings = new HashMap();
    if (transforms.size() == 0) {
      return mappings;
    }

    for (int i = 0; i < files.length; i++) {
      String s = getNonMappedDestPagename(files[i]);
      String s2 = s;
      for (Iterator itr = transforms.keySet().iterator(); itr.hasNext(); ) {
        String s0 = (String) itr.next();
        Pattern p0 = Pattern.compile(s0);
        Matcher m = p0.matcher(s2);
        s2 = m.replaceAll((String) transforms.get(s0));
      }
      // log("Checking mapping: srcmapping: " + srcmapping + " to match: " + s);
      mappings.put(s, s2);
    }
    return mappings;
  }

  private static void copyFile(File from, File to) throws Exception {
    FileInputStream fis = new FileInputStream(from);
    FileOutputStream fos = new FileOutputStream(to);
    copyStreams(fis, fos, true);
  }

  private static void copyStreams(InputStream fis, OutputStream fos, boolean closeWhenDone)
      throws Exception {
    byte[] b = new byte[1024];
    int num = 0;
    while ((num = fis.read(b)) != -1) {
      fos.write(b, 0, num);
    }
    fos.flush();
    if (closeWhenDone) {
      fis.close();
      fos.close();
    }
  }

  private String getMapping(Map mappings, String nonMappedDestPagename) {
    String destpagename = (String) mappings.get(nonMappedDestPagename);
    if (destpagename == null) {
      destpagename = nonMappedDestPagename;
    }
    return destpagename;
  }

  private void copyAttachments(File srcAttachDir, File destAttachDir) throws Exception {
    if (srcAttachDir.exists()) {
      // copy the files from one place to another
      File[] srcAttachDirs = srcAttachDir.listFiles();
      for (int j = 0; j < srcAttachDirs.length; j++) {
        if (!srcAttachDirs[j].isFile() && srcAttachDirs[j].getName().endsWith("-dir")) {
          int idx = srcAttachDirs[j].getName().lastIndexOf("-dir");
          String attfilename0 = srcAttachDirs[j].getName().substring(0, idx);
          // seems jspwiki puts the urlencoded name in the directory. So fix this.
          attfilename0 = URLDecoder.decode(attfilename0, "UTF-8");
          File latestversionfile = getLatestAttachVersionFile(srcAttachDirs[j]);
          copyFile(latestversionfile, new File(destAttachDir, attfilename0));
        } else {
          System.err.println(
              "Attachment directory is invalid: " + srcAttachDirs[j].getAbsolutePath());
        }
      }
    }
  }

  private File getLatestAttachVersionFile(File dir) throws Exception {
    File[] files = dir.listFiles();
    int latestversion = 0;
    File latestversionfile = null;
    for (int i = 0; i < files.length; i++) {
      int indexOfDot = files[i].getName().indexOf(".");
      try {
        int ver = Integer.parseInt(files[i].getName().substring(0, indexOfDot));
        if (ver > latestversion) {
          latestversion = ver;
          latestversionfile = files[i];
        }
      } catch (NumberFormatException nfe) {
      }
    }
    return latestversionfile;
  }

  private void log(Object o) {
    if (debug) {
      System.out.println(o);
    }
  }

  private static void staticInit() {
    try {
      rand = new Random();
      int flags = Pattern.MULTILINE | Pattern.DOTALL;
      blogentryOrCommentFileNamePattern =
          Pattern.compile("((.*?)_(blogentry|comments)_[0-9]+)_[0-9].txt");
      boldSrcPattern = Pattern.compile("__(.*?)__", flags);
      listSrcPattern = Pattern.compile("^([\\p{Space}]*)([#*]+)", flags);
      tableSrcPattern = Pattern.compile("^([\\p{Space}]*)(\\|+).*?$", flags);
      // linkSrcPattern = Pattern.compile("\\[(.*?)\\]", flags);
      macroSrcPattern = Pattern.compile("\\[\\{(.*?)\\}\\]", flags);
      headerSrcPattern = Pattern.compile("^([\\p{Space}]*)((\\!)+)", flags);
      wordSrcPattern = Pattern.compile("([a-zA-Z_0-9\\./-]+)");
      badLinkSrcPattern = Pattern.compile("\\[(.*?)\\|\\|(.*?)\\]");
      linkPotentialAttachmentPattern = Pattern.compile("\\[(.*?)\\|(.*?)\\]");
      dollarPattern = Pattern.compile("\\$");

      subscriptPattern = Pattern.compile("\\,\\,");
      superscriptPattern = Pattern.compile("\\^\\^");
      parameterPattern = Pattern.compile("\\$\\{");
      asisOpenPattern = Pattern.compile("\\(\\(");
      asisClosePattern = Pattern.compile("\\)\\)");
      aposXStylePattern = Pattern.compile("``");
      // space char, then 3 dashes, then optionally not a dash character
      strikePattern = Pattern.compile("([\\p{Space}]?)---([^-]?)");
      // strikePattern = Pattern.compile("([\\p{Space}]?)-{3}");

      BufferedReader br =
          new BufferedReader(
              new InputStreamReader(
                  Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream("net.ugorji.oxygen.wiki.tool/jspwiki_initial_set_of_pages.txt")));
      String s = null;
      while ((s = br.readLine()) != null) {
        s = s.trim();
        if (s.length() > 0 && !s.startsWith("#")) {
          initialListOfJspwikiPages.add(s);
        }
      }
      br.close();
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  private static Reader getReaderForFile(File f) throws Exception {
    return new InputStreamReader(new FileInputStream(f), "UTF-8");
  }

  private static String dollarReplace(String s2) {
    return dollarPattern.matcher(s2).replaceAll("\\\\\\$"); // \\\\$
  }

  public static void printHelp(PrintWriter pw) {
    String sep = System.getProperty("line.separator");
    String helpstr =
        "Usage: java net.ugorji.oxygen.wiki.tool.JspwikiMigrate [options]"
            + sep
            + "Options and arguments:"
            + sep
            + " -h         : Show this usage message"
            + sep
            + "              Synonym is -h OR -usage OR -help OR -?"
            + sep
            + " -df        : Specify if we should ignore all pages that come with the JSPWiki distribution (except Main and About)"
            + sep
            + "              Synonym is -defaultexcludes"
            + sep
            + " -m <regex> : Specify a regex that names of files in src directory must match"
            + sep
            + "              You specify this multiple times to add more matches"
            + sep
            + "              E.g. -m abc.*txt -m Alist.*?txt"
            + sep
            + "              Synonym is -match"
            + sep
            + " -nm <regex>: Specify a regex that names of files in src directory must *NOT* match"
            + sep
            + "              You specify this multiple times to add more"
            + sep
            + "              E.g. -nm abc.*txt -nm Alist.*?txt"
            + sep
            + "              Synonym is -notmatch"
            + sep
            + " -mf <file> : All lines in the specified file are added as regex to match"
            + sep
            + "              (like each was specified as a -m parameter) "
            + sep
            + "              You specify this multiple times to add more"
            + sep
            + "              E.g. -mf matches1.txt -mf matches2.txt"
            + sep
            + "              Synonym is -matchfile"
            + sep
            + " -nmf <file>: All lines in the specified file are added as regex to *NOT* match"
            + sep
            + "              (like each was specified as a -nm parameter) "
            + sep
            + "              You specify this multiple times to add more"
            + sep
            + "              E.g. -nmf matches1.txt -nmf matches2.txt"
            + sep
            + "              Synonym is -notmatchfile"
            + sep
            + " -s <dir>   : Specify the src directory"
            + sep
            + "              Synonym is -srcdir"
            + sep
            + " -d <dir>   : Specify the dest directory"
            + sep
            + "              Synonym is -destdir"
            + sep
            + " -a <dir>   : Specify the attachment directory"
            + sep
            + "              Synonym is -attachdir"
            + sep
            + " -t <regex> <replacement>  : "
            + sep
            + "              Specify the mapping for replacing page names"
            + sep
            + "              E.g. -t \\. /"
            + sep
            + "              Synonym is -transform"
            + sep
            + " ---Examples--- "
            + sep
            + "  -s d:/jspwikidir -d d:/oxywiki/pages/fromjspwiki -m Muscles.*?\\.txt -m AB.*?\\.txt -nm Wiki.*?\\.txt"
            + sep
            + "              lookup jspwiki pages FROM d:/jspwikidir"
            + sep
            + "              write them in collab format TO d:/oxywiki/pages/fromjspwiki"
            + sep
            + "              only convert files from d:/jspwikidir iff "
            + sep
            + "                  they match Muscles.*?.txt AND AB.*?.txt "
            + sep
            + "                  they do *NOT* match Wiki.*?.txt (all those Wiki help pages, etc) "
            + sep
            + "  -s d:/jspwikidir -d d:/oxywiki/pages/fromjspwiki -m Muscles.*?\\.txt -m AB.*?\\.txt -nm Wiki.*?\\.txt -t \\. /"
            + sep
            + "              Same as above "
            + sep
            + "                but ALSO transform . to / in the converted space"
            + sep
            + "              Many folks used . seperators in jspwiki, due to no hierachy built in, "
            + sep
            + "                but / makes us establish a hierachy and parent-child relationship"
            + sep
            + "              E.g. it converts the page Muscles.Oakland.2003.Jan to Muscles/Oakland/2003/Jan"
            + sep
            + "";

    pw.println(helpstr);
    pw.flush();
  }

  public static void printHelp(OutputStream os) {
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
    printHelp(pw);
  }

  public static void main(String[] args) throws Exception {
    doMain(args);
  }

  public static void doTest(String[] args) throws Exception {
    JspwikiMigrate tool = new JspwikiMigrate();
    String s = "--- ,, ^^ ---- ---- ---thnx `` ";
    s = tool.getOxyMarkupGivenJspwikiMarkup(s, new HashMap(), new File("."));
    System.out.println(s);
  }

  public static void doMain(String[] args) throws Exception {
    if (args.length == 0
        || args[0].equals("-usage")
        || args[0].equals("-help")
        || args[0].equals("-h")
        || args[0].equals("-?")) {
      printHelp(System.out);
      return;
    }

    List _matches = new ArrayList();
    List _notmatches = new ArrayList();
    String _srcdir = null;
    String _attachdir = null;
    String _destdir = null;
    boolean _defaultExcludes = false;
    boolean _debug = false;
    LinkedHashMap _transforms = new LinkedHashMap();

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-debug")) {
        _debug = true;
      } else if (args[i].equals("-match") || args[i].equals("-m")) {
        _matches.add(args[++i]);
      } else if (args[i].equals("-defaultexcludes") || args[i].equals("-df")) {
        _defaultExcludes = true;
        _notmatches.add(".*?_(blogentry|comments)_[0-9]+_[0-9]\\.txt");
      } else if (args[i].equals("-notmatch") || args[i].equals("-nm")) {
        _notmatches.add(args[++i]);
      } else if (args[i].equals("-matchfile") || args[i].equals("-mf")) {
        BufferedReader br = new BufferedReader(getReaderForFile(new File(args[++i])));
        String line = null;
        while ((line = br.readLine()) != null) {
          _matches.add(line);
        }
        br.close();
      } else if (args[i].equals("-notmatchfile") || args[i].equals("-nmf")) {
        BufferedReader br = new BufferedReader(getReaderForFile(new File(args[++i])));
        String line = null;
        while ((line = br.readLine()) != null) {
          _notmatches.add(line);
        }
        br.close();
      } else if (args[i].equals("-srcdir") || args[i].equals("-s")) {
        _srcdir = args[++i];
      } else if (args[i].equals("-attachdir") || args[i].equals("-a")) {
        _attachdir = args[++i];
      } else if (args[i].equals("-destdir") || args[i].equals("-d")) {
        _destdir = args[++i];
      } else if (args[i].equals("-transform") || args[i].equals("-t")) {
        _transforms.put(args[++i], args[++i]);
      }
    }

    if (_matches.isEmpty()) {
      _matches.add(".*?\\.txt");
    }

    JspwikiMigrate tool = new JspwikiMigrate();
    tool.setDefaultExcludes(_defaultExcludes);
    tool.setMatches((String[]) _matches.toArray(new String[0]));
    tool.setNotmatches((String[]) _notmatches.toArray(new String[0]));
    tool.setSrcdir(new File(_srcdir));
    tool.setDestdir(new File(_destdir));
    tool.setTransforms(_transforms);
    tool.setAttachdir(new File(_attachdir));
    tool.debug = _debug;
    tool.migrate();
  }
}
