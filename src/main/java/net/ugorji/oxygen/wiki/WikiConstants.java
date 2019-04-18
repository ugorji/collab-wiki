/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import net.ugorji.oxygen.markup.MarkupConstants;

public interface WikiConstants extends MarkupConstants {
  //
  int PUSHBACK_READER_BUFFER_SIZE = 32;

  String BUILTIN_SECTION_NAME = "builtin";
  String FULLBLOWN_PROCESSOR_NAME = "fullblown";
  String DEFAULT_USER_KEY = "net.ugorji.oxygen.wiki.default.user";

  String CALENDAR_TITLE_SOURCE_KEY = "net.ugorji.oxygen.wiki.calendar.title.source";

  String ENGINE_NAME_KEY = "net.ugorji.oxygen.wiki.engine.name";
  String NAME_KEY = "net.ugorji.oxygen.wiki.name";
  String DESCRIPTION_KEY = "net.ugorji.oxygen.wiki.description";

  String WIKII18N_KEY = "net.ugorji.oxygen.wiki.wikii18n";
  String ENCODING_KEY = "net.ugorji.oxygen.wiki.encoding";
  String LOCALE_KEY = "net.ugorji.oxygen.wiki.locale";
  String NOT_SUPPORTED_ACTIONS_KEY = "net.ugorji.oxygen.wiki.actions_not_supported";

  String SUPPORTED_LOCALES_KEY = "net.ugorji.oxygen.wiki.locales.supported";
  // key to find listing of categories in oxywiki.properties file
  String ENGINE_CATEGORIES_KEY = "net.ugorji.oxygen.wiki.categories";
  // key to find WikiPage from the request attributes
  String PAGE_KEY = "net.ugorji.oxygen.wiki.page";
  // key to find WikiEngine from the Servlet Context (or request attribute)
  String ENGINE_KEY = "net.ugorji.oxygen.wiki.engine";

  String ACTION_KEY_PREFIX = "net.ugorji.oxygen.wiki.action.";
  String MACRO_KEY_PREFIX = "net.ugorji.oxygen.wiki.macro.";
  // String EXTERNAL_WIKI_KEY_PREFIX = "net.ugorji.oxygen.wiki.extwiki.";
  String PROCESSOR_KEY_PREFIX = "net.ugorji.oxygen.wiki.processor.";
  String LISTENER_KEY_PREFIX = "net.ugorji.oxygen.wiki.listener.";
  // prefix for property files
  String ENGINE_PROPS_FILE_PREFIX = "oxywiki";

  String ENGINE_PERSISTENCE_DIR_KEY = "net.ugorji.oxygen.wiki.persistence.dir";
  String ENGINE_CACHE_MANAGER_KEY = "net.ugorji.oxygen.wiki.cache.manager";
  String ENGINE_RUNTIME_PERSISTENCE_MANAGER_KEY = "net.ugorji.oxygen.wiki.runtime.persistence.manager";

  String ENGINE_RUNTIME_CONFIG_FILE = "runtime-config-oxywiki.properties";

  String SHOW_DETAILS_BY_DEFAULT_KEY = "net.ugorji.oxygen.wiki.show.details";

  String SERVLET_MAPPING_PREFIX_KEY = "net.ugorji.oxygen.wiki.servlet.prefix";

  String REQUEST_PARAM_VER_KEY = "ver";
  String REQUEST_PARAM_ACTION_KEY = "action";
  String REQUEST_PARAM_PAGE_KEY = "page";
  String REQUEST_PARAM_CATEGORY_KEY = "category";
  String REQUEST_PARAM_EXTRAINFO_KEY = "extrainfo";
  String REQUEST_PARAM_ATTRIBUTE_PREFIX = "attribute.";

  // request attribute set to a Boolean, if we should be showing the review comments
  // String SERVLET_REQ_ATTR_INSERTION = "net.ugorji.oxygen.wiki.insertion";
  // String SERVLET_REQ_SHOWINLINEREVIEWS_KEY = "showinlinereviews";
  // String SERVLET_REQ_SHOWBORDERS_KEY = "showborders";

  // String SERVLET_JSPVIEWS_DIR_PREFIX = "/WEB-INF/oxywiki/jspviews";

  String ENGINE_TIMEBASED_INTERVAL_KEY = "net.ugorji.oxygen.wiki.timebasedinterval";

  String RENDER_ENGINE_KEY = "net.ugorji.oxygen.wiki.renderengine";

  // key for the page provider

  String PAGE_PROVIDER_KEY = "net.ugorji.oxygen.wiki.page.provider";
  String PAGE_REVIEW_PROVIDER_KEY = "net.ugorji.oxygen.wiki.page.review.provider";

  // key for the attachment provider
  String ATTACHMENT_PROVIDER_KEY = "net.ugorji.oxygen.wiki.attachment.provider";

  // key for the temporary directory where files are uploaded to.
  String ATTACHMENT_TMPDIR_KEY = "net.ugorji.oxygen.wiki.attachment.tmpdir";

  // key for the maximum upload size
  String MAX_ATTACHMENT_UPLOAD_SIZE_KEY = "net.ugorji.oxygen.wiki.maxuploadsize";

  // max trail size
  String MAX_TRAIL_SIZE_KEY = "net.ugorji.oxygen.wiki.trail.size.max";

  // key for the built-in jsp template
  String TEMPLATE_BASEFILE_KEY = "net.ugorji.oxygen.wiki.template.basefile";
  String TEMPLATE_TEMPLATES_KEY = "net.ugorji.oxygen.wiki.template.templates";

  // key for the base dir of this provider filesystem (for this category)
  String PROVIDER_FILESYSTEM_HELPER_CLASS_KEY = "net.ugorji.oxygen.wiki.provider.filesystem.helper";
  String PROVIDER_FILESYSTEM_LOCATION_BASE_KEY = "net.ugorji.oxygen.wiki.provider.filesystem.location.base";
  String PROVIDER_FILESYSTEM_NOT_ATTACHMENT_CONFIGURED_REGEX_KEY =
      "net.ugorji.oxygen.wiki.provider.filesystem.attachment.regex.not.match";
  String PROVIDER_FILESYSTEM_NOT_PAGE_CONFIGURED_REGEX_KEY =
      "net.ugorji.oxygen.wiki.provider.filesystem.page.regex.not.match";

  // different servlet actions
  String ACTION_DELETE = "delete";
  String ACTION_SECTIONS = "sections";
  String ACTION_VIEW = "view";
  String ACTION_VIEW_ATTACHMENT = "viewattachment";
  String ACTION_ADMIN = "admin";
  String ACTION_EDIT = "edit";
  String ACTION_ATTACHMENTS = "attachments";
  String ACTION_REVIEW = "review";
  String ACTION_ATTACHMENTINFO = "attachmentinfo";

  // if in the request attribute, then we do not show the add form (used in attachments.jsp,
  // reviews.jsp)
  // String REQUEST_ATTRIBUTE_HIDE_ADD_FORM_KEY = "REQUEST_ATTRIBUTE_HIDE_ADD_FORM_KEY";
  // String REQUEST_PARAMETERS_KEY = "REQUEST_PARAMETERS_KEY";

  // email attributes
  String ENGINE_EMAIL_SENDER = "net.ugorji.oxygen.wiki.email.sender";
  String ENGINE_EMAIL_SMTP_HOST = "net.ugorji.oxygen.wiki.email.smtp.host";
  String ENGINE_EMAIL_SUPPORTED_KEY = "net.ugorji.oxygen.wiki.email.supported";
  String ENGINE_USERNAME_SET_SUPPORTED_KEY = "net.ugorji.oxygen.wiki.username.set.supported";

  String EMAIL_RECIPIENTS_PREFIX = "recipient.";

  // some variables used for searching (storing stuff in the index)
  // these are also used as parameters for searching. Do not change these.
  String SEARCH_INDEX_VERSION = "VERSION";
  String SEARCH_INDEX_CATEGORY = "CATEGORY";
  String SEARCH_INDEX_PAGE = "PAGE";
  String SEARCH_INDEX_PAGE_EDITOR_COMMENT = "PAGE_EDITOR_COMMENT";
  String SEARCH_INDEX_ATTACHMENT_NAME = "ATTACHMENT_NAME";
  String SEARCH_INDEX_CONTENTS = "CONTENTS";
  String SEARCH_INDEX_PAGENAME = "PAGENAME";
  String SEARCH_INDEX_LAST_MODIFIED = "LAST_MODIFIED";
  String SEARCH_INDEX_AUTHOR = "AUTHOR";
  String SEARCH_INDEX_LAST_EDITOR = "LAST_EDITOR";
  String SEARCH_INDEX_COMMENTS = "COMMENTS_CONTENTS";
  String SEARCH_INDEX_COMMENT_NAME = "COMMENT_NAME";
  String SEARCH_INDEX_TAGS = "TAGS";
  String SEARCH_INDEX_INDEX_TYPE = "INDEX_TYPE";
  String SEARCH_INDEX_SIMPLE_SEARCH_KEY = "SIMPLE_SEARCH_KEY";
  String SEARCH_INDEX_REFERENCES = "REFERENCES";

  String SEARCH_MAX_NUM_HITS_KEY = "net.ugorji.oxygen.wiki.search.hits.max";
  String SEARCH_MIN_SCORE_KEY = "net.ugorji.oxygen.wiki.search.score.min";
  String SEARCH_THRESHOLD_SCORE_KEY = "net.ugorji.oxygen.wiki.search.score.threshold";

  // max TTL for an edit lock
  String EDIT_LOCK_TTL_KEY = "net.ugorji.oxygen.wiki.edit.lock.ttl";
  String EDIT_LOCK_TYPE_KEY = "net.ugorji.oxygen.wiki.edit.lock.type";

  // some supported ways of changing content (U can toggle on or off)

  String HTML_TAGS_SUPPORTED_KEY = "net.ugorji.oxygen.wiki.html.tags.supported";
  String USE_CONTEXT_PAGE_TO_RESOLVE_LINKS_KEY = "net.ugorji.oxygen.wiki.usecontextpagetoresolvelinks";

  String I18N_BASENAME = "net.ugorji.oxygen.wiki.WikiResources";

  String ENTRY_PAGE_KEY = "net.ugorji.oxygen.wiki.entry.page";
  String DEFAULT_ENTRY_PAGE = "Main";

  String GENERATED_RSS_FEED_INTERVAL_MS_KEY = "net.ugorji.oxygen.wiki.rss.generated.interval";
  String WIKI_CALENDAR_LAST_DATE_KEY = "net.ugorji.oxygen.wiki.calendar.date";

  // String WIKI_TRAIL_SESSION_KEY = "net.ugorji.oxygen.wiki.trail";
  // String LOCALE_SESSION_KEY = "net.ugorji.oxygen.wiki.locale";
  String WIKI_USER_SESSION_SESSION_KEY = "net.ugorji.oxygen.wiki.user.session";

  String EMAIL_FORMAT_KEY = "net.ugorji.oxygen.wiki.email.format";
  String DELETE_TEMP_UPLOADED_ATTACHMENTS_AFTER_SAVE =
      "net.ugorji.oxygen.wiki.attachments.uploads.delete.temp";

  String PAGETEMPLATE_PARENTPAGE = "net.ugorji.oxygen.wiki.pagetemplate.parentpage";
  String RECREATE_INDEX_ON_STARTUP_KEY = "net.ugorji.oxygen.wiki.index.recreate.on.startup";
  String CLEAR_CACHE_ON_STARTUP_KEY = "net.ugorji.oxygen.wiki.cache.clear.on.startup";

  String INDEX_DETAILS_OF_PAGE_KEY = "net.ugorji.oxygen.wiki.index.details";

  String INDEXING_ANALYZER_CLASSNAME_KEY = "net.ugorji.oxygen.wiki.indexing.analyzer";

  String ATTRIBUTE_AUTHOR = "author";
  String ATTRIBUTE_COMMENTS = "comments";
  String ATTRIBUTE_TAGS = "tags";
  String ATTRIBUTE_PUBLISHED = "published";
  String ATTRIBUTE_SIZE = "size";
  String ATTRIBUTE_TIMESTAMP = "timestamp";

  String REDIRECT_AFTER_POST_SUFFIX_KEY = "net.ugorji.oxygen.wiki.redirect.after.post.suffix";

  String VIEW_CONTEXT_KEY = "net.ugorji.oxygen.wiki.viewcontext";
  String QUERYPARAMETERS_KEY = "net.ugorji.oxygen.wiki.queryparameters";
  String TEMPLATE_WIKIPAGE_KEY = "net.ugorji.oxygen.wiki.template.wikipage";
  String TEMPLATE_WIKIPAGE_TEXT_KEY = "net.ugorji.oxygen.wiki.template.wikipage.text";
  String TEMPLATE_JSPPAGE_KEY = "net.ugorji.oxygen.wiki.template.jsppage";
  String TEMPLATE_SHOWBORDERS_KEY = "net.ugorji.oxygen.wiki.template.showborders";
  String TEMPLATE_REALPAGEVIEW_KEY = "net.ugorji.oxygen.wiki.template.realpageview";

  String USERPREFERENCES_MANAGER_KEY = "net.ugorji.oxygen.wiki.userpreferences.manager";

  String CAPTCHA_ENABLED_KEY = "net.ugorji.oxygen.wiki.captcha.enabled";
  String SHOW_RSS_LINK_IN_HEADER_KEY = "net.ugorji.oxygen.wiki.show.rss.link.in.header";
  String RSS_FORMAT_KEY = "net.ugorji.oxygen.wiki.rss.format";

  String ONLY_INDEX_PUBLISHED_PAGES_KEY = "net.ugorji.oxygen.wiki.only.index.published.pages";

  String PUBLISHER_GROUPS_KEY = "net.ugorji.oxygen.wiki.publisher_groups";

  String SUBMIT_REQUEST_PARAMETER = "wiki.submit";
  String TEMPLATE_PROPERTY_PREFIX = "net.ugorji.oxygen.wiki.template.jsp.";
  String PAGE_DECORATION_PREFIX = TEMPLATE_PROPERTY_PREFIX + "decoration.";

  String READ_TIMEOUT_KEY = "net.ugorji.oxygen.wiki.urlconnection.read_timeout";
  String CONNECT_TIMEOUT_KEY = "net.ugorji.oxygen.wiki.urlconnection.connect_timeout";

  String OPTIMIZATION_SYNCHRONIZE_SAVE_DELETE_ON_INTERNED_STRINGS_KEY =
      "net.ugorji.oxygen.wiki.synchronize_save_delete_on_interned_strings";
  String BSF_LANGUAGE_KEY = "net.ugorji.oxygen.wiki.bsf.lang";

  // String PARAMETER_TEXT = WebConstants.NON_RENDER_PARAMETER_PREFIX + "text";
  String PARAMETER_TEXT = "text";

  String CONSTRAIN_TAGS_KEY = "net.ugorji.oxygen.wiki.edit.constrain_tags";
  String ALLOWED_TAGS_KEY = "net.ugorji.oxygen.wiki.edit.allowed_tags";

  String USER_SHORTCUT_KEY = "net.ugorji.oxygen.wiki.user.shortcut";

  String CACHE_DRAFT_GROUP_PREFIX = "pagedraft";
  String PAGE_DRAFT_SUPPORTED_KEY = "net.ugorji.oxygen.wiki.page_draft_supported";
  String DELETE_VERSION_SUPPORTED_KEY = "net.ugorji.oxygen.wiki.delete_version_supported";
}
