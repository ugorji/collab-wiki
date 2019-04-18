/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.util.Properties;
import net.ugorji.oxygen.util.Closeable;

/**
 * Handles persisting our configuration data to the config directory. It can consequently allow us
 * write files like oxywiki.properties, runtime-config-oxywiki.properties, etc Note that: All the
 * properties files here are just text files. Depending on the type of file, the person calling load
 * knows how to interprete. E.g. oxywiki.properties is interpreted as a properties file, etc etc etc
 *
 * @author ugorji
 */
public interface WikiRuntimePersistenceManager extends Closeable {
  /**
   * Does the initialization for this instance.
   *
   * @param wce
   * @throws Exception
   */
  void prepare() throws Exception;

  /**
   * Store some raw text into a file, relative to the engine's config directory
   *
   * @param fName The name of the file, relative to the engine's config directory
   * @param text The raw text to write to the file
   * @param atts For providers that like to store metadata, the atts contains author's name and
   *     comments
   * @throws Exception
   */
  void store(String fName, String text, Properties atts) throws Exception;

  /**
   * Read up the string contents from a file, relative to the engine's config directory
   *
   * @param fName
   * @return
   * @throws Exception
   */
  String load(String fName) throws Exception;
}
