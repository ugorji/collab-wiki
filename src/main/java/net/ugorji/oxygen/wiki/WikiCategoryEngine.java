/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import net.ugorji.oxygen.markup.*;
import net.ugorji.oxygen.util.*;
import net.ugorji.oxygen.web.WebAction;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;

/**
 * This represents the engine that handles a specific category. This allows different categories to
 * be managed differently. The engine for each category has its own implementation of page,
 * attachment and page review providers
 *
 * <p>NOTE: save/delete...Proxy are synchronized, so that we don't have to worry about some pages
 * being saved while another is being saved. This is kinda high-price-to-pay, but necessary until we
 * implement proper synchronization across all the providers. Else, 2 folks can edit a page
 * together, and each action is not atomic, and steps on other.
 *
 * <p>In general, be careful with synchronized methods, 'cos they can deadlock your app.
 *
 * @author ugorji
 */
public class WikiCategoryEngine implements OxygenEngine {

  private static String LAST_UPDATE_DATE_CACHE_GROUP = "wikicategoryenginelastupdate";

  private final Properties props;
  private Map attributes = new HashMap();

  private MarkupRenderEngine renderEngine = null;
  private String name;
  private WikiEngine engine;

  private WikiPageProvider pageProvider;
  private WikiPageReviewProvider pageReviewProvider;
  private WikiAttachmentProvider attachProvider;
  private WikiPageProvider pageProviderProxy;
  private WikiPageReviewProvider pageReviewProviderProxy;
  private WikiAttachmentProvider attachProviderProxy;
  private WikiEditManager editManager;
  private WikiTemplateHandler templateHdlr;
  private WikiIndexingManager indexmgr;
  private Date lastUpdateDate;
  private Map listeners = new HashMap();
  private Map macros = new HashMap();
  private List supportedLocales;
  private Locale defaultLocale;
  private SimpleLock longTermLock;

  private CensoredWordManager censoredMgr;
  private EmoticonsManager emoticonsMgr;

  // this has to be lazily loaded ... since we need to get some
  private ShorthandManager shorthandMgr;
  private boolean readyForHandlingRequests = false;

  private Set actionsNotSupported = new HashSet();

  private boolean syncSaveDelOnInternedStrings = false;

  private MarkupParserFactory markupParserFactory;

  /**
   * This is protected, since no-one is supposed to be calling this constructor except the over-all
   * WikiEngine. At this point, we combine the properties of this category, the base engine
   * properties, and initialize everything else from here. These include: template handlers,
   * providers We then create the indexes for finding references and doing a search
   *
   * @param _engine
   * @param _name
   * @throws Exception any exception that happens while initializing
   */
  protected WikiCategoryEngine(WikiEngine _engine, String _name) throws Exception {
    OxygenProxy oxypy = null;
    String s = null;
    name = _name;
    engine = _engine;
    longTermLock = new SimpleLock();

    // add default properties
    Map m0 = OxygenUtils.propsToTable(_engine.getProperties(), new Hashtable());
    m0.put(WikiConstants.NAME_KEY, name);
    // System.out.println("m0: " + m0);
    StringWriter fw = new StringWriter();
    _engine.getWikiTemplateFilesHandler().write("init.category.properties", m0, fw);
    CloseUtils.close(fw);
    s = fw.toString();
    // System.out.println(s);
    props = new Properties(engine.getProperties());
    props.putAll(StringUtils.stringToProps(s));

    File f =
        new File(
            engine.getConfigDirectory(),
            WikiConstants.ENGINE_PROPS_FILE_PREFIX + "-" + name + ".properties");
    if (f.exists()) {
      FileInputStream is = new FileInputStream(f);
      props.load(is);
      CloseUtils.close(is);
    }

    StringUtils.replacePropertyReferences(props);

    supportedLocales = new ArrayList();
    StringTokenizer stz =
        new StringTokenizer(getProperty(WikiConstants.SUPPORTED_LOCALES_KEY), ", \n\t");
    while (stz.hasMoreTokens()) {
      Locale locale = OxygenUtils.stringToLocale(stz.nextToken());
      if (locale != null) {
        supportedLocales.add(locale);
      }
    }

    defaultLocale = OxygenUtils.stringToLocale(getProperty(WikiConstants.LOCALE_KEY));

    stz = new StringTokenizer(getProperty(WikiConstants.NOT_SUPPORTED_ACTIONS_KEY), ", \n\t");
    while (stz.hasMoreTokens()) {
      actionsNotSupported.add(stz.nextToken());
    }

    s = getProperty(MarkupConstants.PARSER_FACTORY_CLASS_KEY);
    markupParserFactory = (MarkupParserFactory) Class.forName(s).newInstance();

    Properties p = getProperties();

    emoticonsMgr = new EmoticonsManager(p);
    censoredMgr = new CensoredWordManager(p);
    shorthandMgr = new ShorthandManager(p);

    Properties p2 = new Properties();

    OxygenUtils.extractProps(p, p2, WikiConstants.LISTENER_KEY_PREFIX, true);
    for (Enumeration enum0 = p2.propertyNames(); enum0.hasMoreElements(); ) {
      String name = (String) enum0.nextElement();
      Class clazz = Class.forName(p2.getProperty(name));
      try {
        WikiEventListener wp = (WikiEventListener) clazz.newInstance();
        wp.prepare(this);
        listeners.put(name, wp);
      } catch (Throwable exc) {
        OxygenUtils.error(exc);
      }
    }
    listeners.put("wcelistener", new WCEListener());

    p2 = new Properties();
    OxygenUtils.extractProps(p, p2, WikiConstants.MACRO_KEY_PREFIX, true);
    for (Enumeration enum0 = p2.propertyNames(); enum0.hasMoreElements(); ) {
      String name = (String) enum0.nextElement();
      Class clazz = Class.forName(p2.getProperty(name));
      try {
        MarkupMacro wp = (MarkupMacro) clazz.newInstance();
        // wp.prepare(this);
        macros.put(name, wp);
      } catch (Throwable exc) {
        OxygenUtils.error(exc);
      }
    }

    OxygenUtils.debug(
        "In WCE: net.ugorji.oxygen.wiki.page.provider = " + getProperty("net.ugorji.oxygen.wiki.page.provider"));

    s = getProperty(WikiConstants.RENDER_ENGINE_KEY);
    renderEngine = (MarkupRenderEngine) Class.forName(s).newInstance();
    renderEngine.setName(getName());

    templateHdlr = new WikiTemplateHandler(this);

    s = getProperty(WikiConstants.PAGE_PROVIDER_KEY);
    if (WikiConstants.NULL.equals(s)) {
      oxypy = new OxygenProxy(null, new Class[] {WikiPageProvider.class});
      pageProvider = (WikiPageProvider) oxypy.getProxy();
    } else {
      pageProvider = (WikiPageProvider) Class.forName(s).newInstance();
      pageProvider.prepare(this);
    }

    oxypy =
        new OxygenProxy(pageProvider) {
          protected Object doInvoke(Method m, Object[] args) throws Exception {
            String mName = m.getName();
            int i = 0;
            if (mName.equals("getPage")) {
              return setIfNotSetWPAttributes(m.invoke(target, args));
            } else if (mName.equals("savePage")) {
              savePageProxy((String) args[i++], (String) args[i++], (Properties) args[i++]);
            } else if (mName.equals("deletePage")) {
              deletePageProxy((String) args[i++], (Properties) args[i++]);
            } else if (mName.equals("deletePageVersions")) {
              deletePageVersionsProxy(
                  (String) args[i++], (Properties) args[i++], (OxygenIntRange) args[i++]);
            } else {
              return m.invoke(target, args);
            }
            return null;
          }
        };
    pageProviderProxy = (WikiPageProvider) oxypy.getProxy();

    s = getProperty(WikiConstants.PAGE_REVIEW_PROVIDER_KEY);
    // System.out.println("value of PAGE_REVIEW_PROVIDER_KEY: " + getName() + " -- " + s);
    if (WikiConstants.NULL.equals(s)) {
      oxypy = new OxygenProxy(null, new Class[] {WikiPageReviewProvider.class});
      pageReviewProvider = (WikiPageReviewProvider) oxypy.getProxy();
    } else {
      pageReviewProvider = (WikiPageReviewProvider) Class.forName(s).newInstance();
      pageReviewProvider.prepare(this);
    }

    oxypy =
        new OxygenProxy(pageReviewProvider) {
          protected Object doInvoke(Method m, Object[] args) throws Exception {
            String mName = m.getName();
            int i = 0;
            if (mName.equals("getPageReview")) {
              return setIfNotSetWPAttributes(m.invoke(target, args));
            } else if (mName.equals("savePageReview")) {
              savePageReviewProxy((String) args[i++], (String) args[i++], (Properties) args[i++]);
            } else if (mName.equals("deletePageReview")) {
              deletePageReviewProxy((String) args[i++], (String) args[i++], (Properties) args[i++]);
            } else {
              return m.invoke(target, args);
            }
            return null;
          }
        };
    pageReviewProviderProxy = (WikiPageReviewProvider) oxypy.getProxy();

    s = getProperty(WikiConstants.ATTACHMENT_PROVIDER_KEY);
    if (WikiConstants.NULL.equals(s)) {
      oxypy = new OxygenProxy(null, new Class[] {WikiAttachmentProvider.class});
      attachProvider = (WikiAttachmentProvider) oxypy.getProxy();
    } else {
      attachProvider = (WikiAttachmentProvider) Class.forName(s).newInstance();
      attachProvider.prepare(this);
    }

    oxypy =
        new OxygenProxy(attachProvider) {
          protected Object doInvoke(Method m, Object[] args) throws Exception {
            String mName = m.getName();
            int i = 0;
            if (mName.equals("getAttachment")) {
              return setIfNotSetWPAttributes(m.invoke(target, args));
            } else if (mName.equals("saveAttachment")) {
              saveAttachmentProxy(
                  (String) args[i++], (String) args[i++], (File) args[i++], (Properties) args[i++]);
            } else if (mName.equals("deleteAttachment")) {
              deleteAttachmentProxy((String) args[i++], (String) args[i++], (Properties) args[i++]);
            } else if (mName.equals("deleteAttachmentVersions")) {
              deleteAttachmentVersionsProxy(
                  (String) args[i++],
                  (String) args[i++],
                  (Properties) args[i++],
                  (OxygenIntRange) args[i++]);
            } else {
              return m.invoke(target, args);
            }
            return null;
          }
        };
    attachProviderProxy = (WikiAttachmentProvider) oxypy.getProxy();

    s = getProperty(WikiConstants.RECREATE_INDEX_ON_STARTUP_KEY);
    boolean forceCreateIndex = "true".equals(s);
    indexmgr = new WikiIndexingManager(this);
    indexmgr.resetAll(forceCreateIndex);

    editManager = new WikiEditManager(this);

    s = getProperty(WikiConstants.OPTIMIZATION_SYNCHRONIZE_SAVE_DELETE_ON_INTERNED_STRINGS_KEY);
    syncSaveDelOnInternedStrings = "true".equals(s);
  }

  public SimpleLock getLongTermLock() {
    return longTermLock;
  }

  public MarkupParserFactory getMarkupParserFactory() {
    return markupParserFactory;
  }

  public ShorthandManager getShorthandManager() {
    return shorthandMgr;
  }

  public CensoredWordManager getCensoredWordManager() {
    return censoredMgr;
  }

  public EmoticonsManager getEmoticonsManager() {
    return emoticonsMgr;
  }

  public String getCharacterEncoding() {
    return getProperty(WikiConstants.ENCODING_KEY);
  }

  public String getEntryPage() {
    return getProperty(WikiConstants.ENTRY_PAGE_KEY);
  }

  public MarkupMacro getMacro(String command) {
    return (MarkupMacro) macros.get(command);
  }

  public Locale[] getSupportedUILocales() {
    return (Locale[]) supportedLocales.toArray(new Locale[0]);
  }

  public boolean isSupportedUILocale(Locale l) {
    return supportedLocales.contains(l);
  }

  public Locale getDefaultLocale() {
    return defaultLocale;
  }

  public Object getAttribute(Object s) {
    return attributes.get(s);
  }

  public void setAttribute(Object s, Object o) {
    attributes.put(s, o);
  }

  public void clearAttributes() {
    attributes.clear();
  }

  /** Close this. Called to release all system resources i.e. close all providers, managers. */
  public void close() {
    OxygenUtils.debug("WikiCategoryEngine.close for: " + getName());

    CloseUtils.close(longTermLock);
    CloseUtils.close(pageProviderProxy);
    CloseUtils.close(pageReviewProviderProxy);
    CloseUtils.close(attachProviderProxy);

    renderEngine.close();
    editManager.close();
    templateHdlr.close();
    indexmgr.close();
    censoredMgr.close();
    emoticonsMgr.close();

    for (Iterator itr = listeners.values().iterator(); itr.hasNext(); ) {
      CloseUtils.close((WikiEventListener) itr.next());
    }
    listeners.clear();
    for (Iterator itr = macros.values().iterator(); itr.hasNext(); ) {
      CloseUtils.close((MarkupMacro) itr.next());
    }
    macros.clear();
    props.clear();
    for (Iterator itr = attributes.values().iterator(); itr.hasNext(); ) {
      CloseUtils.close(itr.next());
    }
    attributes.clear();
    defaultLocale = null;
    supportedLocales.clear();

    readyForHandlingRequests = false;
    longTermLock = null;
  }

  public WikiIndexingManager getIndexingManager() {
    return indexmgr;
  }

  public Date getLastUpdateDate() {
    if (lastUpdateDate == null) {
      lastUpdateDate = (Date) engine.getCacheManager().get(LAST_UPDATE_DATE_CACHE_GROUP, getName());
    }
    return lastUpdateDate;
  }

  /**
   * Gets the name of this category engine
   *
   * @return
   */
  public String getName() {
    return name;
  }

  public WikiEngine getWikiEngine() {
    return engine;
  }

  public WikiEditManager getWikiEditManager() {
    return editManager;
  }

  public String getProperty(String key) {
    return getProperty(key, null);
  }

  /**
   * Get a given property, or return the defValue if no property exists for the key. The search
   * order is: - check the properties for this category - check the properties for the whole engine
   *
   * @param key
   * @param defvalue
   * @return
   */
  public String getProperty(String key, String defvalue) {
    // String s = props.getProperty(key);
    // if(s == null) {
    //  s = engine.getProperty(key);
    // }
    // if(s == null) {
    //  s = defvalue;
    // }
    // return s;
    return props.getProperty(key, defvalue);
  }

  /**
   * Gets all properties for this category This is the properties defined in the overall engine and
   * category-specific ones.
   *
   * @return Properties
   */
  public Properties getProperties() {
    // Properties p = new Properties(engine.getProperties());
    // p.putAll(props);
    // return p;
    return props;
  }

  public WikiPageProvider getPageProvider() {
    return pageProviderProxy;
  }

  public WikiPageReviewProvider getPageReviewProvider() {
    return pageReviewProviderProxy;
  }

  public WikiAttachmentProvider getAttachmentProvider() {
    return attachProviderProxy;
  }

  public WikiTemplateHandler getWikiTemplateHandler() {
    return templateHdlr;
  }

  public boolean isActionSupported(String s) {
    return !(actionsNotSupported.contains(s));
  }

  /**
   * If the page exists, write the HTML representation of the page, by delegating to the appropriate
   * processor. If page does not exist, show the appropriate pagenotexits screen
   *
   * @param wp
   * @param req
   * @param resp
   * @param realPageView
   * @throws Exception
   */
  public void writeHTML(WikiProvidedObject wp, boolean realPageView) throws Exception {
    OxygenUtils.debug("In WikiCategoryEngine.writeHTML WikiPage: " + wp.getName());
    Reader r = null;
    try {
      r = pageProvider.getPageReader(wp);
      writeHTML(wp, r, realPageView);
    } finally {
      CloseUtils.close(r);
    }
  }

  /**
   * write the HTML representation for the wiki markup text contained in the passed Reader object.
   * It delegates to the appropriate processor.
   *
   * @param pagerep
   * @param r
   * @param req
   * @param resp
   * @param realPageView
   * @throws Exception
   */
  public void writeHTML(WikiProvidedObject wp, Reader r, boolean realPageView) throws Exception {
    MarkupRenderContext rc = new WikiRenderContext(this, wp, realPageView);
    writeHTML(r, WebLocal.getWebInteractionContext().getWriter(), rc);
  }

  public void writeHTML(Reader r, Writer w, MarkupRenderContext rc) throws Exception {
    BufferedReader br = null;
    if (r instanceof BufferedReader) {
      br = (BufferedReader) r;
    } else {
      br = new BufferedReader(r);
    }
    getRenderEngine().render(w, br, rc, Integer.MAX_VALUE);
  }

  public MarkupRenderEngine getRenderEngine() {
    return renderEngine;
  }

  /**
   * Write the raw text for this wiki page, or do nothing if the wiki page does not exist.
   *
   * @param wp
   * @param req
   * @param resp
   * @throws Exception
   */
  public void writeText(WikiProvidedObject wp) throws Exception {
    // boolean pageExists = pageProvider.pageExists(wp.getName());
    // if(!pageExists) { return; }
    Reader r = null;
    try {
      // Thread.currentThread().dumpStack();
      r = pageProvider.getPageReader(wp);
      writeText(wp.getName(), r);
    } finally {
      CloseUtils.close(r);
    }
  }

  public boolean isPageDraftSupported() {
    return "true".equals(getProperty(WikiConstants.PAGE_DRAFT_SUPPORTED_KEY));
  }

  public void savePageDraft(String pagerep, String author, String text) throws Exception {
    if (isPageDraftSupported()) {
      Map m = new HashMap();
      m.put("page", pagerep);
      m.put("text", text);
      m.put("author", author);
      m.put("date", new Date());
      // only persistent cache manager can be used if page drafts are supported
      OxygenPersistentCacheManager cachemgr =
          (OxygenPersistentCacheManager) WikiLocal.getWikiEngine().getCacheManager();
      cachemgr.put(WikiConstants.CACHE_DRAFT_GROUP_PREFIX + "." + getName(), pagerep, m);
    }
  }

  public void removePageDraft(String pagerep) {
    OxygenCacheManager cachemgr = WikiLocal.getWikiEngine().getCacheManager();
    cachemgr.remove(WikiConstants.CACHE_DRAFT_GROUP_PREFIX + "." + getName(), pagerep);
  }

  public Map getPageDraft(String pagerep) {
    Map m = null;
    if (isPageDraftSupported()) {
      OxygenCacheManager cachemgr = WikiLocal.getWikiEngine().getCacheManager();
      m = (Map) cachemgr.get(WikiConstants.CACHE_DRAFT_GROUP_PREFIX + "." + getName(), pagerep);
    }
    return m;
  }

  public Map getPageDrafts() {
    Map m = null;
    if (isPageDraftSupported()) {
      OxygenCacheManager cachemgr = WikiLocal.getWikiEngine().getCacheManager();
      m = cachemgr.getAll(WikiConstants.CACHE_DRAFT_GROUP_PREFIX + "." + getName(), null);
    }
    return m;
  }

  void ensureReadyForRequestHandling() {
    WebInteractionContext wctx = null;
    if (readyForHandlingRequests || (wctx = WebLocal.getWebInteractionContext()) == null) return;

    // append the action shorthands
    WikiLinkHolder lh = new WikiLinkHolder();
    lh.setWikiPage("{0}");
    lh.setCategory(getName());
    lh.setAttribute(WikiCategoryEngine.class, this);
    String[] actions = engine.getActionManager().getActionKeys();
    for (int i = 0; i < actions.length; i++) {
      // if(!engine.getAction(actions[i]).isFlagSet(WikiWebAction.FLAG_REQUIRES_EXTRAINFO)) {
      WebAction wa = engine.getAction(actions[i]);
      if (wa.isFlagSet(WikiWebAction.FLAG_MAKE_SHORTHAND)
          && !wa.isFlagSet(WikiWebAction.FLAG_REQUIRES_EXTRAINFO)) {
        lh.setAction(actions[i]);
        shorthandMgr.setShorthand("action." + actions[i], lh.getWikiURL());
      }
    }

    readyForHandlingRequests = true;
  }

  private void writeText(String pagerep, Reader r) throws Exception {
    PrintWriter w = WebLocal.getWebInteractionContext().getWriter();
    char[] chars = new char[1024];
    int numread = -1;
    while ((numread = r.read(chars)) != -1) {
      w.write(chars, 0, numread);
    }
    w.flush();
  }

  private void setLastUpdateDate(Date d) {
    lastUpdateDate = d;
    if (lastUpdateDate != null) {
      engine.getCacheManager().put(LAST_UPDATE_DATE_CACHE_GROUP, getName(), lastUpdateDate);
    }
  }

  private void saveAttachmentProxy(String pagerep, String attachment, File f, Properties atts)
      throws Exception {
    Object syncobj = syncobj(pagerep, attachment);
    synchronized (syncobj) {
      atts.setProperty(WikiConstants.ATTRIBUTE_SIZE, String.valueOf(f.length()));
      atts.setProperty(
          WikiConstants.ATTRIBUTE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
      attachProvider.saveAttachment(pagerep, attachment, f, atts);
      WikiEvent we = new WikiEvent(WikiEvent.ATTACHMENT_SAVED);
      we.setAttribute(WikiEvent.PAGE_NAME_KEY, pagerep);
      we.setAttribute(WikiEvent.ATTACHMENT_NAME_KEY, attachment);
      we.setAttribute(WikiEvent.ATTACHMENT_ATTRIBUTES_KEY, atts);
      we.setAttribute(
          WikiEvent.MINOR_EDIT_FLAG_KEY,
          Boolean.valueOf("true".equals(atts.get(WikiEvent.MINOR_EDIT_FLAG_KEY))));
      notifyListeners(we);
    }
  }

  private void deleteAttachmentProxy(String pagerep, String attachment, Properties atts)
      throws Exception {
    Object syncobj = syncobj(pagerep, attachment);
    synchronized (syncobj) {
      atts.setProperty(WikiConstants.ATTRIBUTE_SIZE, "0");
      atts.setProperty(
          WikiConstants.ATTRIBUTE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
      attachProvider.deleteAttachment(pagerep, attachment, atts);
      WikiEvent we = new WikiEvent(WikiEvent.ATTACHMENT_DELETED);
      we.setAttribute(WikiEvent.PAGE_NAME_KEY, pagerep);
      we.setAttribute(WikiEvent.ATTACHMENT_NAME_KEY, attachment);
      we.setAttribute(WikiEvent.MINOR_EDIT_FLAG_KEY, Boolean.FALSE);
      we.setAttribute(WikiEvent.ATTACHMENT_ATTRIBUTES_KEY, atts);
      notifyListeners(we);
    }
  }

  private void deleteAttachmentVersionsProxy(
      String pagerep, String attachment, Properties atts, OxygenIntRange versions)
      throws Exception {
    Object syncobj = syncobj(pagerep, attachment);
    synchronized (syncobj) {
      WikiProvidedObject wp =
          attachProvider.getAttachment(
              pagerep, attachment, WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);
      if (versions.find(attachProvider.getInitialVersion(), true)
          || versions.find(wp.getVersion(), true)) {
        throw new WikiException(
            WebLocal.getI18n().str("engine.cannot_remove_initial_and_latest_versions"));
      }
      atts.setProperty(
          WikiConstants.ATTRIBUTE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
      attachProvider.deleteAttachmentVersions(pagerep, attachment, atts, versions);
      WikiEvent we = new WikiEvent(WikiEvent.ATTACHMENT_VERSIONS_DELETED);
      we.setAttribute(WikiEvent.ATTACHMENT_ATTRIBUTES_KEY, atts);
      we.setAttribute(WikiEvent.PAGE_NAME_KEY, pagerep);
      we.setAttribute(WikiEvent.ATTACHMENT_NAME_KEY, attachment);
      we.setAttribute(WikiEvent.VERSIONS_KEY, versions);
      we.setAttribute(WikiEvent.MINOR_EDIT_FLAG_KEY, Boolean.FALSE);
      notifyListeners(we);
    }
  }

  private void savePageProxy(String pagerep, String text, Properties atts) throws Exception {
    Object syncobj = syncobj(pagerep, null);
    synchronized (syncobj) {
      String origtext = "";
      // System.out.println("Calling savePageProxy");
      if (pageProvider.pageExists(pagerep)) {
        Reader r = pageProvider.getPageReader(new WikiProvidedObject(pagerep));
        origtext = StringUtils.readerToString(r);
      }
      atts.setProperty(
          WikiConstants.ATTRIBUTE_SIZE,
          String.valueOf(text.getBytes(getCharacterEncoding()).length));
      atts.setProperty(
          WikiConstants.ATTRIBUTE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
      pageProvider.savePage(pagerep, text, atts);
      removePageDraft(pagerep);
      // send email to subscribers
      WikiEvent we = new WikiEvent(WikiEvent.PAGE_SAVED);
      we.setAttribute(WikiEvent.PAGE_NAME_KEY, pagerep);
      we.setAttribute(WikiEvent.PAGE_TEXT_ORIGINAL_KEY, origtext);
      we.setAttribute(WikiEvent.PAGE_TEXT_KEY, text);
      we.setAttribute(WikiEvent.PAGE_ATTRIBUTES_KEY, atts);
      we.setAttribute(
          WikiEvent.MINOR_EDIT_FLAG_KEY,
          Boolean.valueOf("true".equals(atts.get(WikiEvent.MINOR_EDIT_FLAG_KEY))));
      notifyListeners(we);
    }
  }

  private void deletePageProxy(String pagerep, Properties atts) throws Exception {
    Object syncobj = syncobj(pagerep, null);
    synchronized (syncobj) {
      atts.setProperty(WikiConstants.ATTRIBUTE_SIZE, "0");
      atts.setProperty(
          WikiConstants.ATTRIBUTE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
      pageProvider.deletePage(pagerep, atts);
      removePageDraft(pagerep);
      WikiEvent we = new WikiEvent(WikiEvent.PAGE_DELETED);
      we.setAttribute(WikiEvent.PAGE_ATTRIBUTES_KEY, atts);
      we.setAttribute(WikiEvent.PAGE_NAME_KEY, pagerep);
      we.setAttribute(WikiEvent.MINOR_EDIT_FLAG_KEY, Boolean.FALSE);
      notifyListeners(we);
    }
  }

  private void deletePageVersionsProxy(String pagerep, Properties atts, OxygenIntRange versions)
      throws Exception {
    Object syncobj = syncobj(pagerep, null);
    synchronized (syncobj) {
      WikiProvidedObject wp =
          pageProvider.getPage(pagerep, WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);
      if (versions.find(pageProvider.getInitialVersion(), true)
          || versions.find(wp.getVersion(), true)) {
        throw new WikiException(
            WebLocal.getI18n().str("engine.cannot_remove_initial_and_latest_versions"));
      }
      atts.setProperty(
          WikiConstants.ATTRIBUTE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
      pageProvider.deletePageVersions(pagerep, atts, versions);
      WikiEvent we = new WikiEvent(WikiEvent.PAGE_VERSIONS_DELETED);
      we.setAttribute(WikiEvent.PAGE_NAME_KEY, pagerep);
      we.setAttribute(WikiEvent.VERSIONS_KEY, versions);
      we.setAttribute(WikiEvent.MINOR_EDIT_FLAG_KEY, Boolean.FALSE);
      notifyListeners(we);
    }
  }

  private void savePageReviewProxy(String pagerep, String text, Properties atts) throws Exception {
    Object syncobj = syncobj(pagerep, null);
    synchronized (syncobj) {
      atts.setProperty(
          WikiConstants.ATTRIBUTE_SIZE,
          String.valueOf(text.getBytes(getCharacterEncoding()).length));
      atts.setProperty(
          WikiConstants.ATTRIBUTE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
      // System.out.println("calling savePageReviewProxy: " + pagerep + " -- " + text + " -- " +
      // atts);
      pageReviewProvider.savePageReview(pagerep, text, atts);
      WikiEvent we = new WikiEvent(WikiEvent.REVIEW_SAVED);
      we.setAttribute(WikiEvent.PAGE_NAME_KEY, pagerep);
      we.setAttribute(WikiEvent.REVIEW_ATTRIBUTES_KEY, atts);
      we.setAttribute(WikiEvent.REVIEW_TEXT_KEY, text);
      we.setAttribute(
          WikiEvent.MINOR_EDIT_FLAG_KEY,
          Boolean.valueOf("true".equals(atts.get(WikiEvent.MINOR_EDIT_FLAG_KEY))));
      notifyListeners(we);
    }
  }

  private void deletePageReviewProxy(String pagerep, String reviewname, Properties atts)
      throws Exception {
    Object syncobj = syncobj(pagerep, null);
    synchronized (syncobj) {
      // System.out.println("calling savePageReviewProxy: " + pagerep + " -- " + text + " -- " +
      // atts);
      atts.setProperty(WikiConstants.ATTRIBUTE_SIZE, "0");
      atts.setProperty(
          WikiConstants.ATTRIBUTE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
      pageReviewProvider.deletePageReview(pagerep, reviewname, atts);
      WikiEvent we = new WikiEvent(WikiEvent.REVIEW_DELETED);
      we.setAttribute(WikiEvent.PAGE_NAME_KEY, pagerep);
      we.setAttribute(WikiEvent.REVIEW_NAME_KEY, reviewname);
      we.setAttribute(WikiEvent.REVIEW_ATTRIBUTES_KEY, atts);
      we.setAttribute(
          WikiEvent.MINOR_EDIT_FLAG_KEY,
          Boolean.valueOf("true".equals(atts.get(WikiEvent.MINOR_EDIT_FLAG_KEY))));
      notifyListeners(we);
    }
  }

  private void notifyListeners(WikiEvent we) {
    for (Iterator itr = listeners.values().iterator(); itr.hasNext(); ) {
      WikiEventListener proc = (WikiEventListener) itr.next();
      try {
        proc.handleWikiEvent(we);
      } catch (Exception exc) {
        OxygenUtils.error(exc);
      }
    }
  }

  private WikiProvidedObject setIfNotSetWPAttributes(Object o) {
    WikiProvidedObject wp = (WikiProvidedObject) o;
    if (wp.getAttribute(WikiConstants.ATTRIBUTE_AUTHOR) == null) {
      wp.setAttribute(WikiConstants.ATTRIBUTE_AUTHOR, getProperty(WikiConstants.DEFAULT_USER_KEY));
    }
    if (wp.getAttribute(WikiConstants.ATTRIBUTE_COMMENTS) == null) {
      wp.setAttribute(WikiConstants.ATTRIBUTE_COMMENTS, "");
    }
    return wp;
  }

  private Object syncobj(String pagerep, String attachment) {
    Object o =
        (syncSaveDelOnInternedStrings
            ? (Object) ((name + "-->" + pagerep).intern())
            : (Object) this);
    return o;
  }

  private class WCEListener implements WikiEventListener {
    public void prepare(WikiCategoryEngine _wce) {}

    public void close() {}

    public void handleWikiEvent(WikiEvent we) {
      try {
        setLastUpdateDate(new Date());
        String pagerep = (String) we.getAttribute(WikiEvent.PAGE_NAME_KEY);
        // always re-index, even if it was just the comment that changed.
        indexmgr.resetWikiPage(pagerep);
        // if(we.getType() == WikiEvent.PAGE_DELETED || PAGE_SAVED ...) {
      } catch (Exception exc) {
        OxygenUtils.error(exc);
      }
    }
  }
}
