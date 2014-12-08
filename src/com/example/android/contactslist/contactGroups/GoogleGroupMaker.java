package com.example.android.contactslist.contactGroups;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Tyson Macdonald on 11/22/2014.
 */
public class GoogleGroupMaker {

    private ContentResolver mContentResolver;
    private Context mContext;

    public GoogleGroupMaker(Context context){
        mContext = context;
        mContentResolver = context.getContentResolver();

    }

    /*
    Set membership to the identified group, if not already a member
     */
    public boolean makeContactGroup(String name){

        //TODO Does the group already exist?
        boolean isNameNew = true;

        //Add if it's a new name
        if(isNameNew) {
            methodTwo(name);
            return true;
        }else {
            return false;
        }
    }

    private void methodTwo(String group_name){

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Groups.CONTENT_URI)

                // the id should be assigned automatically .withValue(ContactsContract.Groups._ID, ANY LONG UNIQUE VALUE)
                .withValue(ContactsContract.Groups.TITLE, group_name)
                .withValue(ContactsContract.Groups.ACCOUNT_TYPE, "com.google")
                // TODO create a resource for the account Name
                .withValue(ContactsContract.Groups.ACCOUNT_NAME, "tmacdona@gmail.com")
                .withValue(ContactsContract.Groups.GROUP_VISIBLE,1)
                .build());


        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            Log.e("ContactsManager", "Failed to apply batch: ");
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void removeGoogleGroup(long groupId)
    {
        String where = ContactsContract.Groups._ID + "=?" ;
        try
        {
            mContentResolver.delete(ContactsContract.Groups.CONTENT_URI,
                    where,
                    new String[] { String.valueOf(groupId) });
        } catch (Exception e)
        {
            e.printStackTrace();
        }



    }
}


