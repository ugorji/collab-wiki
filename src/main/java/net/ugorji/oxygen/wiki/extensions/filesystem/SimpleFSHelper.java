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
 * Structure is something like this:
 * <pre>
     $pagerep/
       PAGE.TXT
       ATTACH1.DAT
       REVIEW.1.TXT
       .OXYWIKI/
         $filename.HEAD_VERSION_NUMBER.TXT
         $filename.$version.METADATA.TXT.GZ
         $filename.$version.$extension.GZ
   </pre>

   This class is being retire as of Apr 11, 2007. Instead, default helper is OxygenVersioningHelper.
   Making it abstract and commenting everything out.
*/

public abstract class SimpleFSHelper extends BaseFSHelper {}

/*
  private static final String HEAD_VERSION_FILENAME_SUFFIX = ".HEAD_VERSION_NUMBER.TXT";
  private static final int HEAD_VERSION_FILENAME_SUFFIX_LENGTH = HEAD_VERSION_FILENAME_SUFFIX.length();
  private static Pattern metadataHeadVerNumFilePathPattern = Pattern.compile("(.*?/)" + ".OXYWIKI" + "/(.*?)" + HEAD_VERSION_FILENAME_SUFFIX);

  public SimpleFSHelper() {
    METADATA_DIRECTORY = ".OXYWIKI";
  }

  public BufferedInputStream getFileContentsAsStream(VirtualFile _f, int rev) throws Exception {
    rev = getUsableVersion(_f, rev, true, true);
    BufferedInputStream br = null;

    VirtualFile _f2 = getMetadataDir(_f);
    if(_f2 != null) {
      VirtualFile _f3 = _f2.getChild(_f.getName() + "." + rev + getExtension(_f) + FSUtils.GZIP_SUFFIX);
      br = new BufferedInputStream(FSUtils.gzipInputStream(_f3));
    }
    return br;
  }

  public synchronized boolean isRepositoryInfoAvailable(VirtualFile _f) throws Exception {
    if(_f != null) {
      VirtualFile _f2 = _f.getParent().getChild(METADATA_DIRECTORY);
      if(_f2 != null && _f2.exists()) {
        VirtualFile _f3 = _f2.getChild(_f.getName() + HEAD_VERSION_FILENAME_SUFFIX);
        return (_f3 != null && _f3.exists());
      }
    }
    return false;
  }

  public String[] lookupNames(final VirtualFile f, int maxdepth, final boolean includeDeleted) throws Exception {
    //System.out.println("SimpleFSHelper.lookupNames: " + f.getPath());
    VirtualFileFilter vff = new VirtualFileFilter() {
        public boolean accept(VirtualFile vf) {
          try {
            String vfname = vf.getName();
            boolean b = (vf.getParent().getName().equals(METADATA_DIRECTORY) &&
                         !vf.getParent().getParent().getName().equals(METADATA_DIRECTORY) &&
                         vfname.endsWith(HEAD_VERSION_FILENAME_SUFFIX));
            if(b && !includeDeleted) {
              //if not includeDeleted, remove those without corresponding files on disk (or where latest version is not empty)
              vfname = vfname.substring(0, vfname.length() - HEAD_VERSION_FILENAME_SUFFIX.length());
              b = vf.getParent().getParent().getChild(vfname).exists();
            }
            //System.out.println("SimpleFSHelper: b: " + b + " vf: " + vf.getPath() + "");
            return b;
          } catch(Exception exc) {
            return false;
          }
        }
      };
    return doLookupNames(vff, metadataHeadVerNumFilePathPattern, f, maxdepth, includeDeleted);
  }

  protected FSFileInfo doGetEntryInfo(VirtualFile _f, int version) throws Exception {
    String s = null;
    FSFileInfo info = new FSFileInfo();
    info.file = _f;
    info.rev = version;
    info.description = "";
    VirtualFile _f2 = getMetadataDir(_f);
    if(_f2 != null) {
      VirtualFile _f4 = _f2.getChild(_f.getName() + "." + version + ".METADATA.TXT" + FSUtils.GZIP_SUFFIX);
      if(_f4 != null && _f4.exists()) {
        info.description = OxygenUtils.getTextContents(new InputStreamReader(FSUtils.gzipInputStream(_f4)), true);
        //try and set the size here
        Properties pp = StringUtils.stringToProps(info.description);
        if(!StringUtils.isBlank(s = pp.getProperty(WikiConstants.ATTRIBUTE_SIZE))) {
          info.size = Long.parseLong(s);
        }
      }
      _f4 = _f2.getChild(_f.getName() + "." + version + getExtension(_f) + FSUtils.GZIP_SUFFIX);
      if(_f4 != null && _f4.exists()) {
        info.date = new Date(_f4.lastModified());
        info.size = _f4.size();
      }
    }
    if(info.date == null) {
      //if no date found from repository, use the date of the actual living file
      info.date = new Date(_f.lastModified());
    }

    return info;
  }

  //if no version is available, returns -1
  protected synchronized int getCurrentVersion(VirtualFile _f) throws Exception {
    int currentver = -1;
    VirtualFile _f2 = getMetadataDir(_f);
    if(_f2 != null) {
      VirtualFile _f3 = _f2.getChild(_f.getName() + HEAD_VERSION_FILENAME_SUFFIX);
      if(_f3.exists()) {
        String s = OxygenUtils.getTextContents(new InputStreamReader(_f3.getInputStream()), true);
        currentver = Integer.parseInt(s.trim());
      }
    }
    return currentver;
  }

  protected void doAddOrEdit(VirtualWritableFile _f, Object newContent) throws Exception {
    int nextver = getNextVersion(_f);
    VirtualWritableFile _f2 = (VirtualWritableFile)_f.getParent().getChild(METADATA_DIRECTORY);
    _f2 = (VirtualWritableFile)_f2.getChild(_f.getName() + "." + nextver + getExtension(_f) + FSUtils.GZIP_SUFFIX);
    String encoding = getEncoding();
    FSUtils.preSaveOverwrite(_f, newContent, encoding, false);
    FSUtils.preSaveOverwrite(_f2, newContent, encoding, true);
  }

  protected FSFileInfo doMakeChange(String logmsg, VirtualWritableFile _f) throws Exception {
    int nextver = getNextVersion(_f);

    VirtualFile _f2 = getMetadataDir(_f);

    VirtualWritableFile _f3 = (VirtualWritableFile)_f2.getChild(_f.getName() + "." + nextver + ".METADATA.TXT" + FSUtils.GZIP_SUFFIX);
    String encoding = getEncoding();
    OutputStreamWriter osw = new OutputStreamWriter(FSUtils.gzipOutputStream(_f3), encoding);
    OxygenUtils.writeTextContents(osw, logmsg, true);

    FSFileInfo info = new FSFileInfo();
    info.description = logmsg;
    info.file = _f;
    info.rev = nextver;
    return info;
  }

  protected synchronized void doSubmit(FSFileInfo info) throws Exception {
    VirtualFile _f = info.file;
    int nextver = getNextVersion(_f);
    VirtualFile _f2 = _f.getParent().getChild(METADATA_DIRECTORY);
    VirtualWritableFile _f3 = (VirtualWritableFile)_f2.getChild(_f.getName() + HEAD_VERSION_FILENAME_SUFFIX);
    String encoding = getEncoding();
    OutputStreamWriter osw = new OutputStreamWriter(_f3.getOutputStream(), encoding);
    OxygenUtils.writeTextContents(osw, String.valueOf(nextver), true);
  }

}

*/
