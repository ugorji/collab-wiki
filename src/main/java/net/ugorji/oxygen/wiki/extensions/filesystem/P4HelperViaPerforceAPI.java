/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions.filesystem;

class P4HelperViaPerforceAPI {

  /*
  //private static int VERSION_MIN_VALID_NUMBER = 1;
  private Env p4Env;
  private String encoding = null;

  static {
    Debug.setDebugLevel(Debug.WARNING);
    Debug.setLogLevel(Debug.LOG_ONLY);
    if(Debug.getEventLog() == null) {
      Debug.setEventLog(new EventLog("P4EventLog", "p4-event-log.txt"));
    }
  }

  public P4HelperViaPerforceAPI(Properties p) {
    p4Env = new Env(p);
    encoding = p.getProperty(WikiConstants.ENCODING_KEY);
  }

  public void delete(int change, VirtualFile _vf) throws Exception {
    File _f = _vf.getFile();
    if(_f == null || !(_f.exists())) {
      return;
    }
    String fileName = _f.getAbsolutePath();
    String[] cmd = {"p4", "delete", "-c", String.valueOf(change), fileName};
    P4Process p = new P4Process(p4Env);
    p.exec(cmd);
    while (null != p.readLine()) {
      // ignore the output.
    }
    p.close();
  }

  public void addOrEdit(int changenum, VirtualFile _vf, File newContent) throws Exception {
    addOrEdit0(changenum, _vf, newContent);
  }

  public void addOrEdit(int changenum, VirtualFile _vf, String newContent) throws Exception {
    addOrEdit0(changenum, _vf, newContent);
  }

  public void submit(int changenum) throws Exception {
    Change change = getChange(changenum);
    change.submit();
  }

  public int makeChange(String logmsg)  throws Exception {
    Change change = new Change(p4Env);
    change.setDescription(logmsg);
    change.commit();
    return change.getNumber();
  }

  public int getHeadChange(VirtualFile f) throws Exception {
    FileEntry p4file = new FileEntry(p4Env, f.getPath());
    p4file.sync();
    return p4file.getHeadChange();
  }

  public FSFileInfo getEntryInfo(VirtualFile f, int version) throws Exception {
    FileEntry p4file = new FileEntry(p4Env, f.getPath());
    int topversion = p4file.getHeadRev();
    if(version >= VERSION_MIN_VALID_NUMBER && version != topversion) {
      p4file.setHeadRev(version);
    }
    p4file.sync();
    p4file.setHeadRev(version);

    FSFileInfo info = new FSFileInfo();
    info.file = f;
    info.date = new Date(p4file.getHeadTime() * 1000);
    info.rev = p4file.getHeadRev();
    info.change = p4file.getHeadChange();

    return info;
  }

  public List getEntryLog(VirtualFile f) throws Exception {
    List files = FileEntry.getFileLog(p4Env, f.getPath());
    List files2 = new ArrayList(files.size());
    for(Iterator itr = files.iterator(); itr.hasNext(); ) {
      FileEntry p4file = (FileEntry)itr.next();
      FSFileInfo info = new FSFileInfo();
      info.file = f;
      info.date = new Date(p4file.getHeadTime() * 1000);
      info.rev = p4file.getHeadRev();
      info.change = p4file.getHeadChange();
      files2.add(info);
    }
    return files2;
  }

  public String getFileContents(VirtualFile _f, int rev) throws Exception {
    FileEntry p4file = new FileEntry(p4Env, _f.getPath());
    String s = null;
    if(rev > 0) {
      s = p4file.getFileContents(p4Env, _f.getPath() + "#" + rev);
    } else {
      s = p4file.getFileContents(p4Env, _f.getPath());
    }
    return s;
  }

  public String getChangeDescription(int changenum) throws Exception {
    Change change = getChange(changenum);
    String log = change.getShortDescription();
    return log;
  }

  public void close() throws Exception {
    //Utils.cleanUp();
  }

  private Change getChange(int changenum) throws Exception {
    return Change.getChange(p4Env, changenum, true) ;
  }

  private void addOrEdit0(int changenum, VirtualFile _vf, Object newContent) throws Exception {
    File _f = _vf.getFile();
    String fileName = _f.getAbsolutePath();
    Change change = getChange(changenum);
    if(_f.exists()) {
      FileEntry.openForEdit(p4Env, fileName, false, false, false, change);
      DefaultFileSystemHelper.preSaveOverwrite(_vf, newContent, encoding, false);
    } else {
      _f.getParentFile().mkdirs();
      DefaultFileSystemHelper.preSaveOverwrite(_vf, newContent, encoding, false);
      FileEntry.openForAdd(p4Env, fileName, change);
    }
  }

  */
}
