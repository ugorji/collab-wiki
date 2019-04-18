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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import net.ugorji.oxygen.manager.OxyManager;
import net.ugorji.oxygen.manager.UserPreferencesManager;
import net.ugorji.oxygen.util.*;
import net.ugorji.oxygen.web.*;
import net.ugorji.oxygen.web.MarkupContainerEngine;

/**
 * This manages an overall wiki engine. It keeps track of all category engine delegates, and its
 * base properties It also has a runtime persistence manager, which persists runtime info, like user
 * preferences, etc.
 *
 * @author ugorji
 */
public class WikiEngine extends MarkupContainerEngine {
  // This is never removed, or cleared ... once it is set ...
  private final Properties initProps;
  private Map wceMap = new HashMap();
  private List wceNames = new ArrayList();
  private File runtimeDir = null;
  private File configDir = null;
  private WikiRuntimePersistenceManager rtpersistmgr = null;
  private List registeredCategoryNames = new ArrayList();
  private WikiTemplateFilesHandler tmplFilesHdlr;

  public WikiEngine(Properties initProps0) throws Exception {
    OxygenUtils.info("WikiEngine initialization starting ...");
    long lcurr = System.currentTimeMillis();
    initProps0 = ((initProps0 == null) ? new Properties() : initProps0);
    initProps = initProps0;
    props = new Properties();
    try {
      WikiLocal.setWikiEngine(this);
      WebLocal.setWebContainerEngine(this);

      loadInitProps();

      // we don't do this within reset, since we always want to maintain the
      // state of the locks even if the engine is being reset
      longTermLock = new SimpleLock();
      shortTermAcquiredLock = new SimpleLock.Strict();

      reset(false);
    } finally {
      WebLocal.setI18n(null);
      WikiLocal.setWikiEngine(null);
      WebLocal.setWebContainerEngine(null);
    }
    OxygenUtils.info(
        "WikiEngine initialization completed in " + (System.currentTimeMillis() - lcurr) + " ms");
  }

  private void loadInitProps() throws Exception {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    InputStream is = cl.getResourceAsStream("net/ugorji/oxygen/wiki/init.properties");
    // System.out.println("WikiEngine: init.properties: is: " + is);
    initProps.load(is);
    CloseUtils.close(is);
    is = cl.getResourceAsStream("oxygen.wiki.config.override.properties");
    if (is != null) {
      initProps.load(is);
      CloseUtils.close(is);
    }
    // System.out.println("WikiEngine: initProps: " + initProps);
    propReplaceReferences(initProps);
  }

  public Properties getInitProps() {
    return initProps;
  }

  public boolean isCategoryNameRegistered(String _name) {
    // System.out.println("registeredCategoryNames: " + registeredCategoryNames);
    return registeredCategoryNames.contains(_name);
  }

  /**
   * Reloads the engine, without touching the category engines This closes engine-wide handlers,
   * reloads the properties, and re-initializes engine-wide handlers
   */
  public void reloadEngineOnly() throws Exception {
    String s = null;

    closeEngineOnly();
    reloadProperties();

    defaultLocale = OxygenUtils.stringToLocale(getProperty(WikiConstants.LOCALE_KEY, null));

    i18n = new I18nManager("net.ugorji.oxygen.wiki.WikiResources", defaultLocale);
    WebLocal.setI18n(i18n.getI18n(null));

    preInitPluginMgr = new PluginManager(props, "net.ugorji.oxygen.wiki.plugin.preinit.");
    preInitPluginMgr.start();

    tmplFilesHdlr = new WikiTemplateFilesHandler();

    s = getProperty(WikiConstants.ENGINE_CACHE_MANAGER_KEY, null);
    if (WikiConstants.NULL.equals(s)) {
      OxygenProxy oxypy = new OxygenProxy(null, new Class[] {OxygenCacheManager.class});
      cachemgr = (OxygenCacheManager) oxypy.getProxy();
    } else {
      cachemgr = (OxygenCacheManager) Class.forName(s).newInstance();
      cachemgr.prepare();
    }

    // long checkinterval = Long.parseLong(getProperty(WikiConstants.ENGINE_TIMEBASED_INTERVAL_KEY,
    // null));

    Properties p2 = new Properties();
    OxygenUtils.extractProps(getProperties(), p2, OxyManager.PROPS_PREFIX, false);
    prefsMgr =
        (UserPreferencesManager)
            Class.forName(getProperty(WikiConstants.USERPREFERENCES_MANAGER_KEY, null))
                .newInstance();
    prefsMgr.init(p2);

    rtpersistmgr =
        (WikiRuntimePersistenceManager)
            Class.forName(getProperty(WikiConstants.ENGINE_RUNTIME_PERSISTENCE_MANAGER_KEY, null))
                .newInstance();
    rtpersistmgr.prepare();

    actionMgr = new ActionManager(props, "net.ugorji.oxygen.wiki.action.");

    if ("true".equals(getProperty(WikiConstants.CLEAR_CACHE_ON_STARTUP_KEY, null))) {
      cachemgr.clear();
    }

    postInitPluginMgr = new PluginManager(props, "net.ugorji.oxygen.wiki.plugin.postinit.");
    postInitPluginMgr.start();
  }

  /**
   * Subset of reloading the engine It resets properties, locale, i18n, template files handler,
   * actions
   */
  public void reloadEngineMetadata() throws Exception {
    String s = null;
    CloseUtils.close(tmplFilesHdlr);
    reloadProperties();
    defaultLocale = OxygenUtils.stringToLocale(getProperty(WikiConstants.LOCALE_KEY, null));
    i18n = new I18nManager("net.ugorji.oxygen.wiki.WikiResources", defaultLocale);
    WebLocal.setI18n(i18n.getI18n(null));
    tmplFilesHdlr = new WikiTemplateFilesHandler();
    actionMgr = new ActionManager(props, "net.ugorji.oxygen.wiki.action.");
  }

  public String getName() {
    return getProperty(WikiConstants.ENGINE_NAME_KEY, null);
  }

  public String getCharacterEncoding() {
    return getProperty(WikiConstants.ENCODING_KEY, null);
  }

  public synchronized void close() {
    // System.out.println("wceNames: " + wceNames);
    // Thread.dumpStack();
    OxygenUtils.info("WikiEngine closing ...");
    long lcurr = System.currentTimeMillis();
    closeCategories();
    closeEngineOnly();
    timer.cancel();
    timer = null;
    CloseUtils.close(shortTermAcquiredLock);
    CloseUtils.close(longTermLock);
    int initTimeSecs = (int) ((System.currentTimeMillis() - lcurr) / 1000);
    OxygenUtils.info("WikiEngine is closed in " + initTimeSecs + " seconds");
  }

  /**
   * Get a WikiCaegoryEngine mapped to a given name
   *
   * @param _name name of a category
   * @return
   */
  public WikiCategoryEngine getWikiCategoryEngine(String _name) {
    if (_name == null) {
      return null;
    }
    WikiCategoryEngine wce0 = (WikiCategoryEngine) wceMap.get(_name);
    if (wce0 != null) {
      wce0.ensureReadyForRequestHandling();
    }
    return wce0;
  }

  public WikiCategoryEngine retrieveWikiCategoryEngine(String _name) {
    WikiCategoryEngine wce0 = getWikiCategoryEngine(_name);
    if (wce0 == null) {
      throw new WebResourceNotFoundException(
          WebLocal.getI18n().str("general.category_not_recognized", _name));
    }
    return wce0;
  }

  public WikiTemplateFilesHandler getWikiTemplateFilesHandler() {
    return tmplFilesHdlr;
  }

  public WikiRuntimePersistenceManager getWikiRuntimePersistenceManager() {
    return rtpersistmgr;
  }

  /** Get an action, throwing an WebResourceNotFoundException if not found */
  public WebAction getAction(String s) {
    // System.out.println(" ... " + s + " ... " + Arrays.asList(actionMgr.getActionKeys()));
    WebAction waction = (WebAction) actionMgr.getAction(s);
    if (waction == null) {
      throw new WebResourceNotFoundException(WebLocal.getI18n().str("general.action_not_exist", s));
    }
    return waction;
  }

  /**
   * Load a category engine. It throws an exception if a category engine already exists for that
   * name, or there is any exception loading the category engine
   *
   * @param _name
   * @throws Exception
   */
  public synchronized void loadWikiCategoryEngine(String _name) throws Exception {
    OxygenUtils.info("WikiEngine starting to load section: " + _name);
    if (!isCategoryNameRegistered(_name)) {
      throw new WikiException(
          i18n.getI18n(getDefaultLocale()).str("general.category_not_registered", _name));
    }
    WikiCategoryEngine wce = (WikiCategoryEngine) wceMap.get(_name);
    if (wce != null) {
      throw new WikiException(
          i18n.getI18n(getDefaultLocale()).str("general.category_engine_already_loaded", _name));
    }
    long lcurr = System.currentTimeMillis();
    wce = new WikiCategoryEngine(this, _name);
    wceMap.put(wce.getName(), wce);
    // System.out.println("wceNames: " + wceNames);
    if (!wceNames.contains(_name)) {
      // System.out.println("Adding " + _name + " to wceNames");
      wceNames.add(_name);
    }
    // System.out.println("wceNames: " + wceNames);
    OxygenUtils.info(
        "WikiEngine loaded section: "
            + _name
            + " in "
            + (System.currentTimeMillis() - lcurr)
            + " ms");
  }

  /**
   * Unload a category engine.
   *
   * @param _name
   * @throws Exception
   */
  public synchronized void unloadWikiCategoryEngine(String _name) {
    // System.out.println("Trying to close WikiCategoryEngine: " + _name);
    WikiCategoryEngine wce = (WikiCategoryEngine) wceMap.get(_name);
    if (wce == null) {
      return;
    }
    // System.out.println("... Closed");
    long lcurr = System.currentTimeMillis();
    wce.close();
    wceMap.remove(_name);
    wceNames.remove(_name);
    OxygenUtils.info(
        "WikiEngine unloaded section: "
            + _name
            + " in "
            + (System.currentTimeMillis() - lcurr)
            + " ms");
  }

  /**
   * Convenience method to unload and then load a category engine
   *
   * @param _name
   * @throws Exception
   */
  public synchronized void reloadWikiCategoryEngine(String _name) throws Exception {
    unloadWikiCategoryEngine(_name);
    loadWikiCategoryEngine(_name);
  }

  public String[] getRegisteredAndLoadedWikiCategoryNames() {
    ArrayList al = new ArrayList();
    // System.out.println("wceNames: " + wceNames);
    for (Iterator itr = wceNames.iterator(); itr.hasNext(); ) {
      String s = (String) itr.next();
      if (registeredCategoryNames.contains(s)) {
        al.add(s);
      }
    }
    return (String[]) al.toArray(new String[0]);
  }

  /**
   * Gets all the category names known by the WikiEngine, whether or not they are loaded.
   *
   * @return
   */
  public String[] getRegisteredWikiCategoryNames() {
    return (String[]) registeredCategoryNames.toArray(new String[0]);
  }

  void ensureReadyForRequestHandling() {}

  private void closeEngineOnly() {

    CloseUtils.close(cachemgr);
    CloseUtils.close(rtpersistmgr);

    CloseUtils.close(tmplFilesHdlr);
    CloseUtils.close(postInitPluginMgr);

    clearAttributes();

    tmplFilesHdlr = null;
    cachemgr = null;
    rtpersistmgr = null;
    runtimeDir = null;
    configDir = null;

    defaultLocale = null;
    registeredCategoryNames.clear();
    props.clear();
    // renderEngine = null;
    CloseUtils.close(preInitPluginMgr);
  }

  private void closeCategories() {
    String[] s = (String[]) wceNames.toArray(new String[0]);
    // System.out.println("Category names to close: " + Arrays.asList(s));
    for (int i = 0; i < s.length; i++) {
      unloadWikiCategoryEngine(s[i]);
    }
  }

  public void reset() throws Exception {
    reset(true);
  }

  private void reset(boolean doCloseFirst) throws Exception {
    // OxyLocal.set(Boolean.class, Boolean.TRUE);
    if (doCloseFirst) {
      close();
    }
    if (timer == null) {
      timer = new Timer(true);
    }
    reloadEngineOnly();
    for (Iterator itr = registeredCategoryNames.iterator(); itr.hasNext(); ) {
      String tok = (String) itr.next();
      try {
        loadWikiCategoryEngine(tok);
      } catch (Exception exc) {
        OxygenUtils.error(exc);
      }
    }
    if (doCloseFirst) {
      // WebLocal.setWebContainerEngine(this);
    }
  }

  /** Reloads the properties, from the oxywiki-properties file. */
  private void reloadProperties() throws Exception {
    props.clear();
    registeredCategoryNames.clear();

    if (initProps != null) {
      for (Enumeration enum0 = initProps.propertyNames(); enum0.hasMoreElements(); ) {
        String _key = (String) enum0.nextElement();
        String _val = initProps.getProperty(_key);
        props.setProperty(_key, _val);
      }
    }

    File f = new File(getConfigDirectory(), WikiConstants.ENGINE_PROPS_FILE_PREFIX + ".properties");
    if (f.exists()) {
      FileInputStream fis = new FileInputStream(f);
      props.load(fis);
      CloseUtils.close(fis);
    }

    propReplaceReferences(props);
    
    String s = props.getProperty(WikiConstants.ENGINE_CATEGORIES_KEY);
    StringTokenizer stz = new StringTokenizer(s, ", ");
    while (stz.hasMoreTokens()) {
      String tok = stz.nextToken();
      registeredCategoryNames.add(tok);
    }
  }
}
