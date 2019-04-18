Tests
=====

Do this per section
-------------------
Do the following in sequence, for 3 different sections:
- Simple File section (sandbox)
- RCS backed section  (rcstest)
- Perforce section    (p4test)

Actions:
- View index page
  - Ensure that pages which are excluded cannot be created/or shown 
  (e.g. **/RCS/**, **/.OXYWIKI/**)
  - ensure lock is removed if error during save happens
- view page history
  - test diff
- View Main page
  - Ensure that details are shown, for rcstest and simple 
    (and not for p4test)
- Create a page 
  - edit the page you just created
  - add attachments (1)
  - add attachment  (3 of them - with 2 of them 
    pointing to the attachment added the first time)
  - view attachment information
  - add review
    (ensure that the review is not treated as an attachment)
- view wiki source (for latest version, and for version 1)
- delete attachments, then add it again. 
  (look at info, ensure version increments.)
- delete the page, with some attachments still there 
  (ensure PageIndex doesn't show the page again)
- Ensure that the RuntimePersistenceProvider works for this helper

On sandbox section, do the following
------------------------------------
- look at all section-wide and global actions
- Ensure menu completely works
- Test show/hide views
- Test Calendar. Especially for year, month, day views
  - Ensure that days with data are click'able
- Test references
  - Ensure that it lists that Main references Main2
- Test User Preferences
- Test email notifications 
  - set user preferences to ugorjid@bea.com, ugorjid
  - then try to edit pages on the mailtest section
  - test it both for text, and for html

On builtin section, do the following
------------------------------------
- Test all the menu actions.
- Test Admin functions.
  - change branding text, and reload metadata 
    (ensure u see new branding text)
  - Reset Engine
- Test Language changing (en, jp)

Other Actions
-------------
- Jar up the help directory, and ensure that we can browse it.
- Browse Help. Ensure it all shows.
  - use admin to point to a help.jar in /tmp. Browse that help.
- Test that you can persist user preferences
- Ensure exclusive editing is respected.
  - edit a page, then edit the page in another browser session 
    (to test for exclusive access)
  - mailtest section uses exclusive access.
- Ensure that extra params are only shown where appropriate
  and that trails, link-to-section-Main-page, etc do not have version parameters
- Run tool through all help pages, to ensure they don't throw errors.
  - any issue would have shown while wiki was loading up
- Test RSS feeds working
- Search
  - Across all sections, 1 section, multiple sections

********** <STOPPED HERE ... > **********

Installed packaging testing
---------------------------
- Run through final installed package.


Skipped / bugs
==================
Noted:
- Seems readonly depot is not in sync with write depot.

