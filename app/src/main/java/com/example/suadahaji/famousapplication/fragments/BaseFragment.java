package com.example.suadahaji.famousapplication.fragments;




import android.support.v4.app.Fragment;

import rx.subscriptions.CompositeSubscription;

public class BaseFragment extends Fragment {
    protected CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();

    }
}
