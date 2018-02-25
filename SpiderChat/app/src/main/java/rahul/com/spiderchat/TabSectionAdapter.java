package rahul.com.spiderchat;

import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by rahul on 25/7/17.
 */

public class TabSectionAdapter extends FragmentPagerAdapter {
    String[] titles = {"REQUESTS","CHATS","FRIENDS"};

    public TabSectionAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position)
        {
            case 0:
                return new RequestsFrag();
            case 1:
                return new ChatsFrag();
            case 2:
                return new FriendsFrag();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
