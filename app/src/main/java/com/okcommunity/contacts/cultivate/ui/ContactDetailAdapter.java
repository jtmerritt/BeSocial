package com.okcommunity.contacts.cultivate.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import java.util.ArrayList;
import android.database.Cursor;
import android.widget.AlphabetIndexer;


/**
 * Adapted from https://github.com/xgc1986/ParallaxPagerTransformer
 * Created by Tyson Macdonald on 10/2/2014.
 */
public class ContactDetailAdapter  extends FragmentStatePagerAdapter {

    private ArrayList<ContactDetailFragment> mFragments;
    private ViewPager mPager;
    private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance


    public ContactDetailAdapter(Context context, FragmentManager fm) {
        super(fm);

        mFragments = new ArrayList<ContactDetailFragment>();
   }

    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public void add(ContactDetailFragment parallaxFragment) {
        parallaxFragment.setAdapter(this);
        mFragments.add(parallaxFragment);
        notifyDataSetChanged();
        mPager.setCurrentItem(getCount() - 1, true);

    }

    public void swapCursor (Cursor newCursor){

        Bundle bundle;
        ContactDetailFragment frag;
        Uri contactUri;

        if((newCursor != null) && (newCursor.moveToFirst())){
            do{

                // Generates the contact lookup Uri
                contactUri = ContactsContract.Contacts.getLookupUri(
                        newCursor.getLong(ContactsListFragment.ContactsQuery.ID),
                        newCursor.getString(ContactsListFragment.ContactsQuery.LOOKUP_KEY));

                bundle = new Bundle();
                bundle.putParcelable(ContactDetailFragment.EXTRA_CONTACT_URI, contactUri);
                frag = new ContactDetailFragment();

                frag.setArguments(bundle);

                this.add(frag);
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

    public void remove(ContactDetailFragment parallaxFragment) {
        mFragments.remove(parallaxFragment);

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
