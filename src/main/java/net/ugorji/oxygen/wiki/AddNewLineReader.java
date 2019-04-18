/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/* make package-private now. Not to be used */
class AddNewLineReader extends FilterReader {
  private boolean newLineNotAdded = true;

  public AddNewLineReader(Reader in) {
    super(in);
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    int num = super.read(cbuf, off, len);
    if (newLineNotAdded && num == -1 && len > 0 && off < cbuf.length) {
      cbuf[off] = '\n';
      newLineNotAdded = false;
      num = 1;
    }
    return num;
  }

  public int read() throws IOException {
    int num = super.read();
    if (newLineNotAdded && num == -1) {
      num = '\n';
      newLineNotAdded = false;
    }
    return num;
  }
}
