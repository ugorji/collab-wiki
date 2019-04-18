/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import net.ugorji.oxygen.markup.MarkupParserBase;
import net.ugorji.oxygen.markup.MarkupRenderContext;
import net.ugorji.oxygen.markup.MarkupRenderEngine;

public class DefaultWikiRenderEngine extends MarkupRenderEngine {

  public void render(
      final Writer out,
      final Reader in,
      final MarkupRenderContext context,
      final int maxNumParagraphs)
      throws IOException {
    if (out == null) {
      return;
    }
    PrintWriter pw = null;
    if (out instanceof PrintWriter) {
      pw = (PrintWriter) out;
    } else {
      pw = new PrintWriter(out);
    }

    final PrintWriter _pw = pw;
    try {
      WikiParser2 parser = new WikiParser2(in, _pw, context, this, maxNumParagraphs);
      parser.markupToHTML();
    } catch (MarkupParserBase.MaxParagraphsExceededSignal mpes) {
      // no-op
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    } finally {
      flushPW(_pw);
    }
  }
}
