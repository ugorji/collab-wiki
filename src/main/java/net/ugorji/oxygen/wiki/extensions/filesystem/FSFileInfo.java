/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import net.ugorji.oxygen.io.VirtualFile;
import net.ugorji.oxygen.io.VirtualPlainFile;

public class FSFileInfo implements Serializable, Comparable {
  public Date date;
  public int rev;
  public int change;
  public transient VirtualFile file;
  public String description;
  // public Properties descriptionProperties;
  public long size = -1;
  public boolean deleteFileOnSubmit = false;

  public FSFileInfo() {}

  public String toString() {
    return "FSFileInfo: "
        + " file: "
        + file
        + " size: "
        + size
        + " change: "
        + change
        + " rev: "
        + rev
        + " deleteFileOnSubmit: "
        + deleteFileOnSubmit
        + " date: "
        + date
        + " description: "
        + description;
  }

  public int compareTo(Object o) {
    return rev - ((FSFileInfo) o).rev;
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
    String fpath = ((file != null && file instanceof VirtualPlainFile) ? file.getPath() : "-");
    s.writeObject(fpath);
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    String fpath = (String) s.readObject();
    if (!(fpath.equals("-"))) {
      file = new VirtualPlainFile(new File(fpath));
    }
  }
}
