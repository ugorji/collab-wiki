/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.ParallelMultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import net.ugorji.oxygen.markup.MarkupLink;
import net.ugorji.oxygen.markup.indexing.HitsHandler;
import net.ugorji.oxygen.markup.indexing.MarkupAnalyzer;
import net.ugorji.oxygen.markup.indexing.MarkupIndexingManager;
import net.ugorji.oxygen.markup.indexing.MarkupIndexingParser;
import net.ugorji.oxygen.markup.indexing.MarkupIndexingParserBase;
import net.ugorji.oxygen.util.ObjectWrapper;
import net.ugorji.oxygen.util.OxygenConstants;
import net.ugorji.oxygen.util.OxygenSearchResults;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.SimpleInt;
import net.ugorji.oxygen.util.StringUtils;

/**
 * Manages our indexes. When a category engine is initialized, it creates an index on all the pages
 * available. This index is used for searches, and for looking up references. At init, create index
 * on save et al, refresh index
 */
public class WikiIndexingManager extends MarkupIndexingManager {
  private static final String[] EMPTY = new String[0];
  private static DateTools.Resolution dateResolution = DateTools.Resolution.DAY;

  // static {
  //  linkPattern = Pattern.compile("(\\[[\\p{Space}]*(.+?)[\\p{Space}]*\\])" + "|" +
  //                                "([\\p{Space}]([\\w\\./]+:[^\\p{Space}]+))" + "|" +
  //                                "([\\p{Space}]([\\w\\./]+?[#|\\^]?[\\w\\./]*))");
  // }

  private WikiCategoryEngine wce;
  private boolean onlyIndexPublishedPages = false;
  private boolean supportFullTextSearch = true;

  // hold a map of page name (String) to all its references (Set)
  // this map thus only knows about pages which exist
  // Map<String, Set>
  private Map refs = new HashMap();
  // holds all the pages contained in this category
  // Set<String>
  private Set allrefs = new TreeSet();

  // let read, and write, lock on the same object.
  // private Object WIM_GET_READER_LOCK = new Object();
  // private Object WIM_WRITE_LOCK = WIM_GET_READER_LOCK;
  // private Object WIM_WRITE_LOCK = new Object();

  public WikiIndexingManager(WikiCategoryEngine _wce) throws Exception {
    wce = _wce;
    onlyIndexPublishedPages =
        "true".equals(wce.getProperty(WikiConstants.ONLY_INDEX_PUBLISHED_PAGES_KEY));
    supportFullTextSearch =
        "true".equals(wce.getProperty(OxygenConstants.FULL_TEXT_SEARCH_SUPPORTED_KEY));
    // analyzer =
    // (Analyzer)Class.forName(wce.getProperty(WikiConstants.INDEXING_ANALYZER_CLASSNAME_KEY)).newInstance();
    analyzer = new MarkupAnalyzer(this);
    analyzerForSearching = new MarkupAnalyzer(this);
  }

  public void close() {
    super.close();
    refs.clear();
    allrefs.clear();
    wce = null;
  }

  /**
   * clears everything, recreates the lucene index'es and fills up the tables which hold the
   * normalized references
   *
   * @throws Exception
   */
  public synchronized void resetAll(boolean forceRecreateIndex) throws Exception {
    WikiCategoryEngine oldwce = WikiLocal.getWikiCategoryEngine();
    try {
      WikiLocal.setWikiCategoryEngine(wce);
      refs.clear();
      allrefs.clear();
      // escapeHTML = "true".equals(wce.getProperty(WikiConstants.ESCAPE_HTML_KEY));
      // freeLinkSupported = "true".equals(wce.getProperty(WikiConstants.FREELINK_SUPPORTED_KEY));
      // camelCaseWordIsLink = "true".equals(wce.getProperty(WikiConstants.CAMEL_CASE_IS_LINK_KEY));
      // slashSeparatedWordIsLink =
      // "true".equals(wce.getProperty(WikiConstants.SLASH_SEPARATED_IS_LINK_KEY));
      recreateIndex(forceRecreateIndex);
      initRefs();
      // System.out.println("wce: " + wce.getName() + " | allrefs: " + allrefs + "\nrefs: " + refs);
    } finally {
      WikiLocal.setWikiCategoryEngine(oldwce);
    }
  }

  /**
   * @param pagerep a wiki page name
   * @return true if a page is referenced, false otherwise
   */
  public boolean isReferenced(String pagerep) {
    return allrefs.contains(pagerep);
  }

  /**
   * @param pagerep a wiki page name
   * @return true if a page is a referer(which is a cheap way of checking if a page exists), false
   *     otherwise
   */
  public boolean isAReferrer(String pagerep) {
    return refs.containsKey(pagerep);
  }

  public WikiCategoryEngine getWikiCategoryEngine() {
    return wce;
  }

  /**
   * This map will take the following keys: All keys are keywords, except - SEARCH_INDEX_CONTENTS
   * and - SEARCH_INDEX_COMMENTS (These should be parsed via Queryparser) SEARCH_INDEX_TAGS: should
   * be tokenized, and OR'ed
   */
  public void search(Map m, boolean allRequired, HitsHandler hhdlr) throws Exception {
    Query query = getSearchQuery(m, allRequired);
    Filter filter = getSearchFilter(m);
    search(query, filter, hhdlr);
  }

  public OxygenSearchResults searchCategories(
      Map m,
      boolean allRequired,
      String[] categories,
      int maxHits,
      double minScore,
      double thresholdScore)
      throws Exception {
    OxygenSearchResults srch = new OxygenSearchResults();
    if (m.size() > 1) {
      WikiEngine we = WikiLocal.getWikiEngine();
      IndexReader[] indexReaders = new IndexReader[categories.length];
      Searchable[] indexSearchers = new Searchable[categories.length];
      WikiIndexingManager[] wim = new WikiIndexingManager[categories.length];
      Searcher multiSearcher = null;
      try {
        for (int i = 0; i < categories.length; i++) {
          wim[i] = we.retrieveWikiCategoryEngine(categories[i]).getIndexingManager();
          indexReaders[i] = wim[i].getIndexReader();
          indexSearchers[i] = wim[i].isearcher0;
        }
        multiSearcher = new ParallelMultiSearcher(indexSearchers);
        Query query = getSearchQuery(m, allRequired);
        Filter filter = getSearchFilter(m);
        Hits hits = multiSearcher.search(query, filter);
        int numhits = hits.length();
        int numResultsAdded = 0;
        // numhits = Math.min(maxHits, numhits);
        for (int i = 0; i < numhits; i++) {
          Document doc = hits.doc(i);
          double score0 = (double) hits.score(i);
          if (score0 >= thresholdScore || (numResultsAdded < maxHits && score0 >= minScore)) {
            srch.addResult(
                doc.get(WikiConstants.SEARCH_INDEX_CATEGORY),
                doc.get(WikiConstants.SEARCH_INDEX_PAGENAME),
                (double) hits.score(i));
            numResultsAdded++;
          }
        }
      } finally {
        for (int i = 0; i < categories.length; i++) {
          // close(indexSearchers[i]);
          if (wim[i] != null) {
            wim[i].returnIndexReader(indexReaders[i]);
          }
        }
        // close(multiSearcher);
      }
    }
    return srch;
  }

  /**
   * Whenever a page is created, updated or deleted, its references get out of sync. This method
   * allows us reset its references, by recreating it in the lucene index, and then reset'ing its
   * references in our tables.
   *
   * @param pagename
   * @throws Exception
   */
  public synchronized void resetWikiPage(String pagename) throws Exception {
    File f = getIndexDir();
    waitTillNoMoreReaders();
    IndexReader ireader = getIndexReader();
    try {
      removeDocuments(pagename, ireader);
      refs.remove(pagename);
      allrefs.remove(pagename);
    } finally {
      returnIndexReader(ireader);
      setWriteDone();
    }

    ireader = null;
    if (wce.getPageProvider().pageExists(pagename)) {
      IndexWriter iwriter = null;
      ireader = getIndexReader();
      try {
        iwriter = new IndexWriter(f, analyzer, false);
        iwriter.setMergeFactor(5);
        createDocuments(pagename, iwriter);
        resetRefForWikiPage(pagename, ireader);
      } finally {
        returnIndexReader(ireader);
        close(iwriter);
        setWriteDone();
      }
    }
  }

  /**
   * Get all referers matching a given regex. This allows us to do things like - get all pages
   * starting with M - get all pages matching M.*abc - get all pages under Main (e.g. Main/a,
   * Main/b, etc) If the regex passed is null, we return all the pages
   *
   * @param regex
   * @return
   * @throws Exception
   */
  public String[] getAllReferersMatching(String regex) throws Exception {
    if (regex == null || regex.trim().length() == 0) {
      return (String[]) allrefs.toArray(new String[0]);
    }
    Pattern pattern = Pattern.compile(regex);
    List list = new ArrayList();
    for (Iterator itr = allrefs.iterator(); itr.hasNext(); ) {
      String s = (String) itr.next();
      if (pattern.matcher(s).matches()) {
        list.add(s);
      }
    }
    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Get all pages that reference a given page. This allows us do things like: - get pages that
   * reference this non-existent page
   *
   * @param pagename
   * @return
   */
  public String[] getPagesThatReference(String pagename) {
    Set set = new HashSet();
    for (Iterator itr = allrefs.iterator(); itr.hasNext(); ) {
      String s = (String) itr.next();
      Set set1 = (Set) refs.get(s);
      if (set1.contains(pagename)) {
        set.add(s);
      }
    }
    String[] arr = (String[]) set.toArray(new String[0]);
    return arr;
  }

  /**
   * Get all pages which are referenced by a given page E.g. get all pages that Main has links to.
   *
   * @param pagename
   * @return
   */
  public String[] getPagesReferencedBy(String pagename) {
    String[] arr = EMPTY;
    Set set = (Set) refs.get(pagename);
    if (set != null) {
      arr = (String[]) set.toArray(new String[0]);
    }
    return arr;
  }

  /** Get all pages which exist, but are not references at all (orphaned pages) */
  public String[] getNonReferencedPages() {
    Set set = new HashSet();
    List list = Arrays.asList(getAllReferences());
    for (Iterator itr = allrefs.iterator(); itr.hasNext(); ) {
      String s = (String) itr.next();
      if (!list.contains(s)) {
        set.add(s);
      }
    }
    String[] arr = (String[]) set.toArray(new String[0]);
    return arr;
  }

  /**
   * Get all non-existent pages (they are referenced, but do not exist)
   *
   * @return
   */
  public String[] getNonExistentPages() {
    Set set = new HashSet();
    List list = Arrays.asList(getAllReferences());
    for (Iterator itr = list.iterator(); itr.hasNext(); ) {
      String s = (String) itr.next();
      if (!allrefs.contains(s)) {
        set.add(s);
      }
    }
    String[] arr = (String[]) set.toArray(new String[0]);
    return arr;
  }

  /**
   * Get all the pages which are referenced within this category So if Main references a, b, c a
   * references b, c, d Then we return a, b, c, d
   *
   * @return
   */
  public String[] getAllReferences() {
    Set set = new HashSet();
    for (Iterator itr = allrefs.iterator(); itr.hasNext(); ) {
      Set set2 = (Set) refs.get(itr.next());
      set.addAll(set2);
    }
    String[] arr = (String[]) set.toArray(new String[0]);
    return arr;
  }

  public String[] lookupAttachmentNames(String pagename, Date from, Date to) throws Exception {
    Map m = new HashMap();
    m.put(WikiConstants.SEARCH_INDEX_PAGENAME, pagename);
    m.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME);
    m.put(WikiConstants.SEARCH_INDEX_LAST_MODIFIED, getDateRangeQueryString(from, to));
    // System.out.println("lookupAttachmentNames: " + from + " ... " + to + " ... " +
    // m.get(WikiConstants.SEARCH_INDEX_LAST_MODIFIED) + "...");

    return getMatchingStringsForLookup(m, WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME);
  }

  public String[] lookupPageReviewNames(String pagename, Date from, Date to) throws Exception {
    Map m = new HashMap();
    m.put(WikiConstants.SEARCH_INDEX_PAGENAME, pagename);
    m.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_COMMENT_NAME);
    m.put(WikiConstants.SEARCH_INDEX_LAST_MODIFIED, getDateRangeQueryString(from, to));
    return getMatchingStringsForLookup(m, WikiConstants.SEARCH_INDEX_COMMENT_NAME);
  }

  public String[] lookupPageNames(Date from, Date to) throws Exception {
    Map m = new HashMap();
    m.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_PAGE);
    m.put(WikiConstants.SEARCH_INDEX_LAST_MODIFIED, getDateRangeQueryString(from, to));
    String[] sa = getMatchingStringsForLookup(m, WikiConstants.SEARCH_INDEX_PAGENAME);
    // Thread.currentThread().dumpStack();
    // System.out.println("lookupPageNames: [" + from + "] TO [" + to + "] " + Arrays.asList(sa));
    return sa;
  }

  public String[] lookupExistingTags() throws Exception {
    Map m = new HashMap();
    m.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_PAGE);
    return getMatchingStringsForLookup(m, WikiConstants.SEARCH_INDEX_TAGS);
  }

  public Map lookupExistingTagsWithCount() throws Exception {
    Map m = new HashMap(); // so that the keys are sorted
    m.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_PAGE);
    Map hmap = new TreeMap();
    getMatchesForLookup(m, WikiConstants.SEARCH_INDEX_TAGS, hmap, null);
    // System.out.println("lookupExistingTagsWithCount: " + wce.getName() + ": " + hmap);
    return hmap;
  }

  public String[] lookupPageNamesGivenTag(String tag) throws Exception {
    Map m = new HashMap();
    m.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_PAGE);
    m.put(WikiConstants.SEARCH_INDEX_TAGS, tag.toLowerCase());
    return getMatchingStringsForLookup(m, WikiConstants.SEARCH_INDEX_PAGENAME);
  }

  public boolean isOnlyIndexPublishedPages() {
    return onlyIndexPublishedPages;
  }

  public WikiProvidedObject getWikiPageFromIndex(String pagename) throws Exception {
    WikiProvidedObject wp = new WikiProvidedObject(pagename);
    final ObjectWrapper ow = new ObjectWrapper(null);
    HitsHandler myhithdlr =
        new HitsHandler() {
          public void handleHits(Hits hits) throws Exception {
            ow.setObject(hits);
          }
        };
    Map m = new HashMap();
    m.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_PAGE);
    m.put(WikiConstants.SEARCH_INDEX_PAGENAME, pagename);
    search(m, true, myhithdlr);

    Hits hits = (Hits) ow.obj();
    if (hits.length() > 0) {
      Document doc = hits.doc(0);
      Field field = null;
      if ((field = doc.getField(WikiConstants.SEARCH_INDEX_VERSION)) != null) {
        wp.setVersion(Integer.parseInt(field.stringValue()));
      }
      if ((field = doc.getField(WikiConstants.SEARCH_INDEX_LAST_MODIFIED)) != null) {
        wp.setDate(DateTools.stringToDate(field.stringValue()));
      }
      if ((field = doc.getField(WikiConstants.SEARCH_INDEX_LAST_EDITOR)) != null) {
        wp.setAttribute(WikiConstants.ATTRIBUTE_AUTHOR, field.stringValue());
      }
      if ((field = doc.getField(WikiConstants.SEARCH_INDEX_PAGE_EDITOR_COMMENT)) != null) {
        wp.setAttribute(WikiConstants.ATTRIBUTE_COMMENTS, field.stringValue());
      }
      Field[] fields = doc.getFields(WikiConstants.SEARCH_INDEX_TAGS);
      if (fields != null && fields.length > 0) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < fields.length; i++) {
          buf.append(fields[i].stringValue()).append(" ");
        }
        wp.setAttribute(WikiConstants.ATTRIBUTE_TAGS, buf.toString().trim());
      }
    }
    return wp;
  }

  protected File getIndexDir() throws Exception {
    File f = new File(wce.getWikiEngine().getRuntimeDirectory(), "searchindex");
    f = new File(f, wce.getName());
    OxygenUtils.mkdirs(f);
    return f;
  }

  protected MarkupIndexingParser newParser(String name, Reader r, List _tokenstrings, HashSet _hs)
      throws Exception {
    WIMMarkupParserBase x = new WIMMarkupParserBase(name, r, _tokenstrings, _hs);
    MarkupIndexingParser mx = new MarkupIndexingParser(x.getMarkupParser(), x);
    return mx;
  }

  private synchronized void recreateIndex(boolean forceRecreateIndex) throws Exception {
    String[] sarr = wce.getPageProvider().getPageNames("/", Integer.MAX_VALUE, false);
    // System.out.println("In WikiIndexingManager<init>: wps: " + Arrays.asList(sarr));
    OxygenUtils.debug("In WikiIndexingManager<init>: wps: " + Arrays.asList(sarr));

    // initialize index
    File f = getIndexDir();
    if (forceRecreateIndex) {
      // Thread.dumpStack();
      // System.out.println("***** Calling deleteFile: " + f.getAbsolutePath());
      OxygenUtils.deleteFile(f);
    }
    // if directory does not exist or it is empty, recreate it and index
    // if(!f.exists() || f.isFile() || f.listFiles().length == 0) {
    if (!IndexReader.indexExists(f)) {
      waitTillNoMoreReaders();
      OxygenUtils.deleteFile(f);
      OxygenUtils.mkdirs(f);
      IndexWriter iwriter = null;
      try {
        iwriter = new IndexWriter(f, analyzer, true);
        iwriter.setMergeFactor(50);
        for (int i = 0; i < sarr.length; i++) {
          // System.out.println("Creating Index document for: " + wce.getName() + ":" + sarr[i]);
          createDocuments(sarr[i], iwriter);
        }
        iwriter.optimize();
      } finally {
        close(iwriter);
        setWriteDone();
      }
    }
  }

  private void initRefs() throws Exception {
    // File f = getIndexDir();
    // initialize the refs maps
    List allpagesinindex = new ArrayList();
    IndexReader ireader = getIndexReader();
    try {
      int numdocs = ireader.numDocs();
      // doc.add(new Field(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_PAGE,
      // Field.Store.YES, Field.Index.UN_TOKENIZED));
      for (int i = 0; i < numdocs; i++) {
        Document doc = ireader.document(i);
        Field field = doc.getField(WikiConstants.SEARCH_INDEX_INDEX_TYPE);
        // System.out.println("indextype: " + field);
        if (WikiConstants.SEARCH_INDEX_PAGE.equals(field.stringValue())) {
          field = doc.getField(WikiConstants.SEARCH_INDEX_PAGENAME);
          String pagename = field.stringValue();
          allpagesinindex.add(pagename);
        }
      }

      for (Iterator itr = allpagesinindex.iterator(); itr.hasNext(); ) {
        resetRefForWikiPage((String) itr.next(), ireader);
      }

    } finally {
      returnIndexReader(ireader);
      // close(ireader);
    }
  }

  private void search(Query query, Filter filter, HitsHandler hhdlr) throws Exception {
    File f = getIndexDir();
    IndexReader ireader = getIndexReader();
    try {
      Hits hits = isearcher0.search(query, filter);
      hhdlr.handleHits(hits);
    } finally {
      returnIndexReader(ireader);
    }
  }

  private Query getSearchQuery(Map m, boolean allRequired) throws Exception {
    BooleanClause.Occur occur =
        (allRequired) ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;
    BooleanQuery query = new BooleanQuery();
    for (Iterator itr = m.keySet().iterator(); itr.hasNext(); ) {
      String key = (String) itr.next();
      String val = (String) m.get(key);
      if (!StringUtils.isBlank(val)) {
        if (WikiConstants.SEARCH_INDEX_TAGS.equals(key)) {
          BooleanQuery q2 = new BooleanQuery();
          StringTokenizer stz = new StringTokenizer(val.trim().toLowerCase(), ", ");
          while (stz.hasMoreTokens()) {
            q2.add(
                new BooleanClause(
                    new TermQuery(new Term(key, stz.nextToken())), BooleanClause.Occur.SHOULD));
          }
          query.add(new BooleanClause(q2, occur));
        } else if (WikiConstants.SEARCH_INDEX_CONTENTS.equals(key)
            || WikiConstants.SEARCH_INDEX_COMMENTS.equals(key)) {
          query.add(
              new BooleanClause(new QueryParser(key, analyzerForSearching).parse(val), occur));
        } else if (WikiConstants.SEARCH_INDEX_LAST_MODIFIED.equals(key)) {
          query.add(
              new BooleanClause(new QueryParser(key, analyzerForSearching).parse(val), occur));
        } else {
          query.add(new BooleanClause(new TermQuery(new Term(key, val)), occur));
        }
      }
    }
    return query;
  }

  private Filter getSearchFilter(Map m) throws Exception {
    return null;
  }

  private void removeDocuments(String pagename, IndexReader ireader) throws Exception {
    Term term = new Term(WikiConstants.SEARCH_INDEX_PAGENAME, pagename);
    ireader.deleteDocuments(term);
  }

  private void createDocuments(String pagename, IndexWriter iwriter) throws Exception {
    try {
      analyzer.setTextSourceName(WikiUtils.fullQualifiedWikiName(wce.getName(), pagename));
      Document doc = new Document();
      int version = WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY;
      String s = null;
      // System.out.println("default version: " + version);

      WikiProvidedObject wp = wce.getPageProvider().getPage(pagename, version);

      if (onlyIndexPublishedPages
          && !("true".equals(wp.getAttribute(WikiConstants.ATTRIBUTE_PUBLISHED)))) {
        return;
      }

      if (version == WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY) {
        WikiProvidedObject wp22 =
            wce.getPageProvider().getPage(pagename, wce.getPageProvider().getInitialVersion());
        if ((s = wp22.getAttribute(WikiConstants.ATTRIBUTE_AUTHOR)) != null) {
          doc.add(
              new Field(
                  WikiConstants.SEARCH_INDEX_AUTHOR, s, Field.Store.YES, Field.Index.UN_TOKENIZED));
        }
        if ((s = wp22.getAttribute(WikiConstants.ATTRIBUTE_COMMENTS)) != null) {
          doc.add(
              new Field(
                  WikiConstants.SEARCH_INDEX_PAGE_EDITOR_COMMENT,
                  s,
                  Field.Store.YES,
                  Field.Index.UN_TOKENIZED));
        }
      }

      doc.add(
          new Field(
              WikiConstants.SEARCH_INDEX_CATEGORY,
              wce.getName(),
              Field.Store.YES,
              Field.Index.UN_TOKENIZED));
      doc.add(
          new Field(
              WikiConstants.SEARCH_INDEX_PAGENAME,
              wp.getName(),
              Field.Store.YES,
              Field.Index.UN_TOKENIZED));
      doc.add(
          new Field(
              WikiConstants.SEARCH_INDEX_VERSION,
              String.valueOf(wp.getVersion()),
              Field.Store.YES,
              Field.Index.UN_TOKENIZED));
      doc.add(
          new Field(
              WikiConstants.SEARCH_INDEX_LAST_MODIFIED,
              DateTools.dateToString(wp.getDate(), dateResolution),
              Field.Store.YES,
              Field.Index.UN_TOKENIZED));
      if (wp.getAttribute(WikiConstants.ATTRIBUTE_AUTHOR) != null) {
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_LAST_EDITOR,
                wp.getAttribute(WikiConstants.ATTRIBUTE_AUTHOR),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
      }
      if (wp.getAttribute(WikiConstants.ATTRIBUTE_TAGS) != null) {
        StringTokenizer stz =
            new StringTokenizer(wp.getAttribute(WikiConstants.ATTRIBUTE_TAGS), ", ");
        while (stz.hasMoreTokens()) {
          doc.add(
              new Field(
                  WikiConstants.SEARCH_INDEX_TAGS,
                  stz.nextToken().trim().toLowerCase(),
                  Field.Store.YES,
                  Field.Index.UN_TOKENIZED));
        }
      }

      int oldrealver = wp.getVersion();
      wp.setVersion(version);
      Reader r = wce.getPageProvider().getPageReader(wp);
      wp.setVersion(oldrealver);

      String pagetext = StringUtils.readerToString(r);
      if (supportFullTextSearch) {
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_CONTENTS,
                pagetext,
                Field.Store.YES,
                Field.Index.TOKENIZED));
      }

      // now add the references to the index
      // note: errors found while trying to index individual pages, should not stop the document
      // from being created
      HashSet myrefs = new HashSet();
      try {
        MarkupIndexingParser wimap =
            newParser(pagename, new BufferedReader(new StringReader(pagetext)), null, myrefs);
        wimap.markupToHTML();
      } catch (Exception exc) {
        OxygenUtils.error(
            "Exception getting references while parsing wiki page during indexing: "
                + wce.getName()
                + ":"
                + pagename,
            exc);
      }

      // System.out.println("analyzer.references: pagename: " + pagename + " --- " + myrefs);
      for (Iterator itr = myrefs.iterator(); itr.hasNext(); ) {
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_REFERENCES,
                (String) itr.next(),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
      }

      String[] sarr = wce.getAttachmentProvider().getAttachmentNames(wp.getName(), false);
      WikiProvidedObject[] attachments = new WikiProvidedObject[sarr.length];
      for (int i = 0; i < attachments.length; i++) {
        attachments[i] = wce.getAttachmentProvider().getAttachment(pagename, sarr[i], version);
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME,
                attachments[i].getName(),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
      }
      sarr = wce.getPageReviewProvider().getPageReviewNames(wp.getName(), false);
      WikiProvidedObject[] comments = new WikiProvidedObject[sarr.length];
      for (int i = 0; i < comments.length; i++) {
        comments[i] = wce.getPageReviewProvider().getPageReview(pagename, sarr[i], version);
        if (supportFullTextSearch) {
          r = wce.getPageReviewProvider().getPageReviewReader(pagename, comments[i]);
          doc.add(
              new Field(
                  WikiConstants.SEARCH_INDEX_COMMENTS,
                  StringUtils.readerToString(r),
                  Field.Store.YES,
                  Field.Index.TOKENIZED));
        }
      }

      doc.add(
          new Field(
              WikiConstants.SEARCH_INDEX_INDEX_TYPE,
              WikiConstants.SEARCH_INDEX_PAGE,
              Field.Store.YES,
              Field.Index.UN_TOKENIZED));

      iwriter.addDocument(doc);
      // System.out.println("analyzer.tokenstringlist: pagename: " + pagename + " --- " +
      // analyzer.tokenstringlist);

      for (int i = 0; i < comments.length; i++) {
        doc = new Document();
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_CATEGORY,
                wce.getName(),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_INDEX_TYPE,
                WikiConstants.SEARCH_INDEX_COMMENT_NAME,
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_COMMENT_NAME,
                comments[i].getName(),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_PAGENAME,
                pagename,
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_LAST_MODIFIED,
                DateTools.dateToString(comments[i].getDate(), dateResolution),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        iwriter.addDocument(doc);
      }

      for (int i = 0; i < attachments.length; i++) {
        doc = new Document();
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_CATEGORY,
                wce.getName(),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_INDEX_TYPE,
                WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME,
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME,
                attachments[i].getName(),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_PAGENAME,
                pagename,
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_VERSION,
                String.valueOf(attachments[i].getVersion()),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(
            new Field(
                WikiConstants.SEARCH_INDEX_LAST_MODIFIED,
                DateTools.dateToString(attachments[i].getDate(), dateResolution),
                Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        iwriter.addDocument(doc);
      }

    } finally {
      analyzer.setTextSourceName(null);
    }
  }

  private void resetRefForWikiPage(String pagename, IndexReader ireader) throws Exception {
    HashSet hs = new HashSet();
    try {
      Map m = new HashMap();
      m.put(WikiConstants.SEARCH_INDEX_PAGENAME, pagename);
      m.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_PAGE);
      String[] refs = getMatchingStringsForLookup(m, WikiConstants.SEARCH_INDEX_REFERENCES);
      hs.addAll(Arrays.asList(refs));

    } catch (Exception exc) {
      // exc.printStackTrace();
      OxygenUtils.info("Error resetRefForWikiPage: " + pagename);
      OxygenUtils.error(exc);
    } finally {
      refs.put(pagename, hs);
      allrefs.add(pagename);
    }
  }

  private String[] getMatchingStringsForLookup(final Map m, final String fieldname)
      throws Exception {
    final HashSet list = new HashSet();
    getMatchesForLookup(m, fieldname, null, list);
    String[] sarr = (String[]) list.toArray(new String[0]);
    Arrays.sort(sarr);
    return sarr;
  }

  private void getMatchesForLookup(
      final Map m, final String fieldname, final Map hmap, final Set hset) throws Exception {
    HitsHandler myhithdlr =
        new HitsHandler() {
          public void handleHits(Hits hits) throws Exception {
            int numhits = hits.length();
            for (int i = 0; i < numhits; i++) {
              Document doc = hits.doc(i);
              Field[] fields = doc.getFields(fieldname);
              if (fields != null && fields.length > 0) {
                for (int j = 0; j < fields.length; j++) {
                  String s = fields[j].stringValue();
                  if (hset != null) {
                    hset.add(s);
                  }
                  if (hmap != null) {
                    SimpleInt si = (SimpleInt) hmap.get(s);
                    if (si == null) {
                      si = new SimpleInt();
                      hmap.put(s, si);
                    }
                    si.increment();
                  }
                }
              }
            }
          }
        };
    search(m, true, myhithdlr);
  }

  public static String getDateRangeQueryString(Date from, Date to) throws Exception {
    // do nothing if both from and to are null (ie the dates are not included in the query)
    if (from == null && to == null) {
      return null;
    }

    StringBuffer buf = new StringBuffer();
    String fromint = "0";
    String toint = String.valueOf(Integer.MAX_VALUE);

    if (from == null) {
      from = new Date(0);
    }
    if (to == null) {
      to = new Date();
    }

    // use minute resolution, else sometimes, U'd get the TooManyClausesException
    buf.append("[")
        .append(DateTools.dateToString(from, dateResolution))
        .append(" TO ")
        .append(DateTools.dateToString(to, dateResolution))
        .append("]");
    return buf.toString();
  }

  private class WIMMarkupParserBase extends MarkupIndexingParserBase {
    private WikiParser2Base wp2 = null;

    public WIMMarkupParserBase(String name, Reader r, List _tokenstrings, HashSet _hs)
        throws Exception {
      super(wce.getMarkupParserFactory().newMarkupParser(r), _tokenstrings, _hs);
      setRenderContext(new WikiRenderContext(wce, new WikiProvidedObject(name), false));
      setRenderEngine(wce.getRenderEngine());
      setTextSourceName(name);

      wp2 = new WikiParser2Base();
      wp2.setRenderContext(getRenderContext());
      wp2.setRenderEngine(getRenderEngine());
    }

    public void link(MarkupLink mlink, boolean doPrint) throws Exception {
      super.link(mlink, doPrint);
      if (hs != null) {
        WikiLinkHolder wlh = wp2.do_link(mlink, false);
        _addlh(wlh);
      }
    }

    public void word(String s, boolean isSlashSeperated, boolean doPrint) throws Exception {
      super.word(s, isSlashSeperated, doPrint);
      if (hs != null) {
        WikiLinkHolder wlh = wp2.do_word(s, isSlashSeperated, false);
        _addlh(wlh);
      }
    }

    private void _addlh(WikiLinkHolder lh) throws Exception {
      // if(lh != null) System.out.println("calling _addlh: " + lh.getCategory() + " | " +
      // lh.isExtLink() + " | " + lh.getCategory() + " | " + lh.getWikiPage());
      if (hs != null
          && lh != null
          && !lh.isExtLink()
          && lh.getCategory().equals(wce.getName())
          && lh.getWikiPage() != null) {
        hs.add(lh.getWikiPage());
        // System.out.println("calling _addlh: added: " + lh.getWikiPage());
      }
    }
  }
}

  /*
  private class WIMMarkupParser extends MarkupIndexingParser {
    private WikiParser2 wp2;

    public WIMMarkupParser(String name, Reader r, List _tokenstrings, HashSet _hs) throws Exception {
      super(new WikiRenderContext(wce, new WikiProvidedObject(name), false),
            wce.getRenderEngine(), name, r, _tokenstrings, _hs);
      wp2 = new WikiParser2(r, new PrintWriter(new NullWriter()), rc, re);
    }

    protected void link(MarkupLink mlink, boolean doPrint) throws Exception {
      super.link(mlink, doPrint);
      if(hs != null) {
        WikiLinkHolder wlh = wp2.do_link(mlink, false);
        _addlh(wlh);
      }
    }

    protected void word(String s, boolean isSlashSeperated, boolean doPrint) throws Exception {
      super.word(s, isSlashSeperated, doPrint);
      if(hs != null) {
        WikiLinkHolder wlh = wp2.do_word(s, isSlashSeperated, false);
        _addlh(wlh);
      }
    }

    private void _addlh(WikiLinkHolder lh) throws Exception {
      //if(lh != null) System.out.println("calling _addlh: " + lh.getCategory() + " | " + lh.isExtLink() + " | " + lh.getCategory() + " | " + lh.getWikiPage());
      if(hs != null &&
         lh != null &&
         !lh.isExtLink() &&
         lh.getCategory().equals(wce.getName()) &&
         lh.getWikiPage() != null) {
        hs.add(lh.getWikiPage());
        //System.out.println("calling _addlh: added: " + lh.getWikiPage());
      }
    }

  }

  */

/*
   private void _resetRefForWikiPage_via_regex(String pagename, String pagetext, HashSet hs) throws Exception {
     WikiUtils.debug("In WikiIndexingManager.resetRefForWikiPage: Pagename: " + pagename);
     Matcher m = linkPattern.matcher(pagetext);
   WikiUtils.info("resetting ref for category: " + wce.getName() + " Page: " + pagename);
     while(m.find()) {
     WikiUtils.info("=== found string: " + m.group(0));
       String s1 = m.group(2);
       String s2 = m.group(4);
       String s3 = m.group(6);
       if(s1 != null && s1.length() > 0) {
         WikiLinkHolder lh = new WikiLinkHolder(wce, pagename, s1, true);
         if(!lh.isExtLink() &&
            lh.getCategory().equals(wce.getName()) &&
            lh.getWikiPage() != null) {
           hs.add(lh.getWikiPage());
         }
       } else if(s2 != null && s2.length() > 0) {
         WikiLinkHolder lh = new WikiLinkHolder(wce, pagename, s2, false);
         if(!lh.isExtLink() &&
            lh.getCategory().equals(wce.getName()) &&
            lh.getWikiPage() != null) {
           hs.add(lh.getWikiPage());
         }
       } else if(s3 != null && s3.length() > 0) {
         if(allrefs.contains(s3)) {
           hs.add(s3);
         }
       }
     }
   }

*/

/**
 * performs a search (delegating to lucene)
 *
 * @param searchkey string to search on, that lucene understands
 * @param indexkey the index to search on
 * @return the search results (non-null)
 * @throws Exception
 */
/*
private void search(String searchkey, HitsHandler hhdlr) throws Exception {
  Query query = new QueryParser(WikiConstants.SEARCH_INDEX_CONTENTS, analyzer).parse(searchkey);
  search(query, hhdlr);
}
*/

/*
  private Filter getSearchFilter(Map m) throws Exception {
    //List filters = new ArrayList(2);
    Filter f = null;
    //FOR NOW, handle dates within the getSearchQuery method
    //if U want this handled here, do nothing within the corresponding stuff under getSearchQuery
    //String val = (String)m.get(WikiConstants.SEARCH_INDEX_LAST_MODIFIED);
    //if(!StringUtils.isBlank(val)) {
    //  int len = val.length();
    //  int i0 = val.indexOf(" TO ");
    //  //System.out.println(val + " ... " + val.substring(1, i0) + " ... " + val.substring(i0+4, len-1));
    //  f = new RangeFilter(WikiConstants.SEARCH_INDEX_LAST_MODIFIED,
    //                      val.substring(1, i0), val.substring(i0+4, len-1),
    //                      true, true);
    //}

    //handle all others with SEARCH_INDEX_ANY_HIT_IDENTIFIER
    // (especially so that PageIndex works, if too many attachments)
    // actually - comment out, 'cos if ANY, then don't include at all in search query or filter
    //for(Iterator itr = m.keySet().iterator(); itr.hasNext(); ) {
    //  String key = (String)itr.next();
    //  String val = (String)m.get(key);
    //  if(!StringUtils.isBlank(val)) {
    //    if(WikiConstants.SEARCH_INDEX_TAGS.equals(key) ||
    //       WikiConstants.SEARCH_INDEX_CONTENTS.equals(key) || WikiConstants.SEARCH_INDEX_COMMENTS.equals(key) ||
    //       WikiConstants.SEARCH_INDEX_LAST_MODIFIED.equals(key)) {
    //      continue;
    //    }
    //    if(WikiConstants.SEARCH_INDEX_ANY_HIT_IDENTIFIER.equals(val)) {
    //      //handle in filter ... actually, if ANY, then don't include it in the search query
    //      f = new RangeFilter(key, "0", null, false, false);
    //      filters.add(f);
    //    }
    //  }
    //}

    //now, figure out the filter - disable all for now, since we don't use filters at all
    //Filter[] filterArr = (Filter[])filters.toArray(new Filter[0]);
    //if(filterArr.length > 1) {
    //  //f = new ChainedFilter(filterArr, ChainedFilter.AND);
    //  throw new UnsupportedOperationException("Multiple Filters cannot be used at a time");
    //} else if(filterArr.length == 1) {
    //  f = filterArr[0];
    //} else {
    //  f = null;
    //}
    return f;
  }

*/
