/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.util.HashMap;
import java.util.Map;
import net.ugorji.oxygen.util.OxygenUtils;

/** Represents a Wiki Event */
public class WikiEvent {
  // note. Do not arbitrarily change these. Some files (e.g. templates, resource files, etc) assume
  // that the positions here do not change
  public static final int PAGE_SAVED = OxygenUtils.makeFlag(0);
  public static final int PAGE_DELETED = OxygenUtils.makeFlag(1);
  public static final int ATTACHMENT_SAVED = OxygenUtils.makeFlag(2);
  public static final int ATTACHMENT_DELETED = OxygenUtils.makeFlag(3);
  public static final int REVIEW_SAVED = OxygenUtils.makeFlag(4);
  public static final int REVIEW_DELETED = OxygenUtils.makeFlag(5);
  public static final int PAGE_VERSIONS_DELETED = OxygenUtils.makeFlag(6);
  public static final int ATTACHMENT_VERSIONS_DELETED = OxygenUtils.makeFlag(7);

  public static final String PAGE_NAME_KEY = "page.name";
  public static final String PAGE_TEXT_ORIGINAL_KEY = "page.text.original";
  public static final String PAGE_TEXT_KEY = "page.text";
  public static final String PAGE_ATTRIBUTES_KEY = "page.attributes";
  public static final String ATTACHMENT_NAME_KEY = "attachment.name";
  public static final String ATTACHMENT_ATTRIBUTES_KEY = "attachment.attributes";
  public static final String REVIEW_ATTRIBUTES_KEY = "review.attributes";
  public static final String REVIEW_NAME_KEY = "review.name";
  public static final String REVIEW_TEXT_KEY = "review.text";
  public static final String MINOR_EDIT_FLAG_KEY = "minor.edit";
  public static final String VERSIONS_KEY = "versions";

  private Map atts = new HashMap();
  private int type = -1;

  public WikiEvent(int _type, Map attributes) {
    setAttributes(attributes);
    setType(_type);
  }

  public WikiEvent(int _type) {
    setType(_type);
  }

  public WikiEvent() {}

  public Map getAttributes() {
    return atts;
  }

  public void setAttributes(Map m) {
    atts = m;
  }

  public int getType() {
    return type;
  }

  public void setType(int i) {
    type = i;
  }

  public void setAttribute(Object key, Object val) {
    atts.put(key, val);
  }

  public Object getAttribute(Object key) {
    return atts.get(key);
  }
}
