package com.example.android.contactslist.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.AlphabetIndexer;

import java.util.ArrayList;


/**
 * Adapted from https://github.com/xgc1986/ParallaxPagerTransformer
 * Created by Tyson Macdonald on 10/2/2014.
 */
public class ContactDetailAdapter  extends FragmentStatePagerAdapter {

    private ArrayList<TestContactDetailFragment> mFragments;
    private ViewPager mPager;

    public ContactDetailAdapter(Context context, FragmentManager fm) {
        super(fm);

        mFragments = new ArrayList<TestContactDetailFragment>();
   }


    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public void add(TestContactDetailFragment fragment) {
        fragment.setAdapter(this);
        mFragments.add(fragment);
        notifyDataSetChanged();
        mPager.setCurrentItem(getCount() - 1, true);

    }

    public void swapCursor (Cursor newCursor){

        if((newCursor != null) && (newCursor.moveToFirst())){
            do{

                // Generates the contact lookup Uri
                final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                        newCursor.getLong(ContactsListFragment.ContactsQuery.ID),
                        newCursor.getString(ContactsListFragment.ContactsQuery.LOOKUP_KEY));

                final Bundle bundle = new Bundle();
                bundle.putParcelable(TestContactDetailFragment.EXTRA_CONTACT_URI, contactUri);
                final TestContactDetailFragment fragment = new TestContactDetailFragment();

                fragment.setArguments(bundle);

                this.add(fragment);
            }while(newCursor.moveToNext());
        }else{

        }
    }

    public void removeAll(){
        mFragments.clear();
    }


    public void remove(int i) {
        mFragments.remove(i);
        notifyDataSetChanged();
    }

    public void remove(TestContactDetailFragment fragment) {
        mFragments.remove(fragment);

        int pos = mPager.getCurrentItem();
        notifyDataSetChanged();

        mPager.setAdapter(this);
        if (pos >= this.getCount()) {
            pos = this.getCount() - 1;
        }
        mPager.setCurrentItem(pos, true);

    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setPager(ViewPager pager) {
        mPager = pager;
    }

}
