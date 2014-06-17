package com.example.android.contactslist.contactStats;

import android.content.Context;
import android.os.AsyncTask;
import com.example.android.contactslist.FractionViewCallback;
import com.example.android.contactslist.contactStats.ContactStatsContract;


/*
    Loading event logs from the database
 */

public class LoadContactStatsTask extends AsyncTask<Void, Void, ContactInfo> {

    private String mContactLookupKey;

    private FractionViewCallback mFractionViewCallback;
    private Context mContext; // only added so this class can call on the event database


    public LoadContactStatsTask(String contactKey,
                                FractionViewCallback fractionViewCallback,
                                Context context  // only added so this class can call on the event database
    ) {
        mContactLookupKey = contactKey;
        mFractionViewCallback = fractionViewCallback;
        mContext = context;
    }




    @Override
    protected ContactInfo doInBackground(Void... v1) {

        //grab contact relevant event data from db
        ContactStatsContract db = new ContactStatsContract(mContext);

        //prepare the shere and args clause for the contact lookup key
        final String where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY + "= ? ";
        String[] whereArgs ={ mContactLookupKey };

        ContactInfo contactStats = db.getContactStats(where, mContactLookupKey);
        db.close();
        return  contactStats;

   }

    protected void onProgressUpdate(Integer... progress) {
        // do something
    }

    protected void onPostExecute(ContactInfo result) {
        // do something
        mFractionViewCallback.finishedLoading(result);
    }

}
