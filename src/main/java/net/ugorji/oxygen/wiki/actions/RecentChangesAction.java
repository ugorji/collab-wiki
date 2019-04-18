/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

/**
 * Shows recent changes. This is no longer used, as we just leverage the SearchAction for RSS,
 * Recent Changes and Search. Make it private and abstract to show this.
 *
 * @author ugorji
 */
abstract class RecentChangesAction extends GenericWikiWebAction {}

/*
  public void includeView()
    throws UnsupportedOperationException, Exception {
    preRender();
    includeJSPView("recentchanges.jsp");
  }

  public int render() throws Exception {
    preRender();
    showJSPView("recentchanges.jsp");
    return RENDER_COMPLETED;
  }

  //A startdate is required, else we should not show anything
  //so if startdate is not passed, and changeperiod is not passed, set WikiProvidedObject to empty
  private void preRender() throws Exception {
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    Map model = RecentChangesAction.doWork();
    wlh.setAttribute("wiki.model.recentchanges", model);
  }

  // We show all details if the user has set details on his session for all the categories
  static Map doWork() throws Exception {
    WikiCategoryEngine wce0 = WikiLocal.getWikiCategoryEngine();
    try {
      WikiCategoryEngine wce = null;
      WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
      WebInteractionContext request = WebLocal.getWebInteractionContext();
      WikiEngine we = WikiLocal.getWikiEngine();
      WikiUserSession wus = WikiLocal.getWikiUserSession();

      int changeperiod = 1;
      String s = request.getParameter("changeperiod");
      if(!StringUtils.isBlank(s)) {
        changeperiod = Integer.parseInt(s);
      }
      long ldelta = changeperiod * 24 * 60 * 60 * 1000l;

      String[] categories = request.getParameterValues("cat");
      if(categories == null || categories.length == 0) {
        categories = new String[]{wlh.getCategory()};
      }

      boolean detailsNecessary = WikiUtils.getShowDetailsForMultipleCategories(categories); //wus.getDefaultShowDetails();

      String encoding = null;
      if(categories.length == 1) {
        wce = we.retrieveWikiCategoryEngine(categories[0]);
        encoding = wce.getCharacterEncoding();
      } else {
        encoding = we.getCharacterEncoding();
      }

      Date endDate = new Date(System.currentTimeMillis());
      Date startDate = new Date(endDate.getTime() - ldelta);
      WikiProvidedObject[] wikipages = null;

      Map wikipagesmap = new HashMap();
      for(int i = 0; i < categories.length; i++) {
        wce =  we.retrieveWikiCategoryEngine(categories[i]);
        WikiLocal.setWikiCategoryEngine(wce);
        wikipages = WikiUtils.getWikiPagesGivenTimeWindow(wce, startDate, endDate, detailsNecessary);
        if(wikipages == null) {
          wikipages = new WikiProvidedObject[0];
        }
        wikipagesmap.put(categories[i], wikipages);
      }

      Map model = new HashMap();
      model.put("categories", categories);
      model.put("startdate", startDate);
      model.put("enddate", endDate);
      model.put("changeperiod", new Integer(changeperiod));
      model.put("wikipagesmap", wikipagesmap);
      model.put("encoding", encoding);
      model.put("detailsNecessary", Boolean.valueOf(detailsNecessary));
      return model;
    } finally {
      WikiLocal.setWikiCategoryEngine(wce0);
    }

  }

}

*/
