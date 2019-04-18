/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import net.ugorji.oxygen.util.Closeable;
import net.ugorji.oxygen.util.OxygenIntRange;

/**
 * This defines the contract that an Attachment Provider must complete Each WikiCategoryEngine has a
 * WikiAttachmentProvider, that it calls to get access to the attachments
 *
 * @author ugorji
 */
public interface WikiAttachmentProvider extends Closeable {

  int getInitialVersion();

  void prepare(WikiCategoryEngine wce) throws Exception;

  boolean attachmentExists(String pagerep, String attachment) throws Exception;

  boolean supportsAttachmentVersions();

  WikiProvidedObject getAttachment(String pagerep, String attachment, int version) throws Exception;

  InputStream getAttachmentInputStream(String pagerep, WikiProvidedObject attach) throws Exception;

  String[] getAttachmentNames(String pagerep, boolean deleted) throws Exception;
  /** Returns a map of pagename<String> to List of attachment names (List<String>) */
  Map getAllAttachmentNames(String parentPageRep, int maxdepth, boolean deleted) throws Exception;

  WikiProvidedObject[] getAttachmentVersionHistory(String pagerep, String attachment)
      throws Exception;

  void saveAttachment(String pagerep, String attachment, File f, Properties attributes)
      throws Exception;

  void deleteAttachment(String pagerep, String attachment, Properties atts) throws Exception;

  void deleteAttachmentVersions(
      String pagerep, String attachment, Properties atts, OxygenIntRange versions) throws Exception;
}
