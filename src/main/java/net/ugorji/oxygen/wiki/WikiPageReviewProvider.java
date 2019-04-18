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

/**
 * A provider of wiki page reviews. Note that these can be persisted and retrieved anyway, e.g. a
 * database, filesystem, zip/jar archive, etc. Any implementation of this can be plugged in, by
 * configuring it appropriately in the properties files.
 *
 * @author ugorji
 */
public interface WikiPageReviewProvider extends Closeable {
  void prepare(WikiCategoryEngine wce) throws Exception;

  WikiProvidedObject getPageReview(String pagerep, String reviewname, int version) throws Exception;

  String[] getPageReviewNames(String pagerep, boolean deleted) throws Exception;

  Reader getPageReviewReader(String pagerep, WikiProvidedObject page) throws Exception;

  void savePageReview(String pagerep, String text, Properties attributes) throws Exception;

  void deletePageReview(String pagerep, String reviewname, Properties atts) throws Exception;
}
