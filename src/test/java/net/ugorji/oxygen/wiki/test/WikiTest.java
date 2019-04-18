
/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */


package net.ugorji.oxygen.wiki.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ugorji.oxygen.markup.parser.MarkupParser1Factory;
import net.ugorji.oxygen.util.ExceptionList;
import net.ugorji.oxygen.util.NullWriter;
import net.ugorji.oxygen.util.OxygenRevision;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.wiki.DefaultWikiRenderEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiEvent;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiParser2;
import net.ugorji.oxygen.wiki.WikiRenderContext;
import net.ugorji.oxygen.wiki.extensions.WikiEmailNotifier;
import net.ugorji.oxygen.wiki.extensions.filesystem.FSPageProvider;
import net.ugorji.oxygen.web.WebLocal;

public class WikiTest {
  
  public static void testparser(String[] args) throws Exception {
    System.out.println("Hi ... ");
    String filename = args[0];
    Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
    StringWriter stw = new StringWriter();
    PrintWriter pw = new PrintWriter(stw);
    // PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
    try {
      WikiParser2 parser = new WikiParser2(r, pw, new WikiRenderContext(null), new DefaultWikiRenderEngine(), Integer.MAX_VALUE);
      parser.markupToHTML();
      pw.flush();
    } finally {
      Thread.sleep(1000);
      pw.flush();
      System.out.println(stw.toString());
    }
  }

  public static void testcheckdir(String[] args) throws Exception {
    int numSuccesses = 0;
    int numFailures = 0;
    ExceptionList exclist = new ExceptionList("Exceptions for files failing wiki test");
    File dir = new File(args[0]);
    ArrayList al = new ArrayList();
    FileFilter ff = new FileFilter() {
        private Pattern pattern = Pattern.compile("PAGE.TXT|REVIEW.*?TXT", Pattern.DOTALL);
        public boolean accept(File f) {
          return (pattern.matcher(f.getName()).matches());
        }
      };
    OxygenUtils.listFiles(dir, Integer.MAX_VALUE, al, ff);
    Properties p = new Properties();
    File persistDir = new File(System.getProperty("java.io.tmpdir"), "wikitest");
    File f2 = new File(persistDir, "config/userpreferences.properties");
    f2.getParentFile().mkdirs();
    f2.createNewFile();
    p.setProperty("net.ugorji.oxygen.wiki.persistence.dir", persistDir.getAbsolutePath().replace('\\', '/'));
    WikiLocal.setWikiEngine(new WikiEngine(p));
    for(Iterator itr = al.iterator(); itr.hasNext(); ) {
      File f = (File)itr.next();
      try {
        //System.out.println(" ............ " + f);
        Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8");
        WikiParser2 parser = new WikiParser2(new MarkupParser1Factory(), r, new PrintWriter(new NullWriter()), new WikiRenderContext(null), new DefaultWikiRenderEngine(), Integer.MAX_VALUE);
        parser.markupToHTML();
        parser.getMarkupParser().getMarkupParserBase().getWriter().flush();
        System.out.println(" SUCCESS: " + f);
        //System.out.flush();
        numSuccesses++;
      } catch(Exception exc) {
        //System.out.println(" ............ " + f);
        //System.out.println("ERROR: " + f);
        //System.out.flush();
        //System.err.println("ERROR: " + f);
        System.out.println(" ERROR ............ " + f);
        System.err.println(" ERROR ............ " + f);
        exc.printStackTrace();
        //System.err.flush();
        exclist.addThrowable(exc);
        numFailures++;
      }
    }
    System.out.println("REPORT: Successes: " + numSuccesses + " Failures: " + numFailures);
    WikiLocal.setWikiEngine(null);
    if(exclist.numThrowables() > 0) {
      throw exclist;
    }
    
  }
  
  public static void testnotif(String[] args) throws Exception {
    Properties attributes = new Properties();
    attributes.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, "james");
    attributes.setProperty(WikiConstants.ATTRIBUTE_COMMENTS, "some arbitrary comments ...");
    WikiEvent we = new WikiEvent();
    we.setAttribute(WikiEvent.MINOR_EDIT_FLAG_KEY, Boolean.TRUE);
    WikiEmailNotifier.WENModel wenmodel = new WikiEmailNotifier.WENModel(we, null);
    String originalpagetext = "a\nb\nc\ndef";
    String pagetext = "ab\ndef\nkih";
    OxygenRevision wrev = OxygenRevision.getDiff(originalpagetext, pagetext, WebLocal.getI18n());
    Map tmplctx = new HashMap();
    tmplctx.put("hdlr", wenmodel);
    tmplctx.put("wrev", wrev);
    Writer w = new OutputStreamWriter(System.out);
    
    we.setType(WikiEvent.PAGE_SAVED);
    we.setAttribute(WikiEvent.PAGE_NAME_KEY, "ugorji");
    we.setAttribute(WikiEvent.PAGE_TEXT_ORIGINAL_KEY, originalpagetext);
    we.setAttribute(WikiEvent.PAGE_TEXT_KEY, pagetext);
    we.setAttribute(WikiEvent.PAGE_ATTRIBUTES_KEY, attributes);
    
    WikiLocal.getWikiEngine().getWikiTemplateFilesHandler().write(args[0], tmplctx, w);
    w.flush();
    
    we.setType(WikiEvent.ATTACHMENT_SAVED);
    we.setAttribute(WikiEvent.ATTACHMENT_NAME_KEY, "my-attachment.txt");
    we.setAttribute(WikiEvent.ATTACHMENT_ATTRIBUTES_KEY, attributes);
    
    WikiLocal.getWikiEngine().getWikiTemplateFilesHandler().write(args[0], tmplctx, w);
    w.flush();
    
    we.setType(WikiEvent.REVIEW_SAVED);
    we.setAttribute(WikiEvent.REVIEW_ATTRIBUTES_KEY, attributes);
    we.setAttribute(WikiEvent.REVIEW_TEXT_KEY, originalpagetext + pagetext);

    WikiLocal.getWikiEngine().getWikiTemplateFilesHandler().write(args[0], tmplctx, w);
    w.flush();
    
  }
  
  public static void testFindFilesWithTableLikeSeparatorInLink(String[] args) throws Exception {
    Pattern badLinkSrcPattern = Pattern.compile("\\[(.*?)\\|\\|(.*?)\\]");
    FileFilter ff = new FileFilter() {
        public boolean accept(File f) {
          return f.getName().equals(FSPageProvider.PAGENAME);
        }
      };
    List list = new ArrayList();
    OxygenUtils.listFiles(new File(args[0]), Integer.MAX_VALUE, list, ff);
    for(Iterator itr = list.iterator(); itr.hasNext(); ) {
      File f = (File)itr.next();
      String s = OxygenUtils.getFileContents(f);
      Matcher m = badLinkSrcPattern.matcher(s);
      if(m.find()) {
        System.out.println(f.getAbsolutePath());
        System.out.println("...... " + m.group(0));
        while(m.find()) {
          System.out.println("...... " + m.group(0));
        }
      }
    }
  }
  
  public static void main(String[] args) throws Exception {
    String command = args[0];
    String[] args2 = new String[args.length - 1];
    System.arraycopy(args, 1, args2, 0, args2.length);
    if(command.equals("-parser")) {
      testparser(args2);
    } else if(command.equals("-checkdir")) {
      testcheckdir(args2);
    } else if(command.equals("-notif")) {
      testnotif(args2);
    } else if(command.equals("-findbadlinks")) {
      testFindFilesWithTableLikeSeparatorInLink(args2);
    } 
  }
  
}
