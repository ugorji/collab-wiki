/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

/**
 * A WikiProvidedObject is the base superclass for WikiPage and WikiAttachment. It has some base
 * information, and a number of attributes. Attributes can be author, version-comments, etc
 *
 * @author ugorji
 */
public class WikiProvidedObject implements Serializable {
  /**
   * If this version number is passed, it means we want the most recent version of the page, but do
   * not care so much for details (like last modified, etc)
   */
  public static final int VERSION_LATEST_DETAILS_UNNECESSARY = -1;
  /**
   * If this version number is passed, it means we want the most recent version of the page, and
   * MUST have the accurate details (like last modified, etc)
   */
  public static final int VERSION_LATEST_DETAILS_NECESSARY = -2;

  private String name;
  private int version = -1;
  private Date lastModified = new Date();
  private long size = -1;

  private Properties atts = new Properties();

  public WikiProvidedObject(String _name, Date _lastModified, long _size, int _version) {
    name = _name;
    lastModified = _lastModified;
    version = _version;
    size = _size;
  }

  public WikiProvidedObject(String _name, Date _lastModified, long _size) {
    this(_name, _lastModified, _size, VERSION_LATEST_DETAILS_UNNECESSARY);
  }

  public WikiProvidedObject(String _name) {
    name = _name;
  }

  public WikiProvidedObject() {}

  public String getAttribute(String key) {
    return (String) atts.get(key);
  }

  public void setAttribute(String key, String value) {
    atts.setProperty(key, value);
  }

  public Properties getAttributes() {
    return atts;
  }

  public void setAttributes(Properties p) {
    atts = p;
  }

  public String getName() {
    return name;
  }

  public Date getDate() {
    return lastModified;
  }

  public long getSize() {
    return size;
  }

  public int getVersion() {
    return version;
  }

  public void setName(String s) {
    name = s;
  }

  public void setDate(Date d) {
    lastModified = d;
  }

  public void setSize(long l) {
    size = l;
  }

  public void setVersion(int i) {
    version = i;
  }
}

/*
public void copyFrom(WikiProvidedObject source) {
  name = source.name;
  version = source.version;
  lastModified = source.lastModified;
  size = source.size;
  atts = source.atts;
}
*/
