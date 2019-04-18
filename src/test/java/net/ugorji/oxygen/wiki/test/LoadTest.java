
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
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.StringUtils;

public class LoadTest {
  private int numThreads = 30;
  private int numIterations = 10;
  private long thinktime = 100;
  private PrintWriter logwriter;
  private String urlprefix = "http://localhost:8080/oxywiki/p/";
  //urlprefix = "http://[fe80:0:0:0:0:5efe:a0a:468]:7001/oxywiki/p/";
  private String resourceFile = "net.ugorji.oxygen.wiki.test/loadtest.txt";
  
  private String[] links = null;
  private Random rand = new Random();

  private LoadTest() { }

  private void work() throws Exception {
    List l = new ArrayList();
    InputStream is = OxygenUtils.searchForResourceAsStream(resourceFile, getClass());
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line = null;
    while((line = br.readLine()) != null) {
      line = line.trim();
      if(!StringUtils.isBlank(line) && !line.startsWith("#")) {
        l.add(line);
      }
    } 
    CloseUtils.close(br);
    links = (String[])l.toArray(new String[0]);
    
    Thread[] threads = new Thread[numThreads];
    for(int i = 0; i < numThreads; i++) {
      Runner rr = this.new Runner(i);
      threads[i] = new Thread(OxygenUtils.topLevelThreadGroup(), rr);
      threads[i].start();
    }
    for(int i = 0; i < numThreads; i++) {
      threads[i].join();
    }
    System.out.println("-------- DONE ---------");
  }

  private String getNextURL() {
    return links[rand.nextInt(links.length)];
  }
  
  private synchronized void log(String[] cols) {
    StringBuffer buf = new StringBuffer().append(StringUtils.toCSVString(cols[0]));
    for(int i = 1; i < cols.length; i++) {
      buf.append(",").append(StringUtils.toCSVString(cols[i]));
    }
    logwriter.println(buf.toString());
    logwriter.flush();
  }

  private class Runner implements Runnable {
    private int index;
    private HttpClient client = new HttpClient(); 
    
    private Runner(int index0) {
      index = index0;
    }
    
    public void run() {
      for(int j = 0; j < numIterations; j++) {
        run0(j);
      }
    }
    
    private void run0(int j) {
      String s = getNextURL();
      String s2 = urlprefix + s;
      GetMethod method = new GetMethod(s2);
      String[] msg = new String[]{"", "", "", "", "", ""};
      int statusCode = -1;
      try {
        statusCode = client.executeMethod(method);              
      } catch(Exception exc) {
        msg[5] = exc.toString();
      } finally {
        int i = 0;
        msg[i++] = String.valueOf(index);
        msg[i++] = String.valueOf(j);
        msg[i++] = s;
        msg[i++] = String.valueOf(statusCode);  
        msg[i++] = ((statusCode == HttpStatus.SC_OK) ? "PASS" : "FAIL");
        log(msg);
        method.releaseConnection();
      } 
      if(thinktime > 0) {
        OxygenUtils.sleep(thinktime);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    String logfilename = System.getProperty("java.io.tmpdir") + "/loadtest.logfile.txt";
    
    LoadTest lt = new LoadTest();
    //System.out.println("args: " + Arrays.asList(args));
    
    for(int i = 0; i < args.length; i++) {
      if("-numthreads".equals(args[i])) {
        lt.numThreads = Integer.parseInt(args[++i]);
      } else if("-numiterations".equals(args[i])) {
        lt.numIterations = Integer.parseInt(args[++i]);
      } else if("-urlprefix".equals(args[i])) {
        lt.urlprefix = args[++i];
      } else if("-resourcefile".equals(args[i])) {
        lt.resourceFile = args[++i];
      } else if("-thinktime".equals(args[i])) {
        lt.thinktime = Long.parseLong(args[++i]);
      } else if("-logfile".equals(args[i])) {
        logfilename = args[++i];
      } 
    }
    
    PrintWriter pw = new PrintWriter(new FileWriter(logfilename, false));
    lt.logwriter = pw;
    try {
      lt.work();
    } finally {
      pw.flush();
      CloseUtils.close(pw);
    }
  }
    
}


