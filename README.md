TODO: All THIS STUFF
___________________________________________________________________

***Small Tasks***
- make new event activity text field not auto focus
- group stats must be collected just like contact stats
-- performing db updatees based on the contacts in the largest group assumes that everyone is in the largest group.  Bad assumption
- Have a nightly database maintenance routine
- restrict to portrait mode
- move the interactive chart to it's own full screen activity
- fix the fact that tapping on the update notification initiates a new file import activity

***Medium Tasks***
- place header graphics for in/out in Details Table
- push contact photo into actionbar
- place reach-out buttons at the top, just under the actionbar
- generate real history plot
- fill out new statistics
- Make event browser
- open Event Note editor by clicking on the button with the current text
- which group is viewed by default should be indicated in the menu drawer
- Create routine for producing the long duration statistics:
    - average time
    - score over time
- Create class to estimate contact due date
- Create interface for manually adding events
- Create interface for adding contacts to group
- Low Priority: Try placing user photo and pie chart in action bar, then implement swipable tabs.


***Large Tasks***
- add contacts to a group
- add a group
- Need to have some kind of happy chime and message after making contact with a friend
- Create a first-time startup UI
- Change UI to infinite tab interface (like photo viewing) for switcing between contacts
- Access gmail through official API: https://developers.google.com/gmail/android/
- Access hangouts data: ?
- Access facebook through official API: https://developers.facebook.com/docs/android
- fix crash when setDisplayChartValues(true) for renderer_SMS
- On the chart, it would be nice to display the data value.  But changing setDisplayChartValues(true)  crashes the program.
- Fix Default Contact Group of the Preferences menu, currently hard set



***Not Sure if Needed***
- Is it possible to monitor when communication happens from a different app?
- Find a cluster analysis package.
- Cloud-based backup?
- disallow viewPager for chartView - http://stackoverflow.com/questions/19359516/achartengine-with-panning-inside-a-viewpager
- Save output of chart engine as bitmap? http://stackoverflow.com/questions/3107527/android-save-view-to-jpg-or-png
- Under SMS Log and email log, make 'incoming' and 'outgoing', 'missed' be different colors with arrows possibly  (This is already done for call log)
- Implement tab listeners to only have tab data loaded when needed - “Slow Adaptor” to load items to a scrolling list on the fly
- Implement listeners for the preference changes (http://developer.android.com/guide/topics/ui/settings.html)



Notes
___________________________________________________________________
Get library for a chartengine: https://code.google.com/p/achartengine/


Done
___________________________________________________________________
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