TODO: All THIS STUFF
___________________________________________________________________

***Priority List***
- Have a nightly database maintenance routine
- figure out what to do with the event notes
- When the contact list is in search mode, there needs to be something to stop the contact updtes
    -- adds everyone to the database!
- stop the updater from including the misses you group
- make groups editor show mosaic of contact photos of it's photo
- re-evaluate what needs to be saved when a fragment goes into the background
 - may be related to crashes

- background images
    - from text messages
    - internet source
        - picasso
        - facebook
    - display generic photo

- make cards interface
- flag for manually entered events?

- contact detail plant animation

- Access gmail through official API: https://developers.google.com/gmail/android/
- Use ActionMode.callback to manually select multiple contacts to remove from a group
    - http://stackoverflow.com/questions/20304140/onlongclick-with-context-action-bar-cab-not-taking-place-only-onlistitemclick-p
    - http://stackoverflow.com/questions/12137798/remove-contact-from-a-specific-group-in-android
    - http://stackoverflow.com/questions/10598348/multiple-selection-in-custom-listview-with-cab/10598553#10598553
    - http://stackoverflow.com/questions/17635499/hide-items-in-context-action-bar-cab-dynamically-when-multiple-items-are-selec
    - http://stackoverflow.com/questions/17714124/how-to-add-contact-group-to-my-own-account-in-android-4-2

- Update contact stats when a communication happens
- Test XML file import
- Keep group updates from running too often in the contactsListActivity
- deleting a group in google should delete on app

**** Need Help ****
- Convert project to Gradle
 -http://tools.android.com/tech-docs/new-build-system/migrating-from-eclipse-projects
- Get Support Library v7
 -http://developer.android.com/tools/support-library/setup.html
 - http://developer.android.com/tools/support-library/index.html
 - http://developer.android.com/tools/support-library/features.html
-Update to Material Design
 - https://developer.android.com/training/material/theme.html

***Bug List***
- not entering a number in the wordcount dialog of the event entry fragment, hitting OK, causes crash
- crashing when contact notes are updated
- Is it a problem that the method that creates the master contact list for the update, is also
    reading from the Misses You list?
+ [bug] the scroll position for the current detail view affects the chart loading for the next page
    - premature loading of the chart
- when the detail stats range is chosen, the background photo position shifts
- Fix bug: The user should not be able to delete contacts from the search results in the Contact List
- Make the Cancel update button work.
- fix update auto-run
- fix onResume for wordCloud


***Future***
- add another option to the CAB:
    - Move selection to another group
    -
- Event Entry Fragment: Add a mapping interface button to pull a new address/place
- context/item help
- Fix the title and userpic display of the action bar
    - http://stackoverflow.com/questions/14427005/setting-actionbar-title-with-a-viewpager
    - http://stackoverflow.com/questions/19292605/android-viewpager-actionbar-and-android-app-fragments
    - Or just replace it with a custom menu bar with the frosted glass effect
- group stats must be collected just like contact stats
+ Word cloud
    - words you use with others but don't use with the current contact
- add "Average Reply Time" to the detail stats display
- Social event viewer
    - have a feature to display the library of tags
- Access facebook through official API: https://developers.facebook.com/docs/android
- Need to have some kind of happy chime and message after making contact with a friend
- Access hangouts data: ?
- On the chart, it would be nice to display the data value.  But changing setDisplayChartValues(true)  crashes the program.
- Figure out how to cluster events into a conversation
- Figure out how to calculate average reply time
- Create cards viewer for special data
    - Create a first-time startup UI
    - infrequent charts
    - feature notifications
    - make animation for welcome screen
- interface for other applications to send text to this, and enter that text into the event database with full analysis
- For periodic database updates, it may be faster just to do it by message, rather than contact
- Make the welcome screen update skip if the last update was very recent



***Not Sure if Needed***
- fix crash when setDisplayChartValues(true) for renderer_SMS
- Is it possible to monitor when communication happens from a different app?
- Find a cluster analysis package.
- Cloud-based backup?
- disallow viewPager for chartView - http://stackoverflow.com/questions/19359516/achartengine-with-panning-inside-a-viewpager
- Save output of chart engine as bitmap? http://stackoverflow.com/questions/3107527/android-save-view-to-jpg-or-png
- Implement listeners for the preference changes (http://developer.android.com/guide/topics/ui/settings.html)



Notes
___________________________________________________________________
Get library for a chartengine: https://code.google.com/p/achartengine/
Get library for mpandroidchartlibrary https://github.com/PhilJay/MPAndroidChart


Done
___________________________________________________________________
- Method to edit contact decay setting
+ Create class to estimate contact due date
+ Create routine for producing the long duration statistics:
    - average time
    - score over time
- implement update of individual contacts when viewing the contact list
- OnClick listener in the groupsEditor interface is broken
- redo data import to better support a per contact update
- activity for creating group
- view the last 6 communications
+ Event Entry Fragment
    - [done] hook into event database
    - [done] add in human readable date
    - [done but for address] Make values persist with screen rotation
- created interface for adding to the contact notes
- charts instead of numbers in Detail stats
- contact detail activity header bar
    - implement contact action menu
+ [done] Make static plot for recent history of contact
    - make it load when user scrolls down
    - there needs to be a datapoint for every month/week, in range, even if zero
+ Word cloud
    - words you use with someone
- having an overlap time for the update can lead to duplicate data getting added into stats
+ app should add and remove contact from Misses You group automatically at the appropriate times
    - [done] can add/remove contacts from group at time of update
    - [done] can add/remove contacts from group at time of manual event entry
    - [done] set updates to happen through the welcome screen with a preference activation
+ performing db updates based on the contacts in the largest group assumes that everyone is in the largest group.  Bad assumption
+ Change UI to infinite tab interface (like photo viewing) for switcing between contacts
    - http://developer.android.com/reference/android/support/v4/app/FragmentStatePagerAdapter.html
    - https://github.com/xgc1986/ParallaxPagerTransformer
    - http://stackoverflow.com/questions/23433027/onpagechangelistener-alpha-crossfading
+ Use photo blur on contact detail image as user scrolls down
    - http://nicolaspomepuy.fr/blur-effect-for-android-design/
    - https://github.com/PomepuyN/BlurEffectForAndroidDesign/blob/master/BlurEffect/res/layout/activity_main.xml
- Create interface for adding contacts to group
+ fix the fact that tapping on the update notification initiates a new file import activity
  - remove the intent
- Fix Default Contact Group of the Preferences menu, currently hard set
- FEATURE TO add contacts to a group
- bug with phone call count, both parties get same number on readout
- There's a bug with the stats display in the contactDetailFragment.  Bad numbers for 1 Month.
- move all the stats accumulation to a ContactDetailFragment for on the fly display of the same stats
 for 1 month and 6 month time ranges, as well, as all time, in an on-demand fashion
  - involves making a EventLog content Provider
  - removing the old code
- from the group list, pressing the + button should allow the user to add an existing contact to the group
  - can use standard contacts app to perform selection
_ editing dialog for new event notes
- make new event activity text field not auto focus
- fixed screen rotation
- move the interactive chart to it's own full screen activity
- push contact photo into actionbar
- place reach-out buttons in the actionbar
- place header graphics for in/out in Details Table
- Create interface for manually adding events
-- pretty major bug where the auto_update ran repeatedly in quick successsion
- Collect stats for the group by creating new contacts with ContactKey "Group"
  - perhaps there needs to be a contactStats data field for dominating group
- Default group bug fix AND the list of groups to choose from is automatically updated
- The various contact initiation buttons now reference correct contact information, so pressing them initiates the correct address
- Have a nightly database update routine
- used event database to keep track of when each data source is pulled from
- used the SocialEventsContract to inform PhoneLogAccess methods how far back the last update covered.
- make charts on contact list update
- Add Notification graphic to indicate that the data is still being collected - “Progress Bar” “Dialogs” “Intermediate” - for a dialog box with the cirular progress indicator
    - kind of how PocketCast has the notification animation for downloading media
- Maybe have a ContentProvider for the contact database but not the event database.
- Swipe geusture for changing date range on data plot.
- display text in the middle of the fraction chart indicating the value,
      toggling text with a tap.
- change span of chart based on chart taps
- Background fetching data from the database for the chart
- Populate the screen of statistics with real data
    - Average days between events
    - Max days between events
    - Total word count
    - Total call time
    - Average Call time
    - Reciprocity for each event Kind
- Implement date range function for chart
    - Make the standard range up to 1 year
    - if there is more data before, display a "back arrow" button on the bottom.
        When the button is pressed, redraw the chart with the previous set of data
    - When displaying the previous set of data, show a "forward arrow" button.
- Using selection from drop down box, populate the rest of the list of contacts
- Show the number of people in the group
- Only show 2 groups in the group list: Starred and BeSocial
- Generate graph of communicaton data for display per contact.
        - https://code.google.com/p/achartengine/
- Fixed bugs with my contacts (Brandy, Tyson) crashing when attempting to look at them.
- Make the load contact logs (sms and call) an async task.  This was the start of breaking up ContactDetailFragment into mutilple files.
- KS: fix SMS log.  I broke it. NOTE: this change is really on betterBranch.
- (for Call Log) BD: Under SMS Log, Call log and email log, make 'incoming' and 'outgoing', 'missed' be different colors with arrows possibly
- Load cummunication log as background task when contact is selected
- Load list of contacts within group as async task
- Move betterBranch to main branch.  Note- we have made a mess trying to do this.  Maybe ask someone really good at git to help with this at some point.
- Add color changing progress bar to individual contacts on the list of contacts
- Break up ContactDetailFragment into multiple files.
    - Done: Data collection
    - Done: Chart generation
- bring back the underscore shading under the tab buttons