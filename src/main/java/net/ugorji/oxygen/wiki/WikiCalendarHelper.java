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
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import net.ugorji.oxygen.markup.MarkupRenderContext;
import net.ugorji.oxygen.markup.MarkupRenderEngine;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;

public class WikiCalendarHelper implements Serializable {
  private static String SEP = "/";

  private List weeks; // - list of int[7]
  private Date origdate;

  private Calendar cal;

  private boolean showDay = false;
  private boolean showMonth = false;
  private boolean showYear = false;

  private int numParagraphsDay = 4000;
  private int numParagraphsMonth = 4;
  private int numParagraphsYear = 1;

  public WikiCalendarHelper(String datestr) throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WebInteractionContext _request = WebLocal.getWebInteractionContext();
    WikiLinkHolder lh = WikiLocal.getWikiLinkHolder();
    int[] ymd = WikiCalendarHelper.parse(datestr, SEP);
    if (ymd[2] != -1) {
      showDay = true;
    } else if (ymd[1] != -1) {
      showMonth = true;
    } else {
      showYear = true;
    }

    if (ymd[1] == -1) {
      ymd[1] = 5;
    }
    if (ymd[2] == -1) {
      ymd[2] = 15;
    }
    cal = Calendar.getInstance(lh.getLocale());
    cal.clear();
    cal.setLenient(true);
    cal.set(ymd[0], ymd[1], ymd[2]);

    postInit(_request);
  }

  public WikiCalendarHelper(Date d) throws Exception {
    WikiLinkHolder lh = WikiLocal.getWikiLinkHolder();
    WebInteractionContext _request = WebLocal.getWebInteractionContext();
    cal = Calendar.getInstance(lh.getLocale());
    cal.setLenient(true);
    cal.setTime(d);
    postInit(_request);
  }

  public Calendar getCal() {
    return cal;
  }

  public Date getOrigdate() {
    return origdate;
  }

  public List getWeeks() {
    return weeks;
  }

  public synchronized WikiProvidedObject[] getPages() throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject[] wps = null;
    WikiLinkHolder lh = WikiLocal.getWikiLinkHolder();
    try {
      Date d1 = null;
      Date d2 = null;
      if (showDay) {
        d1 = cal.getTime();
        d2 = d1;
      } else if (showMonth) {
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        d1 = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        d2 = cal.getTime();
      } else if (showYear) {
        cal.set(Calendar.MONTH, cal.getActualMinimum(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        d1 = cal.getTime();
        cal.set(Calendar.MONTH, cal.getActualMaximum(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        d2 = cal.getTime();
      }
      // ... and sort them based on date, and in reverse order
      wps = WikiUtils.getWikiPagesGivenTimeWindow(wce, d1, d2);
      for (int i = 0; i < wps.length; i++) {
        wps[i] =
            wce.getPageProvider()
                .getPage(wps[i].getName(), WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);
      }

      Comparator comp =
          new Comparator() {
            public int compare(Object o1, Object o2) {
              WikiProvidedObject arg1 = (WikiProvidedObject) o1;
              WikiProvidedObject arg2 = (WikiProvidedObject) o2;
              return (-1 * arg1.getDate().compareTo(arg2.getDate()));
            }
          };
      Arrays.sort(wps, comp);
    } finally {
      cal.setTime(origdate);
    }
    return wps;
  }

  public synchronized Collection getDaysInCurrentMonthWithData() throws Exception {
    try {
      WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
      HashSet hs = new HashSet();
      cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
      Date d1 = cal.getTime();
      cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      Date d2 = cal.getTime();
      long d1long = d1.getTime();
      long d2long = d2.getTime();
      WikiProvidedObject[] wps = WikiUtils.getWikiPagesGivenTimeWindow(wce, d1, d2);
      for (int i = 0; i < wps.length; i++) {
        Date d99 = wps[i].getDate();
        long d99long = d99.getTime();
        if (d99long >= d1long && d99long <= d2long) {
          cal.setTime(d99);
          hs.add(new Integer(cal.get(Calendar.DAY_OF_MONTH)));
        }
      }
      // System.out.println("getDaysWithData: " + hs);
      // Thread.dumpStack();
      return hs;
    } finally {
      cal.setTime(origdate);
    }
  }

  public String getTitle(WikiProvidedObject wp) throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    String title = null;
    String calTitleSrc = wce.getProperty(WikiConstants.CALENDAR_TITLE_SOURCE_KEY);
    if ("PageName".equals(calTitleSrc)) {
      title = wp.getName();
    } else if ("FirstLine".equals(calTitleSrc)) {
      BufferedReader br = getReader(wp);
      title = getFirstLine(br);
    }
    return title;
  }

  public void writeHTML(WikiProvidedObject wp) throws Exception {
    BufferedReader br = getReader(wp);
    try {
      WebInteractionContext request = WebLocal.getWebInteractionContext();
      WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
      MarkupRenderEngine re = wce.getRenderEngine();
      MarkupRenderContext rc = new WikiRenderContext(wce, wp, true);
      if (showDay) {
        re.render(request.getWriter(), br, rc, numParagraphsDay);
      } else if (showMonth) {
        re.render(request.getWriter(), br, rc, numParagraphsMonth);
      } else if (showYear) {
        re.render(request.getWriter(), br, rc, numParagraphsYear);
        // String calTitleSrc = wce.getProperty(WikiConstants.CALENDAR_TITLE_SOURCE_KEY);
        // if(!("FirstLine".equals(calTitleSrc))) {
        //  response.getWriter().println(getFirstLine(br));
        // }
      }
    } finally {
      CloseUtils.close(br);
    }
  }

  private void postInit(WebInteractionContext _request) {
    cal.setLenient(true);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);

    origdate = cal.getTime();
    // System.out.println("origdate: " + origdate);

    weeks = new ArrayList();

    int[] week0 = new int[7];
    Arrays.fill(week0, -1);
    int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    int firstDayOfWeek = cal.getFirstDayOfWeek();
    for (int i = 1; i <= maxDay; i++) {
      cal.set(Calendar.DAY_OF_MONTH, i);
      int idx = cal.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
      week0[idx] = i;

      if (idx == 6) {
        weeks.add(week0);
        week0 = new int[7];
        Arrays.fill(week0, -1);
      }
    }
    if (week0[0] != -1 || week0[6] != -1) {
      weeks.add(week0);
    }

    cal.setTime(origdate);
  }

  private BufferedReader getReader(WikiProvidedObject wp) throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    Reader r = wce.getPageProvider().getPageReader(wp);
    BufferedReader br = null;
    if (r instanceof BufferedReader) {
      br = (BufferedReader) r;
    } else {
      br = new BufferedReader(r);
    }
    return br;
  }

  private String getFirstLine(BufferedReader br) throws Exception {
    try {
      String lineread = null;
      while ((lineread = br.readLine()) != null) {
        lineread = lineread.trim();
        if (lineread.length() > 0) {
          return lineread;
        }
      }
      return null;
    } finally {
      CloseUtils.close(br);
    }
  }

  // 2005_09_15 2005_09 2005
  private static int[] parse(String datestr, String sep) {
    String[] elems = StringUtils.split(datestr, sep);
    int[] arr = new int[3];
    Arrays.fill(arr, -1);
    if (elems.length > 0) {
      arr[0] = Integer.parseInt(elems[0]);
    }
    if (elems.length > 1) {
      arr[1] = Integer.parseInt(elems[1]) - 1;
    }
    if (elems.length > 2) {
      arr[2] = Integer.parseInt(elems[2]);
    }
    // System.out.println("arr: " + arr[0] + " " + arr[1] + " " + arr[2]);
    return arr;
  }
}
