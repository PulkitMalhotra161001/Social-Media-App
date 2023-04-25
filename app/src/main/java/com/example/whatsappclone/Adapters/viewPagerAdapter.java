package com.example.whatsappclone.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.whatsappclone.Fragments.AIChatFragment;
import com.example.whatsappclone.Fragments.AnonymousChatFragment;
import com.example.whatsappclone.Fragments.EphemeralChatFragment;
import com.example.whatsappclone.Fragments.GroupChatFragment;
import com.example.whatsappclone.Fragments.PrivateChatFragment;

public class viewPagerAdapter extends FragmentPagerAdapter {
    public viewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new AIChatFragment();
            case 1:
                return new AnonymousChatFragment();
            case 2:
                return new PrivateChatFragment();
            case 3:
                return new GroupChatFragment();
            case 4:
                return new EphemeralChatFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 5;
    }
}
