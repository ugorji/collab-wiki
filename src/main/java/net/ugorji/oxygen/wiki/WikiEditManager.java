/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebLocal;

/**
 * Manages the editing within a specific category.
 *
 * @author ugorji
 */
public class WikiEditManager {
  public static final String LOCK_NONE = "NONE";
  public static final String LOCK_WARN = "WARN";
  public static final String LOCK_EXCLUSIVE = "EXCLUSIVE";
  private static final List LOCK_TYPES =
      Arrays.asList(new String[] {LOCK_NONE, LOCK_WARN, LOCK_EXCLUSIVE});

  private WikiCategoryEngine wce;
  private Set locks = Collections.synchronizedSet(new HashSet());
  private long maxttl = (20 * 60 * 1000l);
  private String locktype = LOCK_WARN;

  private TimerTask timertask;

  protected WikiEditManager(final WikiCategoryEngine _wce) {
    wce = _wce;
    String s = wce.getProperty(WikiConstants.EDIT_LOCK_TTL_KEY);
    if (s != null) {
      maxttl = Long.parseLong(((String) wce.getProperty(WikiConstants.EDIT_LOCK_TTL_KEY)).trim());
    }
    locktype = wce.getProperty(WikiConstants.EDIT_LOCK_TYPE_KEY);
    if (locktype == null || !LOCK_TYPES.contains(locktype)) {
      locktype = LOCK_WARN;
    }
    // long checkinterval = maxttl / 100;
    timertask =
        new TimerTask() {
          public void run() {
            try {
              WikiLocal.setWikiEngine(_wce.getWikiEngine());
              cleanupLocks();
            } finally {
              WikiLocal.setWikiEngine(null);
            }
          }
        };
    long interval = getMaxTTL() / 100;
    wce.getWikiEngine().addTask(timertask, interval, interval);
  }

  public void close() {
    // System.out.println("WikiEditManager.close called");
    while (timertask.cancel()) {
      OxygenUtils.sleep(50);
    }
    // timertask.cancel();
    timertask = null;
    cleanupLocks();
  }

  public String getLockType() {
    return locktype;
  }

  public boolean isLockExclusive() {
    return locktype.equals(LOCK_EXCLUSIVE);
  }

  public long getMaxTTL() {
    return maxttl;
  }

  public Set getAllLocks() {
    return new HashSet(locks);
  }

  public synchronized WikiEditLock[] getLocks(String pagename) {
    List list = new ArrayList();
    for (Iterator itr = locks.iterator(); itr.hasNext(); ) {
      WikiEditLock elock = (WikiEditLock) itr.next();
      // System.out.println("pagename: " + pagename);
      // System.out.println("elock: " + elock);
      // System.out.println("elock.getPagename(): " + elock.getPagename());
      if (elock.getPagename().equals(pagename)) {
        list.add(elock);
      }
    }
    return (WikiEditLock[]) list.toArray(new WikiEditLock[0]);
  }

  public synchronized WikiEditLock getLock(String pagename, String username) {
    WikiEditLock mylock = null;
    for (Iterator itr = locks.iterator(); itr.hasNext(); ) {
      WikiEditLock elock = (WikiEditLock) itr.next();
      if (elock.getPagename().equals(pagename) && elock.getUsername().equals(username)) {
        mylock = elock;
        break;
      }
    }
    return mylock;
  }

  public synchronized void releaseLock(String pagename, String username) {
    WikiEditLock elock = getLock(pagename, username);
    if (elock != null) {
      locks.remove(elock);
    }
  }

  public synchronized boolean canAcquireLock(String pagename, String username) {
    boolean b = true;
    if (locktype.equals(LOCK_EXCLUSIVE)) {
      WikiEditLock[] elocks = getLocks(pagename);
      if ((elocks.length == 0)
          || (elocks.length == 1 && elocks[0].getUsername().equals(username))) {
        b = true;
      } else {
        b = false;
      }
    }
    return b;
  }

  public synchronized void acquireLock(String pagename, String username) throws Exception {
    if (locktype.equals(LOCK_WARN)) {
      releaseLock(pagename, username);
      WikiEditLock elock = new WikiEditLock(username, pagename);
      locks.add(elock);
    } else if (locktype.equals(LOCK_EXCLUSIVE)) {
      WikiEditLock[] elocks = getLocks(pagename);
      if ((elocks.length == 0)
          || (elocks.length == 1 && elocks[0].getUsername().equals(username))) {
        releaseLock(pagename, username);
        WikiEditLock elock = new WikiEditLock(username, pagename);
        locks.add(elock);
      } else {
        throw new WikiException(WebLocal.getI18n().str("general.lock_already_acquired"));
      }
    }
  }

  public long getTTL(WikiEditLock elock) {
    long currtime = System.currentTimeMillis();
    long inputtime = elock.getLocktime();
    long timepassed = currtime - inputtime;
    long timeleft = maxttl - timepassed;
    if (timeleft < 0) {
      timeleft = 0;
    }
    return timeleft;
  }

  public String getFormattedTTL(WikiEditLock elock) {
    long ttl = getTTL(elock);
    return StringUtils.getFormattedTimePeriod(ttl);
  }

  private void cleanupLocks() {
    long currtime = System.currentTimeMillis();
    synchronized (locks) {
      for (Iterator itr = locks.iterator(); itr.hasNext(); ) {
        WikiEditLock elock = (WikiEditLock) itr.next();
        long inputtime = elock.getLocktime();
        if ((currtime - inputtime) >= maxttl) {
          itr.remove();
        }
      }
    }
    OxygenUtils.debug("cleanUpLocks ran for section: " + wce.getName());
  }
}
