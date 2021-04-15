package com.padmajeet.techforedu.wowcollege.admin;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentStudentAttendance extends Fragment {
    private View view=null;
    private TabLayout tabAttendance;
    private ViewPager viewPager;
    int tabPos;

    public FragmentStudentAttendance() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_student_attendance, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.studentAttendance));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.enquiry));
        tabAttendance = view.findViewById(R.id.tabAttendance);
        viewPager = view.findViewById(R.id.viewPager);

        tabAttendance.addTab(tabAttendance.newTab().setText(getString(R.string.takeAttendance)));
        tabAttendance.addTab(tabAttendance.newTab().setText(getString(R.string.studentAttendance)));

        ViewPagerAdapter tabsAdapter = new ViewPagerAdapter(getChildFragmentManager(), tabAttendance.getTabCount());
        viewPager.setAdapter(tabsAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabAttendance));

        tabAttendance.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                viewPager.getAdapter().notifyDataSetChanged();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        SessionManager sessionManager = new SessionManager(getContext());
        tabPos = sessionManager.getInt("tabPos");
        tabPos = tabPos<0?0:tabPos;

        tabAttendance.getTabAt(tabPos).select();
        sessionManager.remove("tabPos");
    }
    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;
        public ViewPagerAdapter(FragmentManager fm, int NoOfTabs){
            super(fm);
            this.mNumOfTabs = NoOfTabs;
        }
        @Override
        public int getCount() {
            return mNumOfTabs;
        }
        @Override
        public Fragment getItem(int position){
            switch (position){
                case 0:
                    FragmentTakeStudentAttendance fragmentTakeAttendance = new FragmentTakeStudentAttendance();
                    return fragmentTakeAttendance;
                case 1:
                    FragmentAttendance fragmentAttendance = new FragmentAttendance();
                    return fragmentAttendance;
                default:
                    return null;
            }
        }
    }

}
