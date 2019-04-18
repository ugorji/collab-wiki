/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.web.JCaptchaAction;

public class CaptchaAction extends GenericWikiWebAction {
  private JCaptchaAction jcaptcha = new JCaptchaAction();

  public int render() throws Exception {
    return jcaptcha.render();
  }
}
