/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.PrintWriter;
import java.io.Reader;
import net.ugorji.oxygen.markup.MarkupParser;
import net.ugorji.oxygen.markup.MarkupParserFactory;
import net.ugorji.oxygen.markup.MarkupRenderContext;
import net.ugorji.oxygen.markup.MarkupRenderEngine;

public class WikiParser2 {

  // protected WikiCategoryEngine wce;
  private WikiParser2Base mbase;
  private MarkupParser mp;

  public WikiParser2(
      Reader _r,
      PrintWriter _w,
      MarkupRenderContext _rc,
      MarkupRenderEngine _re,
      int maxNumParagraphs)
      throws Exception {
    MarkupParserFactory _mpf = WikiLocal.getWikiCategoryEngine().getMarkupParserFactory();
    doInit(_mpf, _r, _w, _rc, _re, maxNumParagraphs);
  }

  public WikiParser2(
      MarkupParserFactory _mpf,
      Reader _r,
      PrintWriter _w,
      MarkupRenderContext _rc,
      MarkupRenderEngine _re,
      int maxNumParagraphs)
      throws Exception {
    doInit(_mpf, _r, _w, _rc, _re, maxNumParagraphs);
  }

  private void doInit(
      MarkupParserFactory _mpf,
      Reader _r,
      PrintWriter _w,
      MarkupRenderContext _rc,
      MarkupRenderEngine _re,
      int maxNumParagraphs)
      throws Exception {
    mp = _mpf.newMarkupParser(_r);
    mbase = new WikiParser2Base();
    mbase.setRenderContext(_rc);
    mbase.setRenderEngine(_re);
    mbase.setWriter(_w);
    mbase.setMaxNumParagraphs(maxNumParagraphs);
    mp.setMarkupParserBase(mbase);
  }

  public void markupToHTML() throws Exception {
    mp.markupToHTML();
  }

  public MarkupParser getMarkupParser() {
    return mp;
  }
}
