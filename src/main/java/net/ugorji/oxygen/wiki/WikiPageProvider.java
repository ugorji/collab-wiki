/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.Reader;
import java.util.Properties;
import net.ugorji.oxygen.util.Closeable;
import net.ugorji.oxygen.util.OxygenIntRange;
import net.ugorji.oxygen.util.OxygenRevision;

/**
 * A provider of Wiki pages, with the appropriate call backs Note that these can be persisted and
 * retrieved anyway, e.g. a database, filesystem, zip/jar archive, etc. Any implementation of this
 * can be plugged in, by configuring it appropriately in the properties files.
 */
public interface WikiPageProvider extends Closeable {
  int getInitialVersion();

  void prepare(WikiCategoryEngine wce) throws Exception;

  boolean pageExists(String pagerep) throws Exception;

  boolean supportsPageVersions();

  WikiProvidedObject getPage(String pagerep, int version) throws Exception;

  Reader getPageReader(WikiProvidedObject page) throws Exception;

  void savePage(String pagerep, String text, Properties attributes) throws Exception;

  void deletePage(String pagerep, Properties atts) throws Exception;

  void deletePageVersions(String pagerep, Properties atts, OxygenIntRange versions)
      throws Exception;
  /** returns the page versions, with the latest being returned first */
  WikiProvidedObject[] getPageVersionHistory(String pagerep) throws Exception;

  String[] getPageNames(String parentPageRep, int maxdepth, boolean deleted) throws Exception;
  /**
   * If r1 and r2 are less than getInitialVersion(), return the latest change Else either is less
   * than getInitialVersion(), treat it as the latest version
   */
  OxygenRevision getPageRevision(String pagerep, int r1, int r2) throws Exception;
}
