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
import net.ugorji.oxygen.util.FreemarkerTemplateHelper;

public class WikiTemplateFilesHandler extends FreemarkerTemplateHelper {
  public WikiTemplateFilesHandler() {
    Map staticModelStrings = new HashMap();
    staticModelStrings.put("WikiConstants", "net.ugorji.oxygen.wiki.WikiConstants");
    staticModelStrings.put("WikiUtils", "net.ugorji.oxygen.wiki.WikiUtils");
    staticModelStrings.put("WikiEvent", "net.ugorji.oxygen.wiki.WikiEvent");
    staticModelStrings.put("StringUtils", "net.ugorji.oxygen.util.StringUtils");
    init(new String[] {"/net/ugorji/oxygen/wiki/templatefiles", "/net/ugorji/oxygen/web"}, staticModelStrings);
  }
}
