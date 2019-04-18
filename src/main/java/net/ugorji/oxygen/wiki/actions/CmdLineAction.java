/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import net.ugorji.oxygen.util.ProcessHandler;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;

public class CmdLineAction extends GenericWikiWebAction {

  public int render() throws Exception {
    // getPrefilledModel();
    workOnOS();
    showJSPView("cmdline.jsp");
    return RENDER_COMPLETED;
  }

  private static Map getPrefilledModel() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder lh = WikiLocal.getWikiLinkHolder();
    Map model = (Map) lh.getAttribute("wiki.model.cmdline");
    if (model == null) {
      // System.out.println("model is null");
      model = new HashMap();

      model.put("exitcode", new Integer(0));
      model.put("outerrstr", "");
      String cmdline = request.getParameter("cmdline");
      if (cmdline == null) {
        cmdline = "\n";
      }
      model.put("cmdline", cmdline);
      String dir = request.getParameter("dir");
      if (dir == null) {
        dir = ".";
      }
      model.put("dir", dir);
      lh.setAttribute("wiki.model.cmdline", model);
    }
    // System.out.println("model: " + model);
    return model;
  }

  private void workOnOS() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    Map model = getPrefilledModel();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    if (request.getParameter(WikiConstants.SUBMIT_REQUEST_PARAMETER) != null) {
      String dir = (String) model.get("dir");
      String cmdline = (String) model.get("cmdline");
      cmdline = cmdline.trim();
      StringWriter stw = new StringWriter();
      Process p = Runtime.getRuntime().exec(cmdline, null, new File(dir));
      ProcessHandler.handle(p, stw, null, true);
      int exitcode = p.exitValue();
      String outerrstr = stw.toString();

      model.put("exitcode", new Integer(exitcode));
      model.put("outerrstr", outerrstr);
    }
    // wlh.setAction("cmdline");
    wlh.setAttribute("wiki.model.cmdline", model);
  }
}
