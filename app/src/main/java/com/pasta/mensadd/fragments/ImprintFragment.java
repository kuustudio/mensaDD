package com.pasta.mensadd.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pasta.mensadd.MainActivity;
import com.pasta.mensadd.R;

public class ImprintFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_imprint, container, false);
        setHasOptionsMenu(true);
        TextView licenseView = v.findViewById(R.id.imprintLicense);

        licenseView.setMovementMethod(LinkMovementMethod.getInstance());
        licenseView.setText(Html
                .fromHtml(getString(R.string.imprint_license)));
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null)
            activity.updateToolbar(-1, getString(R.string.pref_imprint));
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.fragment_imprint_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.feedback:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "julianctni@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "feedback mensaDD");
                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback_mail)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
